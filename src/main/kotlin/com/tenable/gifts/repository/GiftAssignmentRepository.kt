package com.tenable.gifts.repository

import com.tenable.gifts.dao.GiftAssignment
import com.tenable.gifts.dao.Participant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GiftAssignmentRepository : JpaRepository<GiftAssignment, Long> {
    // Find if a participant has given a gift within the last 3 years
    fun findByGiverAndYearGreaterThan(
        giver: Participant,
        year: Int
    ): List<GiftAssignment>
}