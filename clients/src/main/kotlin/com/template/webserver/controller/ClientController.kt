package com.template.webserver.controller

import com.template.states.*
import com.template.webserver.model.User
import com.template.webserver.service.ClientService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
@RequestMapping("/client")
open class ClientController(private val clientService: ClientService) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

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

    @PostMapping(value = ["insurance/claim/{policyNumber}"])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun addClaim(@RequestBody claimInfo: ClaimInfo, @PathVariable policyNumber: String): ResponseEntity<String> {
        print("\nclaimInfo---------")
        print(claimInfo)
        print("\npolicyNumber---------")
        print(policyNumber)
        print("---------")
        return try {
            val stx = clientService.addClaim(claimInfo, policyNumber)
            ResponseEntity.status(HttpStatus.CREATED).body("Claim filed ${stx.id}")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @PostMapping(value = ["/insurance"])
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    open fun createInsurance(
            principal: Principal?,
            @RequestBody insuranceInfo: InsuranceInfo
    ): ResponseEntity<String> {
        logger.info("-------------")
        logger.info(principal?.name)
        logger.info("-------------")
        val (policyIssuer, networkId) = clientService.getPolicyIssuer()
        return try {
            val result = clientService.issueInsurance(
                    networkId,
                    insuranceInfo.addUsers(principal!!.name),
                    policyIssuer
            )
            ResponseEntity.status(HttpStatus.CREATED).body("Issue Insurance ${result.id} Completed")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }
}