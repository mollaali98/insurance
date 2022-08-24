package com.template.flows.businessmembership

import co.paralleluniverse.fibers.Suspendable
import net.corda.bn.flows.CreateGroupFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name

@StartableByRPC
class CreateNetworkSubGroup(
        private val networkId: String,
        private val groupName: String,
        private val groupParticipants: Set<UniqueIdentifier> = emptySet()
) : FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        // Obtain a reference from a notary we wish to use.
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))
        val groupId = UniqueIdentifier()
        subFlow(CreateGroupFlow(networkId, groupId, groupName, groupParticipants, notary))
        return "\n${this.groupName} has created under BN network (${networkId})" +
                "GroupId: $groupId" +
                "\nAdded participants(shown by membershipId): ${groupParticipants.map { "\n- $it" }}"
    }
}