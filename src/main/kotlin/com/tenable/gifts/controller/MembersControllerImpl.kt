package com.tenable.gifts.controller

import com.tenable.generated.fabrikt.controllers.MembersController
import com.tenable.generated.fabrikt.models.Member
import com.tenable.generated.fabrikt.models.MemberAddOrUpdate
import com.tenable.generated.fabrikt.models.Members
import com.tenable.gifts.dao.Participant
import com.tenable.gifts.service.SecretSantaService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class MembersControllerImpl(val secretSantaService: SecretSantaService) : MembersController {
    override fun get(): ResponseEntity<Members> {
        val allMembers = secretSantaService.getAllParticipants()
        val data = Members(allMembers.map { Member(it.id, it.name) })
        return ResponseEntity.ok(data)
    }

    override fun post(memberAddOrUpdate: MemberAddOrUpdate): ResponseEntity<Member> {
        val member = secretSantaService.addParticipant(Participant(name = memberAddOrUpdate.name))
        return ResponseEntity.created(URI.create(member.id)).build()
    }

    override fun getById(id: String): ResponseEntity<Member> {
        val maybeMember = secretSantaService.getParticipant(id.toLong())
        return if (maybeMember.isEmpty)
            ResponseEntity.notFound().build()
        else
            ResponseEntity.ok(maybeMember.get())
    }

    override fun putById(memberAddOrUpdate: MemberAddOrUpdate, id: String): ResponseEntity<Member> {
        val updatedMember =
            secretSantaService.updateParticipant(id.toLong(), Participant(name = memberAddOrUpdate.name))
        return ResponseEntity.ok(updatedMember)
    }


    override fun deleteById(id: String): ResponseEntity<Unit> {
        secretSantaService.deleteParticipant(id.toLong())
        return ResponseEntity.noContent().build()
    }


}