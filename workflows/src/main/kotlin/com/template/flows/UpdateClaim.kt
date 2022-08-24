package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.InsuranceContract
import com.template.flows.businessmembership.BusinessNetworkIntegrationFlow
import com.template.flows.service.InsuranceService
import com.template.states.ClaimUpdate
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


@InitiatingFlow
@StartableByRPC
class UpdateClaim(
        private val claimUpdate: ClaimUpdate
) : BusinessNetworkIntegrationFlow<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val service = serviceHub.cordaService(InsuranceService::class.java)
        val inputStateAndRef = service.findInsuranceByPolicyNumber(claimUpdate.policyNumber)

        val client = inputStateAndRef.state.data.client
        val insurance = ourIdentity
        val networkId = inputStateAndRef.state.data.networkId
        businessNetworkFullVerificationClaimUpdate(networkId, insurance, client)
        val (policyIssuerMembership, clientMembership) = businessNetworkPartialVerification(networkId, insurance, client)
        val input = inputStateAndRef.state.data
        // Create the output
        val inputClaims = input.claims.filter { it.claimNumber != claimUpdate.claimNumber }
        val updateClaim = input.claims.firstOrNull { it.claimNumber == claimUpdate.claimNumber }
                ?: throw IllegalArgumentException("Claim with claimNumber ${claimUpdate.claimNumber} not found")

        val output = input.copy(claims = inputClaims + updateClaim.copy(claimStatus = claimUpdate.claimStatus))
        // Build the transaction.
        val txBuilder = TransactionBuilder(inputStateAndRef.state.notary)
                .addInputState(inputStateAndRef)
                .addOutputState(output)
                .addCommand(InsuranceContract.Commands.UpdateClaim(claimNumber = updateClaim.claimNumber), listOf(client.owningKey, insurance.owningKey))
                .addReferenceState(ReferencedStateAndRef(policyIssuerMembership))
                .addReferenceState(ReferencedStateAndRef(clientMembership))
        // Verify the transaction
        txBuilder.verify(serviceHub)

        // Sign the transaction
        val session: FlowSession = initiateFlow(client)
        val stx = serviceHub.signInitialTransaction(txBuilder)
        val fullySignedTx = subFlow(CollectSignaturesFlow(stx, setOf(session)))
        return subFlow(FinalityFlow(fullySignedTx, listOf(session)))
    }

}

@InitiatedBy(UpdateClaim::class)
class UpdateClaimResponder(val counterpartySession: FlowSession) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}