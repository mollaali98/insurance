package com.template.webserver.service

import com.template.schema.InsuranceSchemaV1
import com.template.states.InsuranceState
import com.template.webserver.NodeRPCConnection
import com.template.webserver.model.ERole
import com.template.webserver.repository.UserRepository
import net.corda.core.internal.toX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import org.springframework.stereotype.Service


@Service
class ClientService(
        private val userRepository: UserRepository,
        rpc: NodeRPCConnection
) {

    private val proxyInsuree = rpc.clientProxy

    fun getPeers() = proxyInsuree.networkMapSnapshot().map { it.legalIdentities.first().name.toX500Name().toDisplayString() }

    fun getInsuranceUsers() = userRepository.findByRole(ERole.ROLE_INSURANCE)

    fun getInsurances() = proxyInsuree.vaultQuery(InsuranceState::class.java)

    fun getClaimsByPolicyNumber(policyNumber: String): Vault.Page<InsuranceState> {
        val criteria = QueryCriteria.VaultCustomQueryCriteria(
                builder {
                    InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber, false)
                }
        ).and(QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED))
        return proxyInsuree.vaultQueryByCriteria(criteria, InsuranceState::class.java)
    }
}