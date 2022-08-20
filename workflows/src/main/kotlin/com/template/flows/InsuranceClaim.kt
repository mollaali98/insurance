package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.InsuranceContract
import com.template.flows.businessmembership.BusinessNetworkIntegrationFlow
import com.template.schema.InsuranceSchemaV1
import com.template.states.ClaimInfo
import com.template.states.InsuranceState
import com.template.states.toClaim
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class InsuranceClaim(
        val claimInfo: ClaimInfo,
        val policyNumber: String
) : BusinessNetworkIntegrationFlow<SignedTransaction>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): SignedTransaction {

        /*************************************************************************************
         * This section is the custom query code. Instead of query out all the insurance state and filter by their policy number,
         * custom query will only retrieve the insurance state that matched the policy number. The filtering will happen behind the scene.
         * **/
        val policyNumbercriteria = QueryCriteria.VaultCustomQueryCriteria(builder {
            InsuranceSchemaV1.PersistentInsurance::policyNumber.equal(policyNumber, false)
        })

        /** And you can have joint custom criteria as well. Simply add additional criteria and add it to the criteria object by using and().
         * val insuredValuecriteria = VaultCustomQueryCriteria(builder { InsuranceSchemaV1.PersistentInsurance::insuredValue.equal(insuredValue, false) })
         * **/
        val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED).and(policyNumbercriteria)
        ///.and(insuredValuecriteria)
        val insuranceStateAndRefs = serviceHub.vaultService.queryBy(InsuranceState::class.java, criteria)
        /***************************************************************************************/

        if (insuranceStateAndRefs.states.isEmpty())
            throw IllegalArgumentException("Policy not found")

        val inputStateAndRef = insuranceStateAndRefs.states.first()

        // Compose claim
        val claim = claimInfo.toClaim()
        val input = inputStateAndRef.state.data

        businessNetworkFullVerification(input.networkId, ourIdentity, input.client)

        // Create the output
        val output = input.copy(claims = input.claims + claim)

        // Build the transaction.
        val txBuilder = TransactionBuilder(inputStateAndRef.state.notary)
                .addInputState(inputStateAndRef)
                .addOutputState(output)
                .addCommand(InsuranceContract.Commands.AddClaim(), listOf(ourIdentity.owningKey, input.client.owningKey))

        // Verify the transaction
        txBuilder.verify(serviceHub)

        // Sign the transaction
        val stx = serviceHub.signInitialTransaction(txBuilder)

        val counterpartySession = initiateFlow(input.client)
        val fullySignedTx = subFlow(CollectSignaturesFlow(stx, setOf(counterpartySession)))

        return subFlow(FinalityFlow(fullySignedTx, listOf(counterpartySession)))
    }
}

@InitiatedBy(InsuranceClaim::class)
class InsuranceClaimResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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