package com.template.webserver.controller

import com.template.states.*
import com.template.webserver.model.User
import com.template.webserver.service.InsuranceService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal


@RestController
@RequestMapping("/insurance")
open class InsuranceController(private val insuranceService: InsuranceService) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    @GetMapping(value = ["/peers"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun getPeers(): ResponseEntity<List<String>> =
            ResponseEntity.status(HttpStatus.OK).body(insuranceService.getPeers())

    @GetMapping(value = ["/client"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun getClients(): ResponseEntity<List<User>> =
            ResponseEntity.status(HttpStatus.OK).body(insuranceService.getClients())

    @PostMapping(value = ["/{name}"])
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun createInsurance(
            principal: Principal?,
            @RequestBody insuranceInfo: InsuranceInfo,
            @PathVariable name: String
    ): ResponseEntity<String> {
        val (username, networkId, matchingPasties) = insuranceService.getMatchingPasties(name)
        logger.info("-------------")
        logger.info(name)
        logger.info("-------------")
        return try {
            val result = insuranceService.issueInsurance(
                    networkId,
                    insuranceInfo.addUsers(username),
                    matchingPasties.first()
            )
            ResponseEntity.status(HttpStatus.CREATED).body("Issue Insurance ${result.id} Completed")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun getInsurances(): ResponseEntity<*> =
            try {
                val result = insuranceService.getInsurances().states.toInsuranceInfo()
                ResponseEntity.status(HttpStatus.OK).body(result)
            } catch (e: Exception) {
                ResponseEntity.badRequest().body(e.message)
            }

    @PostMapping(value = ["/claim/{policyNumber}"])
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun addClaim(@RequestBody claimInfo: ClaimInfo, @PathVariable policyNumber: String): ResponseEntity<String> {
        print("\nclaimInfo---------")
        print(claimInfo)
        print("\npolicyNumber---------")
        print(policyNumber)
        print("---------")
        return try {
            val stx = insuranceService.addClaim(claimInfo, policyNumber)
            ResponseEntity.status(HttpStatus.CREATED).body("Claim filed ${stx.id}")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @GetMapping(value = ["/claim/{policyNumber}"])
    @PreAuthorize("hasRole('ROLE_INSURANCE')")
    open fun getClaimsByPolicyNumber(@PathVariable policyNumber: String): ResponseEntity<*> =
            try {
                val result = insuranceService.getClaimsByPolicyNumber(policyNumber).states.toClaimInfo()
                ResponseEntity.status(HttpStatus.OK).body(result)
            } catch (e: Exception) {
                ResponseEntity.badRequest().body(e.message)
            }
}