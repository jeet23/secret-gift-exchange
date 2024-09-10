package com.tenable.gifts.repository

import com.tenable.gifts.dao.Participant
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository

@Repository
interface ParticipantRepository : JpaRepository<Participant, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findAll(): List<Participant>
}