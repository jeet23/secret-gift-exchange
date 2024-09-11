package com.tenable.gifts.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.ThrowableProblem
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class GlobalExceptionHandler : ProblemHandling {
    @ExceptionHandler(value = [NotEnoughParticipantsException::class])
    fun notFound(
        ex: NotEnoughParticipantsException,
        request: HttpServletRequest,
    ): ResponseEntity<ThrowableProblem> =
        respondWith(
            Status.BAD_REQUEST,
            ex.message,
        )

    @ExceptionHandler(value = [NoValidReceiverFoundException::class])
    fun notFound(
        ex: NoValidReceiverFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ThrowableProblem> =
        respondWith(
            Status.CONFLICT,
            ex.message,
        )

    @ExceptionHandler(value = [MemberNotFoundException::class])
    fun notFound(
        ex: MemberNotFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ThrowableProblem> =
        respondWith(
            Status.NOT_FOUND,
            ex.message,
        )

    private fun respondWith(
        status: Status,
        detail: String?,
    ): ResponseEntity<ThrowableProblem> {
        val entityBuilder = ResponseEntity.status(HttpStatus.valueOf(status.statusCode))
        val problemBody = detail?.let { Problem.valueOf(status, it) } ?: Problem.valueOf(status)
        return entityBuilder.body(problemBody)
    }
}
