package com.tenable.gifts

import com.tenable.gifts.dao.Participant
import com.tenable.gifts.exceptions.MemberNotFoundException
import com.tenable.gifts.repository.GiftAssignmentRepository
import com.tenable.gifts.repository.ParticipantRepository
import com.tenable.gifts.service.GiftExchangeService
import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class MembersTest {

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
    fun `addParticipant should save a new participant`() {
        // Given
        val participant = Participant(1L, "John Doe", "john@example.com")
        every { participantRepository.save(participant) } returns participant

        // When
        val result = giftExchangeService.addParticipant(participant)

        // Then
        verify(exactly = 1) { participantRepository.save(participant) }
        Assertions.assertEquals(participant.name, result.name)
    }

    @Test
    fun `updateParticipant should update an existing participant`() {
        // Given
        val participant = Participant(1L, "John Doe", "john@example.com")
        val updatedParticipant = Participant(1L, "John Smith", "johnsmith@example.com")

        every { participantRepository.findById(1L) } returns Optional.of(participant)
        every { participantRepository.save(updatedParticipant) } returns updatedParticipant

        // When
        val result = giftExchangeService.updateParticipant(1L, updatedParticipant)

        // Then
        verify(exactly = 1) { participantRepository.findById(1L) }
        verify(exactly = 1) { participantRepository.save(updatedParticipant) }
        Assertions.assertEquals(updatedParticipant.name, result?.name)
    }

    @Test
    fun `updateParticipant should throw exception if participant does not exist`() {
        // Given
        val updatedParticipant = Participant(1L, "John Smith", "johnsmith@example.com")

        every { participantRepository.findById(1L) } returns Optional.empty()

        // When/Then
        assertThrows<MemberNotFoundException> {
            giftExchangeService.updateParticipant(1L, updatedParticipant)
        }
        verify(exactly = 1) { participantRepository.findById(1L) }
        verify(exactly = 0) { participantRepository.save(any()) }
    }

    @Test
    fun `getAllParticipants should return list of participants`() {
        // Given
        val participants = listOf(
            Participant(1L, "John Doe", "john@example.com"),
            Participant(2L, "Jane Doe", "jane@example.com")
        )

        every { participantRepository.findAll() } returns participants

        // When
        val result = giftExchangeService.getAllParticipants()

        // Then
        verify(exactly = 1) { participantRepository.findAll() }
        Assertions.assertEquals(participants.map { it.name }, result.map { it.name })
    }

    @Test
    fun `getParticipant should return the participant if exists`() {
        // Given
        val participant = Participant(1L, "John Doe", "john@example.com")
        every { participantRepository.findById(1L) } returns Optional.of(participant)

        // When
        val result = giftExchangeService.getParticipant(1L)

        // Then
        verify(exactly = 1) { participantRepository.findById(1L) }
        Assertions.assertEquals(participant.name, result.get().name)
    }

    @Test
    fun `getParticipant should throw exception if participant does not exist`() {
        // Given
        every { participantRepository.findById(1L) } returns Optional.empty()

        // When/Then
        assertThrows<MemberNotFoundException> {
            giftExchangeService.getParticipant(1L)
        }
        verify(exactly = 1) { participantRepository.findById(1L) }
    }

    @Test
    fun `deleteParticipant should delete the participant if exists`() {
        // Given
        val participant = Participant(1L, "John Doe", "john@example.com")
        every { participantRepository.findById(1L) } returns Optional.of(participant)
        every { participantRepository.deleteById(1L) } just Runs

        // When
        giftExchangeService.deleteParticipant(1L)

        // Then
        verify(exactly = 1) { participantRepository.deleteById(1L) }
    }
}