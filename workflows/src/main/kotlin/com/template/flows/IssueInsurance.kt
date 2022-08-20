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
import net.corda.core.utilities.unwrap

// Run on Client Node
@InitiatingFlow
@StartableByRPC
class IssueInsurance(
        val insuranceInfo: InsuranceInfo,
        val networkId: String,
        val policyIssuer: Party
) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        businessNetworkFullVerification(networkId, policyIssuer, ourIdentity)
        // Obtain a reference from a notary we wish to use.
        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2
        val client = ourIdentity

        // Initiate flow
        val session: FlowSession = initiateFlow(policyIssuer)
        val output: InsuranceState = try {
            session.sendAndReceive(InsuranceState::class.java, insuranceInfo to networkId).unwrap { it -> it }
        } catch (e: Exception) {
            logger.info("Unable to create Insurance", e)
            error("Unable to create Insurance")
        }

        val (policyIssuerMembership, clientMembership) = businessNetworkPartialVerification(networkId, policyIssuer, client)

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
        val fullySignedTx = subFlow(CollectSignaturesFlow(stx, setOf(session)))
        return subFlow(FinalityFlow(fullySignedTx, setOf(session)))
    }
}

// Run on Insurance Node
@InitiatedBy(IssueInsurance::class)
class IssueInsuranceResponder(val counterpartySession: FlowSession) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        val (insuranceInfo, networkId) = counterpartySession.receive<Pair<InsuranceInfo, String>>().unwrap { it -> it }
        businessNetworkFullVerification(networkId, ourIdentity, counterpartySession.counterparty)
        val propertyInfo: PropertyInfo = insuranceInfo.propertyInfo
        val property = propertyInfo.toPropertyData()

        // Build the insurance output state.
        val output = insuranceInfo.toInsuranceState(networkId, ourIdentity, counterpartySession.counterparty, property)
        counterpartySession.send(output)

        subFlow(object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}