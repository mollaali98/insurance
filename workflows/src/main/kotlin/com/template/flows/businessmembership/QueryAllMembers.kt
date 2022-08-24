package com.template.flows.businessmembership

import co.paralleluniverse.fibers.Suspendable
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC

@StartableByRPC
class QueryAllMembers : FlowLogic<List<Pair<String, UniqueIdentifier>>>() {

    @Suspendable
    override fun call(): List<Pair<String, UniqueIdentifier>> {
        val membershipRequests = serviceHub.vaultService.queryBy(contractStateType = MembershipState::class.java).states
        return membershipRequests.map { it.state.data.identity.cordaIdentity.name.organisation to it.state.data.linearId }
    }
}