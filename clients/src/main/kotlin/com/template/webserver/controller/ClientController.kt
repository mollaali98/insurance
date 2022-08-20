package com.template.webserver.controller

import com.template.states.toClaimInfo
import com.template.states.toInsuranceInfo
import com.template.webserver.model.User
import com.template.webserver.service.ClientService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/client")
open class ClientController(private val clientService: ClientService) {

    @GetMapping(value = ["/peers"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun getPeers(): ResponseEntity<List<String>> =
            ResponseEntity.status(HttpStatus.OK).body(clientService.getPeers())

    @GetMapping(value = ["/insurances"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun getInsuranceUsers(): ResponseEntity<List<User>> =
            ResponseEntity.status(HttpStatus.OK).body(clientService.getInsuranceUsers())

    @GetMapping(value = ["/insurance"])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun getInsurances(): ResponseEntity<*> =
            try {
                val result = clientService.getInsurances().states.toInsuranceInfo()
                ResponseEntity.status(HttpStatus.OK).body(result)
            } catch (e: Exception) {
                ResponseEntity.badRequest().body(e.message)
            }

    @GetMapping(value = ["/insurance/claim/{policyNumber}"])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun getClaimsByPolicyNumber(@PathVariable policyNumber: String): ResponseEntity<*> =
            try {
                val result = clientService.getClaimsByPolicyNumber(policyNumber).states.toClaimInfo()
                ResponseEntity.status(HttpStatus.OK).body(result)
            } catch (e: Exception) {
                ResponseEntity.badRequest().body(e.message)
            }
}