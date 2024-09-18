package com.tenable.gifts

import com.tenable.gifts.dao.GiftAssignment
import com.tenable.gifts.dao.Participant
import com.tenable.gifts.exceptions.NoValidReceiverFoundException
import com.tenable.gifts.exceptions.NotEnoughParticipantsException
import com.tenable.gifts.repository.GiftAssignmentRepository
import com.tenable.gifts.repository.ParticipantRepository
import com.tenable.gifts.service.GiftExchangeService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.Year
import kotlin.random.Random

@SpringBootTest
class GiftExchangeServiceTest {

    private lateinit var giftExchangeService: GiftExchangeService
    private lateinit var participantRepository: ParticipantRepository
    private lateinit var giftAssignmentRepository: GiftAssignmentRepository

    @BeforeEach
    fun setup() {
        participantRepository = mockk()
        giftAssignmentRepository = mockk()
        giftExchangeService = GiftExchangeService(participantRepository, giftAssignmentRepository)
    }

    @Test
    fun `should assign each participant exactly one gift`() {
        val participants = listOf(
            Participant(1L, "Alice"),
            Participant(2L, "Bob"),
            Participant(3L, "Charlie")
        )

        every { participantRepository.findAll() } returns participants
        every { giftAssignmentRepository.findByGiverAndYearGreaterThan(any(), any()) } returns emptyList()
        every { giftAssignmentRepository.save(any()) } returns mockk()

        val random = Random(42)
        val assignments = giftExchangeService.drawNames(random)

        // Assert each participant is a giver exactly once
        assertEquals(participants.size, assignments.size)

        // Assert each participant is a receiver exactly once
        val receivers = assignments.values.toSet()
        assertEquals(participants.size, receivers.size)

        // Ensure no one is assigned to themselves
        assignments.forEach { (giver, receiver) ->
            assertNotEquals(giver, receiver, "Giver and receiver should not be the same")
        }
    }

    @Test
    fun `should not allow a participant to give to the same person within 3 years`() {
        val participants = listOf(
            Participant(1L, "Alice" ),
            Participant(2L, "Bob"),
            Participant(3L, "Charlie"),
            Participant(4L, "Tom")
        )

        // Create an assignment such that Alice can't give to Bob
        val pastAssignment = GiftAssignment(
            id = 1L,
            giver = participants[0],  // Alice
            receiver = participants[1],  // Bob
            year = Year.now().value - 2  // Assigned 2 years ago
        )

        every { participantRepository.findAll() } returns participants
        every {
            giftAssignmentRepository.findByGiverAndYearGreaterThan(
                participants[0],
                Year.now().value - 3
            )
        } returns listOf(pastAssignment)
        every {
            giftAssignmentRepository.findByGiverAndYearGreaterThan(
                participants[1],
                Year.now().value - 3
            )
        } returns emptyList()
        every {
            giftAssignmentRepository.findByGiverAndYearGreaterThan(
                participants[2],
                Year.now().value - 3
            )
        } returns emptyList()
        every {
            giftAssignmentRepository.findByGiverAndYearGreaterThan(
                participants[3],
                Year.now().value - 3
            )
        } returns emptyList()
        every { giftAssignmentRepository.save(any()) } returns mockk()

        val random = Random(42)

        val assignments = giftExchangeService.drawNames(random)

        // Verify that Alice did not get Bob as a receiver
        assertNotEquals(participants[1], assignments[participants[0]], "Alice should not be assigned to Bob")
    }

    @Test
    fun `should throw exception if not enough participants`() {
        val participants = listOf(Participant(1L, "Alice"))

        every { participantRepository.findAll() } returns participants

        val exception = assertThrows(NotEnoughParticipantsException::class.java) {
            giftExchangeService.drawNames()
        }

        assertEquals("Not enough participants to perform gift-exchange, please add more members!", exception.message)
    }

    @Test
    fun `should throw exception if no valid receivers remain for a participant`() {
        val participants = listOf(
            Participant(1L, "Alice"),
            Participant(2L, "Bob"),
            Participant(3L, "Charlie")
        )

        // Simulate a scenario where everyone is blocked due to the 3-year rule
        val pastAssignment1 = GiftAssignment(null, participants[0], participants[1], Year.now().value - 1)
        val pastAssignment2 = GiftAssignment(null, participants[1], participants[2], Year.now().value - 1)
        val pastAssignment3 = GiftAssignment(null, participants[2], participants[0], Year.now().value - 1)

        every { participantRepository.findAll() } returns participants
        every { giftAssignmentRepository.findByGiverAndYearGreaterThan(any(), any()) } returns listOf(
            pastAssignment1,
            pastAssignment2,
            pastAssignment3
        )
        every { giftAssignmentRepository.save(any()) } returns mockk()

        val random = Random(42)
        val exception = assertThrows(NoValidReceiverFoundException::class.java) {
            giftExchangeService.drawNames(random)
        }

        assertThat(exception.message, containsString("Unable to assign a valid gift-recipient"))
    }

    @Test
    fun `should save all gift assignments to the repository`() {
        val participants = listOf(
            Participant(1L, "Alice"),
            Participant(2L, "Bob"),
            Participant(3L, "Charlie")
        )

        every { participantRepository.findAll() } returns participants
        every { giftAssignmentRepository.findByGiverAndYearGreaterThan(any(), any()) } returns emptyList()
        every { giftAssignmentRepository.save(any()) } returns mockk()

        val random = Random(42)
        giftExchangeService.drawNames(random)

        // Verify that the save method was called for each participant
        verify(exactly = participants.size) { giftAssignmentRepository.save(any()) }
    }

    @Test
    fun `getAllGiftExchanges should return a list of gift exchanges`() {
        // Given
        val giftExchanges = listOf(
            GiftAssignment(1L, Participant(id = 1L, name = "John"), Participant(id = 2L, name = "Alice"), 2023),
            GiftAssignment(2L, Participant(id = 3L, name = "Bob"), Participant(id = 4L, name = "Charlie"), 2023)
        )

        every { giftAssignmentRepository.findAll() } returns giftExchanges

        // When
        val result = giftExchangeService.getAllGiftExchanges()

        // Then
        verify(exactly = 1) { giftAssignmentRepository.findAll() }
        assertEquals(giftExchanges.size, result.size)
        assertEquals(giftExchanges.map { it.giver.id.toString() }, result.map { it.memberId })
        assertEquals(giftExchanges.map { it.receiver.id.toString() }, result.map { it.recipientMemberId })
    }
}
