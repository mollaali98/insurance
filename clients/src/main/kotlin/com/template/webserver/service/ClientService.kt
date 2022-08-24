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
import org.springframework.stereotype.Service


@Service
class ClientService(
        private val userRepository: UserRepository,
        rpc: NodeRPCConnection
) {

    private val proxyClient = rpc.clientProxy

    fun getPeers() = proxyClient.networkMapSnapshot().map { it.legalIdentities.first().name.toX500Name().toDisplayString() }

    fun getInsuranceUsers() = userRepository.findByRole(ERole.ROLE_INSURANCE)

    fun getInsurances() = proxyClient.vaultQuery(InsuranceState::class.java)

    fun getClaimsByPolicyNumber(policyNumber: String): Vault.Page<InsuranceState> {
        val criteria = QueryCriteria.VaultCustomQueryCriteria(
                builder {
                    InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber, false)
                }
        ).and(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED))
        return proxyClient.vaultQueryByCriteria(criteria, InsuranceState::class.java)
    }

    fun addClaim(claimInfo: ClaimInfo, policyNumber: String): SignedTransaction =
            proxyClient.startFlow(::InsuranceClaim, claimInfo, policyNumber).returnValue.get()

    fun getMatchingPasties(username: String): Triple<String, String, Set<Party>> {
        val user = userRepository.findByUsername(username).orElseThrow { error("User with name $username not fount") }
        val roles = user.roles.map { it.name }
        return when {
            roles.contains(ERole.ROLE_CLIENT) -> Triple(user.username, user.networkId, proxyClient.partiesFromName("Client", false))
            roles.contains(ERole.ROLE_INSURANCE) -> Triple(user.username, user.networkId, proxyClient.partiesFromName("Insurance", false))
            else -> error("User role is not supported")
        }
    }

    fun getPolicyIssuer(): Pair<Party, String> {
        val user = userRepository.findByRole(ERole.ROLE_INSURANCE).firstOrNull() ?: error("User not found")
        return proxyClient.partiesFromName("Insurance", false).first() to user.networkId
    }

    fun issueInsurance(networkId: String, insuranceInfo: InsuranceInfo, policyIssuer: Party): SignedTransaction =
            proxyClient.startFlow(::IssueInsurance, insuranceInfo, networkId, policyIssuer).returnValue.get()
}