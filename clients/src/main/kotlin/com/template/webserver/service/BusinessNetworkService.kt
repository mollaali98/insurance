package com.template.webserver.service

import com.template.flows.businessmembership.*
import com.template.states.FirmType
import com.template.webserver.NodeRPCConnection
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.messaging.startFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.stereotype.Service
import java.time.Duration


@Service
class BusinessNetworkService(rpc: NodeRPCConnection) {

    private val proxyNetworkOperator = rpc.networkOperatorProxy
    private val proxyInsurance = rpc.insuranceProxy
    private val proxyClient = rpc.clientProxy

    fun getNetworkOperatorParty() = proxyNetworkOperator.nodeInfo().legalIdentities.first()

    fun createNetwork() = proxyNetworkOperator.startFlow(::CreateNetwork).returnValue.get()

    fun queryAllMembers() = proxyNetworkOperator.startFlow(::QueryAllMembers).returnValue.get()

    fun activeMembers(membershipId: UniqueIdentifier) = proxyNetworkOperator.startFlow(::ActiveMembers, membershipId).returnValue.get()

    fun createNetworkSubGroup(
            networkId: String,
            groupName: String,
            groupParticipants: Set<UniqueIdentifier> = emptySet()
    ) = proxyNetworkOperator.startFlow(::CreateNetworkSubGroup, networkId, groupName, groupParticipants).returnValue.get()

    fun assignBNIdentity(
            firmType: FirmType,
            membershipId: UniqueIdentifier,
            bnIdentity: String
    ) = proxyNetworkOperator.startFlow(::AssignBNIdentity, firmType, membershipId, bnIdentity).returnValue.get()

    fun assignPolicyIssuerRole(
            membershipId: UniqueIdentifier,
            networkId: String
    ): SignedTransaction? {
        val result = proxyNetworkOperator.startFlow(::AssignPolicyIssuerRole, membershipId, networkId).returnValue.getOrThrow(Duration.ofSeconds(30))
        return result
    }

    fun assignPolicyIReceiverRole(
            membershipId: UniqueIdentifier,
            networkId: String
    ) = proxyNetworkOperator.startFlow(::AssignPolicyIReceiverRole, membershipId, networkId).returnValue.get()

    fun getAllMembershipState() = proxyNetworkOperator.vaultQuery(MembershipState::class.java).states.map { it.state.data }

    fun requestMembershipInsurance(authorisedParty: Party, networkId: String) = proxyInsurance.startFlow(::RequestMembership, authorisedParty, networkId).returnValue.get()

    fun requestMembershipClient(authorisedParty: Party, networkId: String) = proxyClient.startFlow(::RequestMembership, authorisedParty, networkId).returnValue.get()
}
