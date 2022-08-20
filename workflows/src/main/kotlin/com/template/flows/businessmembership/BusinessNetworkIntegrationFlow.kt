package com.template.flows.businessmembership

import co.paralleluniverse.fibers.Suspendable
import com.template.states.ClientIdentity
import com.template.states.InsurerIdentity
import com.template.states.IssuePermissions
import com.template.states.ReceiverPermissions
import net.corda.bn.flows.BNService
import net.corda.bn.flows.IllegalMembershipStatusException
import net.corda.bn.flows.MembershipAuthorisationException
import net.corda.bn.flows.MembershipNotFoundException
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.identity.Party
import java.lang.IllegalStateException


/**
 * This abstract flow is extended by any flow which will use business network membership verification methods.
 */
abstract class BusinessNetworkIntegrationFlow<T> : FlowLogic<T>() {

    @Suspendable
    protected fun businessNetworkPartialVerification(networkId: String, insurer: Party, client: Party): Memberships {
        val bnService = serviceHub.cordaService(BNService::class.java)
        val insurerMembership = bnService.getMembership(networkId, insurer)
                ?: throw MembershipNotFoundException("insurer is not part of Business Network with $networkId ID")
        val clientProMembership = bnService.getMembership(networkId, client)
                ?: throw MembershipNotFoundException("client is not part of Business Network with $networkId ID")

        return Memberships(insurerMembership, clientProMembership)
    }

    /**
     * Verifies that [policyIssuer] and [client] are members of Business Network with [networkId] ID, their memberships are active, contain
     * business identity of [MembershipState] type and that policyIssuer is authorised to issue policy.
     *
     * @param networkId ID of the Business Network in which verification is performed.
     * @param policyIssuer Party issuing the policy.
     * @param client Party paying of the policy.
     */
    @Suppress("ComplexMethod", "ThrowsCount")
    @Suspendable
    protected fun businessNetworkFullVerification(networkId: String, policyIssuer: Party, client: Party) {
        checkInsurerIdentity(networkId, policyIssuer, IssuePermissions.CAN_ISSUE_POLICY)
        checkClientIdentity(networkId, client, ReceiverPermissions.CAN_RECEIVE_POLICY)
    }

    @Suppress("ComplexMethod", "ThrowsCount")
    @Suspendable
    protected fun businessNetworkFullVerificationClaim(networkId: String, policyIssuer: Party, client: Party) {
        checkInsurerIdentity(networkId, policyIssuer, IssuePermissions.CAN_ISSUE_CLAIM)
        checkClientIdentity(networkId, client, ReceiverPermissions.CAN_RECEIVE_CLAIM)
    }

    @Suppress("ComplexMethod", "ThrowsCount")
    @Suspendable
    private fun checkInsurerIdentity(networkId: String, policyIssuer: Party, issuePermissions: IssuePermissions) {
        val bnService = serviceHub.cordaService(BNService::class.java)
        try {
            bnService.getMembership(networkId, policyIssuer)?.state?.data?.apply {
                if (!isActive()) {
                    throw IllegalMembershipStatusException("$policyIssuer is not active member of Business Network with $networkId ID")
                }
                if (identity.businessIdentity !is InsurerIdentity) {
                    throw IllegalMembershipBusinessIdentityException("$policyIssuer business identity should be InsurerIdentity")
                }
                if (roles.find { issuePermissions in it.permissions } == null) {
                    throw MembershipAuthorisationException("$policyIssuer is not authorised to issue insurance Policy in Business Network with $networkId ID")
                }
            } ?: throw MembershipNotFoundException("$policyIssuer is not member of Business Network with $networkId ID")
        } catch (e: IllegalStateException) {
            throw MembershipNotFoundException("$policyIssuer is not member of Business Network with $networkId ID")
        }
    }

    @Suppress("ComplexMethod", "ThrowsCount")
    @Suspendable
    private fun checkClientIdentity(networkId: String, client: Party, receiverPermissions: ReceiverPermissions) {
        val bnService = serviceHub.cordaService(BNService::class.java)
        bnService.getMembership(networkId, client)?.state?.data?.apply {
            if (!isActive()) {
                throw IllegalMembershipStatusException("$client is not active member of Business Network with $networkId ID")
            }
            if (identity.businessIdentity !is ClientIdentity) {
                throw IllegalMembershipBusinessIdentityException("$client business identity should be ClientIdentity")
            }
            if (roles.find { receiverPermissions in it.permissions } == null) {
                throw MembershipAuthorisationException("$client is not authorised to receive insurance Policy in Business Network with $networkId ID")
            }
        } ?: throw MembershipNotFoundException("$client is not member of Business Network with $networkId ID")
    }
}

data class Memberships(val MembershipA: StateAndRef<MembershipState>, val MembershipB: StateAndRef<MembershipState>)

/**
 * Exception thrown when membership's business identity is in illegal state.
 */
class IllegalMembershipBusinessIdentityException(message: String) : FlowException(message)