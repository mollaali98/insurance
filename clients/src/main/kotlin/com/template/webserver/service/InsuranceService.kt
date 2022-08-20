package com.template.webserver.service

import com.template.flows.InsuranceClaim
import com.template.flows.IssueInsurance
import com.template.schema.InsuranceSchemaV1
import com.template.states.ClaimInfo
import com.template.states.InsuranceInfo
import com.template.states.InsuranceState
import com.template.webserver.NodeRPCConnection
import com.template.webserver.model.ERole
import com.template.webserver.repository.UserRepository
import net.corda.core.identity.Party
import net.corda.core.internal.toX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.springframework.stereotype.Service

@Service
class InsuranceService(
        private val userRepository: UserRepository,
        rpc: NodeRPCConnection
) {

    private val proxyInsurer = rpc.insuranceProxy

    fun getPeers() = proxyInsurer.networkMapSnapshot().map { it.legalIdentities.first().name.toX500Name().toDisplayString() }

    fun getClients() = userRepository.findByRole(ERole.ROLE_CLIENT)

//    fun getMatchingPasties(username: String): Triple<String, String, Set<Party>> {
//        val user = userRepository.findByUsername(username).orElseThrow { error("User with name $username not fount") }
//        val roles = user.roles.map { it.name }
//        return when {
//            roles.contains(ERole.ROLE_CLIENT) -> Triple(user.username, user.networkId, proxyInsurer.partiesFromName("Client", false))
//            roles.contains(ERole.ROLE_INSURANCE) -> Triple(user.username, user.networkId, proxyInsurer.partiesFromName("Insurance", false))
//            else -> error("User role is not supported")
//        }
//    }
//
//    fun issueInsurance(networkId: String, insuranceInfo: InsuranceInfo, client: Party): SignedTransaction =
//            proxyInsurer.startFlow(::IssueInsurance, insuranceInfo, networkId, client).returnValue.get()

    fun getInsurances() = proxyInsurer.vaultQuery(InsuranceState::class.java)

//    fun addClaim(claimInfo: ClaimInfo, policyNumber: String): SignedTransaction =
//            proxyInsurer.startFlow(::InsuranceClaim, claimInfo, policyNumber).returnValue.get()


    fun getClaimsByPolicyNumber(policyNumber: String): Vault.Page<InsuranceState> {
        val criteria = QueryCriteria.VaultCustomQueryCriteria(
                builder {
                    InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber, false)
                }
        ).and(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED))
        return proxyInsurer.vaultQueryByCriteria(criteria, InsuranceState::class.java)
    }
}

fun X500Name.toDisplayString(): String = BCStyle.INSTANCE.toString(this)
