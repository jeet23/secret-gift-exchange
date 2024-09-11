package com.tenable.gifts.controller

import com.tenable.generated.fabrikt.controllers.MembersController
import com.tenable.generated.fabrikt.models.Member
import com.tenable.generated.fabrikt.models.MemberAddOrUpdate
import com.tenable.generated.fabrikt.models.Members
import com.tenable.gifts.dao.Participant
import com.tenable.gifts.service.GiftExchangeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class MembersControllerImpl(val giftExchangeService: GiftExchangeService) : MembersController {
    override fun get(): ResponseEntity<Members> {
        val allMembers = giftExchangeService.getAllParticipants()
        val data = Members(allMembers.map { Member(it.id, it.name) })
        return ResponseEntity.ok(data)
    }

    override fun post(memberAddOrUpdate: MemberAddOrUpdate): ResponseEntity<Member> {
        val member = giftExchangeService.addParticipant(Participant(name = memberAddOrUpdate.name))
        return ResponseEntity.created(URI.create(member.id)).build()
    }

    override fun getById(id: String): ResponseEntity<Member> {
        val maybeMember = giftExchangeService.getParticipant(id.toLong())
        return if (maybeMember.isEmpty)
            ResponseEntity.notFound().build()
        else
            ResponseEntity.ok(maybeMember.get())
    }

    override fun putById(memberAddOrUpdate: MemberAddOrUpdate, id: String): ResponseEntity<Member> {
        val updatedMember =
            giftExchangeService.updateParticipant(id.toLong(), Participant(name = memberAddOrUpdate.name))
        return ResponseEntity.ok(updatedMember)
    }


    override fun deleteById(id: String): ResponseEntity<Unit> {
        giftExchangeService.deleteParticipant(id.toLong())
        return ResponseEntity.noContent().build()
    }


}