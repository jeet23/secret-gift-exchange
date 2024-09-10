package com.tenable.gifts.service

import com.tenable.generated.fabrikt.models.GiftExchange
import com.tenable.generated.fabrikt.models.Member
import com.tenable.gifts.dao.GiftAssignment
import com.tenable.gifts.dao.Participant
import com.tenable.gifts.repository.GiftAssignmentRepository
import com.tenable.gifts.repository.ParticipantRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Year
import java.util.Optional
import kotlin.random.Random

@Service
class SecretSantaService(
    val participantRepository: ParticipantRepository,
    val giftAssignmentRepository: GiftAssignmentRepository
) {

    fun addParticipant(participant: Participant): Member {
        val member = participantRepository.save(participant)
        return Member(member.id.toString(), member.name)
    }

    fun updateParticipant(id: Long, update: Participant): Member? {
        val existingParticipant = participantRepository.findById(id)
        if (existingParticipant.isEmpty)
            return null
        val updatedParticipant = existingParticipant.get().copy(name = update.name, email = update.email)
        val member = participantRepository.save(updatedParticipant)
        return Member(member.id.toString(), member.name)
    }

    fun getAllParticipants(): List<Member> {
        return participantRepository.findAll().map { Member(it.id.toString(), it.name) }
    }

    fun getParticipant(id: Long): Optional<Member> {
        return participantRepository.findById(id).map { Member(it.id.toString(), it.name) }
    }

    fun deleteParticipant(id: Long) {
        return participantRepository.deleteById(id)
    }

    fun getAllGiftExchanges(): List<GiftExchange> {
        return giftAssignmentRepository.findAll().map { GiftExchange(it.giver.name, it.receiver.name) }
    }

    @Transactional
    fun drawNames(random: Random = Random.Default): Map<Participant, Participant> {
        val participants = participantRepository.findAll()

        // Ensure at least two participants for the draw
        if (participants.size < 2) {
            throw IllegalStateException("Not enough participants to draw names")
        }

        // Create a mutable list of receivers to ensure each participant gets only one gift
        val availableReceivers = participants.toMutableList()

        // Map to store the final assignments
        val assignments = mutableMapOf<Participant, Participant>()

        // Shuffle participants for randomness
        participants.shuffled(random).forEach { giver ->
            // Remove any previous gift assignments that would violate the 3-year rule
            val invalidReceivers = giftAssignmentRepository
                .findByGiverAndYearGreaterThan(giver, Year.now().value - 3)
                .map { it.receiver }

            // Filter out invalid receivers (including the giver themselves)
            val validReceivers = availableReceivers.filter { it != giver && !invalidReceivers.contains(it) }

            if (validReceivers.isEmpty()) {
                throw IllegalStateException("Unable to assign a valid receiver for $giver")
            }

            // Randomly pick a valid receiver
            val receiver = validReceivers.random(random)

            // Save assignment and remove the receiver from the list of available receivers
            assignments[giver] = receiver
            availableReceivers.remove(receiver)

            // Save assignment to the repository
            giftAssignmentRepository.save(GiftAssignment(null, giver, receiver, Year.now().value))
        }

        return assignments
    }
}