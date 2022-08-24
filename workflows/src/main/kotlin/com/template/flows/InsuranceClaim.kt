package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.InsuranceContract
import com.template.flows.businessmembership.BusinessNetworkIntegrationFlow
import com.template.flows.service.InsuranceService
import com.template.states.ClaimInfo
import com.template.states.InsuranceState
import com.template.states.toClaim
import net.corda.core.contracts.ReferencedStateAndRef
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

@InitiatingFlow
@StartableByRPC
class InsuranceClaim(
        val claimInfo: ClaimInfo,
        val policyNumber: String
) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val service = serviceHub.cordaService(InsuranceService::class.java)
        val inputStateAndRef = service.findInsuranceByPolicyNumber(policyNumber)

        val client = ourIdentity
        val insurance = inputStateAndRef.state.data.insurance
        val networkId = inputStateAndRef.state.data.networkId

        // Initiate flow
        val session: FlowSession = initiateFlow(insurance)
        businessNetworkFullVerificationClaim(networkId, insurance, client)

        val output: InsuranceState = try {
            session.sendAndReceive(InsuranceState::class.java, Triple(claimInfo, networkId, policyNumber)).unwrap { it -> it }
        } catch (e: Exception) {
            logger.info("Unable to update Insurance", e)
            error("Unable to update Insurance")
        }
        val (policyIssuerMembership, clientMembership) = businessNetworkPartialVerification(networkId, insurance, client)

        // Build the transaction.
        val txBuilder = TransactionBuilder(inputStateAndRef.state.notary)
                .addInputState(inputStateAndRef)
                .addOutputState(output)
                .addCommand(InsuranceContract.Commands.AddClaim(), listOf(client.owningKey, insurance.owningKey))
                .addReferenceState(ReferencedStateAndRef(policyIssuerMembership))
                .addReferenceState(ReferencedStateAndRef(clientMembership))
        // Verify the transaction
        txBuilder.verify(serviceHub)

        // Sign the transaction
        val stx = serviceHub.signInitialTransaction(txBuilder)
        val fullySignedTx = subFlow(CollectSignaturesFlow(stx, setOf(session)))
        return subFlow(FinalityFlow(fullySignedTx, listOf(session)))
    }
}

@InitiatedBy(InsuranceClaim::class)
class InsuranceClaimResponder(val counterpartySession: FlowSession) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {

        val (claimInfo, networkId, policyNumber) = counterpartySession.receive<Triple<ClaimInfo, String, String>>().unwrap { it -> it }
        businessNetworkFullVerificationClaim(networkId, ourIdentity, counterpartySession.counterparty)
        val service = serviceHub.cordaService(InsuranceService::class.java)
        val inputStateAndRef = service.findInsuranceByPolicyNumber(policyNumber)

        // Compose claim
        val claim = claimInfo.toClaim()
        val input = inputStateAndRef.state.data
        // Create the output
        val output = input.copy(claims = input.claims + claim)
        counterpartySession.send(output)

        subFlow(object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) {
            }
        })
        return subFlow(ReceiveFinalityFlow(counterpartySession))
    }
}