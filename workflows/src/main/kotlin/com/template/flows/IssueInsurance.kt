package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.InsuranceContract
import com.template.flows.businessmembership.BusinessNetworkIntegrationFlow
import com.template.states.*
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class IssueInsurance(
        val insuranceInfo: InsuranceInfo,
        val networkId: String,
        val client: Party
) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        businessNetworkFullVerification(networkId, ourIdentity, client)
        // Obtain a reference from a notary we wish to use.
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2

        val policyIssuer = ourIdentity

        val propertyInfo: PropertyInfo = insuranceInfo.propertyInfo
        val property = propertyInfo.toPropertyData()

        // Build the insurance output state.
        val output = insuranceInfo.toInsuranceState(networkId, policyIssuer, client, property)

        val (policyIssuerMembership, clientMembership) = businessNetworkPartialVerification(networkId, ourIdentity, client)

        // Build the transaction
        val txBuilder = TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(InsuranceContract.Commands.IssueInsurance(), listOf(policyIssuer.owningKey, client.owningKey))
                .addReferenceState(ReferencedStateAndRef(policyIssuerMembership))
                .addReferenceState(ReferencedStateAndRef(clientMembership))
        // Verify the transaction
        txBuilder.verify(serviceHub)

        // Sign the transaction
        val stx = serviceHub.signInitialTransaction(txBuilder)

        // Call finality Flow
        val ownerSession = initiateFlow(client)
        val fullySignedTx = subFlow(CollectSignaturesFlow(stx, setOf(ownerSession)))

        return subFlow(FinalityFlow(fullySignedTx, setOf(ownerSession)))
    }
}

@InitiatedBy(IssueInsurance::class)
class IssueInsuranceResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        subFlow(object : SignTransactionFlow(counterpartySession) {
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}