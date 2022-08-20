package com.template.contracts

import com.template.states.ClientIdentity
import com.template.states.InsuranceState
import com.template.states.InsurerIdentity
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party

// ************
// * Contract *
// ************
class InsuranceContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.InsuranceContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.IssueInsurance -> verifyIssue(tx)
            is Commands.AddClaim -> verifyAddClaim(tx)
        }
    }

    private fun verifyIssue(tx: LedgerTransaction) {
        tx.verifyMembershipsForMedInsuranceTransaction("Issue")
        val inputs = tx.inputs
        requireThat {
            "Transaction must have no input states." using (inputs.isEmpty())
        }
    }

    private fun verifyAddClaim(tx: LedgerTransaction) {
        tx.verifyMembershipsForMedInsuranceTransaction("AddClaim")
        val inputs = tx.inputs
        val outputs = tx.outputs
        requireThat {
            "Transaction must have one input states." using (inputs.isNotEmpty())
            "Transaction must have one output states." using (outputs.isNotEmpty())
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class IssueInsurance : Commands
        class AddClaim : Commands
    }
}

/**
 * Contract verification check over reference [MembershipState]s.
 * Make sure the participants are in the correct [Network], and has the correct [CustomIdentityType]
 *
 */
private fun LedgerTransaction.verifyMembershipsForMedInsuranceTransaction(
        commandName: String
) = requireThat {

    val output = if (outputStates.isNotEmpty()) outputs.single() else null
    val outputState = output?.data as? InsuranceState


    val networkId: String = outputState!!.networkId
    val insurance: Party = outputState.insurance
    val client: Party = outputState.client

    //Verify number of memberships
    "Insurance $commandName transaction should have 2 reference states" using (referenceStates.size == 2)
    "Insurance $commandName transaction should contain only reference MembershipStates" using (referenceStates.all { it is MembershipState })

    //Extract memberships
    val membershipReferenceStates = referenceStates.map { it as MembershipState }

    //Check for membership network IDs.
    "Insurance $commandName transaction should contain only reference membership states from Business Network with $networkId ID" using (membershipReferenceStates.all { it.networkId == networkId })

    //Extract Membership and verify not null
    val insuranceMembership = membershipReferenceStates.find { it.networkId == networkId && it.identity.cordaIdentity == insurance }
    val clientMembership = membershipReferenceStates.find { it.networkId == networkId && it.identity.cordaIdentity == client }
    "Insurance $commandName transaction should have insurance's reference membership state" using (insuranceMembership != null)
    "Insurance $commandName transaction should have client's reference membership state" using (clientMembership != null)

    //Exam the customized Identity
    insuranceMembership?.apply {
        "insurance should be active member of Business Network with $networkId" using (isActive())
        "insurance should have business identity of FirmIdentity type" using (identity.businessIdentity is InsurerIdentity)
    }
    clientMembership?.apply {
        "client should be active member of Business Network with $networkId" using (isActive())
        "client should have business identity of FirmIdentity type" using (identity.businessIdentity is ClientIdentity)
    }
}