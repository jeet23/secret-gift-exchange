package com.tenable.gifts.controller

import com.tenable.generated.fabrikt.controllers.GiftExchangeController
import com.tenable.generated.fabrikt.models.GiftExchange
import com.tenable.generated.fabrikt.models.GiftExchanges
import com.tenable.gifts.service.GiftExchangeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class GiftExchangeControllerImpl(val giftExchangeService: GiftExchangeService): GiftExchangeController {

    override fun get(): ResponseEntity<GiftExchanges> {
        val gifts = giftExchangeService.getAllGiftExchanges()
        return ResponseEntity.ok(GiftExchanges(gifts))
    }

    override fun post(): ResponseEntity<GiftExchanges> {
        val results = giftExchangeService.drawNames()
        val exchanges = GiftExchanges(results.map { GiftExchange(it.key.id.toString(), it.value.id.toString()) })
        return ResponseEntity.ok(exchanges)
    }


}