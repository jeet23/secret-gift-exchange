package com.tenable.gifts.exceptions

import org.zalando.problem.Problem

class NotEnoughParticipantsException(
    val error: String,
): RuntimeException(error)

class NoValidReceiverFoundException(
    val error: String
): RuntimeException(error)

class MemberNotFoundException(
    val error: String
): RuntimeException(error)