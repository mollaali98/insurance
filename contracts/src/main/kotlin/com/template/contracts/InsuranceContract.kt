package com.template.contracts

import com.template.states.ClaimStatus
import com.template.states.ClientIdentity
import com.template.states.InsuranceState
import com.template.states.InsurerIdentity
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
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
            is Commands.IssueInsurance -> verifyIssue(tx, command)
            is Commands.AddClaim -> verifyAddClaim(tx, command)
            is Commands.UpdateClaim -> verifyUpdateClaim(tx, (command.value as Commands.UpdateClaim).claimNumber, command)
        }
    }

    private fun verifyUpdateClaim(tx: LedgerTransaction, claimNumber: String, command: CommandWithParties<Commands>) {
        tx.verifyMembershipsForMedInsuranceTransaction("UpdateClaim")
        val input = tx.requireSingleInputOfType<InsuranceState>()
        val output = tx.requireSingleOutputOfType<InsuranceState>()
        requireThat {
            "The networkId should be same".using(input.networkId == output.networkId)
            "The owner should be same" using (input.owner == output.owner)
            "The policyNumber should be same" using (input.policyNumber == output.policyNumber)
            "The insuredValue should be same" using (input.insuredValue == output.insuredValue)
            "The policyType should be same" using (input.policyType == output.policyType)
            "The startDate should be same".using(input.startDate == output.startDate)
            "The endDate should be same".using(input.endDate == output.endDate)
            (input.claims + output.claims).groupBy { it.claimNumber }.forEach { (id, claims) ->
                val first = claims[0]
                if (first.claimStatus != ClaimStatus.Waiting) {
                    val second = claims[1]
                    if (claimNumber == id) {
                        "The status should be different".using(first.claimStatus != second.claimStatus)
                    } else {
                        "The claimAmount should be same".using(first.claimAmount == second.claimAmount)
                        "The claimDescription should be same".using(first.claimDescription == second.claimDescription)
                    }
                }
            }
            "All of the participants must be signers." using (command.signers.containsAll(output.participants.map { it.owningKey }))
        }
    }

    private fun verifyIssue(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        tx.verifyMembershipsForMedInsuranceTransaction("Issue")
        tx.requireZeroInputsOfType<InsuranceState>()
        val output = tx.requireSingleOutputOfType<InsuranceState>()
        requireThat {
            "The startDate and endDate should be different".using(output.startDate != output.endDate)
            "The insuredValue should be bigger than zero".using(output.insuredValue > 0)
            "The startDate and endDate should be different".using(output.startDate != output.endDate)
            "The claims should be empty".using(output.claims.isEmpty())
        }
    }

    private fun verifyAddClaim(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        tx.verifyMembershipsForMedInsuranceTransaction("AddClaim")
        val input = tx.requireSingleInputOfType<InsuranceState>()
        val output = tx.requireSingleOutputOfType<InsuranceState>()
        requireThat {
            "The networkId should be same".using(input.networkId == output.networkId)
            "The owner should be same" using (input.owner == output.owner)
            "The policyNumber should be same" using (input.policyNumber == output.policyNumber)
            "The insuredValue should be same" using (input.insuredValue == output.insuredValue)
            "The policyType should be same" using (input.policyType == output.policyType)
            "The startDate should be same".using(input.startDate == output.startDate)
            "The endDate should be same".using(input.endDate == output.endDate)
            "Claims should be increased in the output".using(input.claims.size < output.claims.size)
            "All of the participants must be signers." using (command.signers.containsAll(output.participants.map { it.owningKey }))
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class IssueInsurance : Commands
        class AddClaim : Commands
        class UpdateClaim(val claimNumber: String) : Commands
    }
}

/**
 * Contract verification check over reference [MembershipState]s.
 * Make sure the participants are in the correct [Network], and has the correct [CustomIdentityType]
 *
 */
private fun LedgerTransaction.verifyMembershipsForMedInsuranceTransaction(commandName: String) = requireThat {
    val outputState = requireSingleOutputOfType<InsuranceState>()
    val networkId: String = outputState.networkId
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

inline fun <reified T : ContractState> LedgerTransaction.requireZeroInputsOfType() =
        require(inputsOfType<T>().isEmpty()) { "No ${T::class.simpleName} state should be consumed" }

inline fun <reified T : ContractState> LedgerTransaction.requireSingleInputOfType() =
        requireNotNull(inputsOfType<T>().singleOrNull()) { "Single ${T::class.simpleName} input state is required" }

inline fun <reified T : ContractState> LedgerTransaction.requireSingleOutputOfType() =
        requireNotNull(outputsOfType<T>().singleOrNull()) { "Single ${T::class.simpleName} output state is required" }

inline fun <reified T : ContractState> LedgerTransaction.requireSingleReferenceOfType() =
        requireNotNull(referenceInputsOfType<T>().singleOrNull()) { "Single ${T::class.simpleName} reference state is required" }

inline fun <reified T : ContractState> LedgerTransaction.requireReferencesOfType(): List<T> =
        referenceInputsOfType<T>().also { refs ->
            require(refs.isNotEmpty()) { "Reference inputs of type ${T::class.simpleName} are required" }
        }

inline fun <reified T : ContractState> LedgerTransaction.requireZeroReferencesOfType(): List<T> =
        referenceInputsOfType<T>().also { refs ->
            require(refs.isEmpty()) { "Reference inputs of type ${T::class.simpleName} should not be provided" }
        }