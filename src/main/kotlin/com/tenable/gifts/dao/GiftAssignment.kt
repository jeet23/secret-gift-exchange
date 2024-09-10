package com.tenable.gifts.dao

import jakarta.persistence.*

@Entity
data class GiftAssignment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    val giver: Participant,

    @ManyToOne
    val receiver: Participant,

    val year: Int
)