package com.template.flows.businessmembership

import co.paralleluniverse.fibers.Suspendable
import com.template.states.ClientIdentity
import com.template.states.FirmType
import com.template.states.InsurerIdentity
import net.corda.bn.flows.IllegalFlowArgumentException
import net.corda.bn.flows.ModifyBusinessIdentityFlow
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name

@StartableByRPC
class AssignBNIdentity(
        private val firmType: FirmType,
        private val membershipId: UniqueIdentifier,
        private val bnIdentity: String
) : FlowLogic<String>() {
    @Suspendable
    override fun call(): String {
        // Obtain a reference from a notary we wish to use.
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))
        when (firmType) {
            FirmType.InsuranceFirm -> {
                val insuranceIdentity = InsurerIdentity(bnIdentity).apply {
                    if (!isValid()) {
                        throw IllegalFlowArgumentException("$bnIdentity in not a valid Insurance Identity")
                    }
                }
                subFlow(ModifyBusinessIdentityFlow(membershipId, insuranceIdentity, notary))
            }
            FirmType.ClientFirm -> {
                val careProviderIdentity = ClientIdentity(bnIdentity).apply {
                    if (!isValid()) {
                        throw IllegalFlowArgumentException("$bnIdentity in not a valid Client Identity")
                    }
                }
                subFlow(ModifyBusinessIdentityFlow(membershipId, careProviderIdentity, notary))
            }
        }
        return "Issue a ${this.firmType} BN Identity to member(${this.membershipId})"
    }
}