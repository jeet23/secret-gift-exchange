package com.tenable.gifts.dao

import jakarta.persistence.*

@Entity
data class Participant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    @Version  // Enables optimistic locking
    val version: Int? = null
)

