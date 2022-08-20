package com.template.states

import net.corda.core.serialization.CordaSerializable
import java.util.regex.Pattern
import net.corda.bn.states.BNIdentity
import net.corda.bn.states.BNPermission
import net.corda.bn.states.BNRole

/**
 * Custom Identity #1
 * Business identity specific for Insurer Companies. Uses mimicking Swift Business Identifier Code (BIC).
 *
 * @property bic Business Identifier Code of the bank.
 */
@CordaSerializable
data class InsurerIdentity(val insuranceIdentityCode: String) : BNIdentity {
    companion object {
        private const val iicRegex = "^[a-zA-Z]{6}[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?$"
    }

    /** Checks whether provided BIC is valid. **/
    fun isValid() = insuranceIdentityCode.matches(Pattern.compile(iicRegex).toRegex())
}

/**
 * Represents Policy Issuer role which has permission to issue Policy.
 */
@CordaSerializable
class PolicyIssuerRole : BNRole("PolicyIssuer", setOf(IssuePermissions.CAN_ISSUE_POLICY, IssuePermissions.CAN_ISSUE_CLAIM))

/**
 * PolicyIssuer related permissions which can be given to a role.
 */
@CordaSerializable
enum class IssuePermissions : BNPermission {

    /** Enables Business Network member to issue [InsuranceState]s. **/
    CAN_ISSUE_POLICY,

    /** Enables Business Network member to issue [Claim]s. **/
    CAN_ISSUE_CLAIM
}

/**
 * Custom Identity #2
 * Business identity specific for ClientIdentity. Uses mimicking Swift Business Identifier Code (BIC).
 *
 * @property bic Business Identifier Code of the bank.
 */
@CordaSerializable
data class ClientIdentity(val cic: String) : BNIdentity {
    companion object {
        private const val cicRegex = "^[a-zA-Z]{6}[0-9a-zA-Z]{2}([0-9a-zA-Z]{3})?$"
    }

    /** Checks whether provided BIC is valid. **/
    fun isValid() = cic.matches(Pattern.compile(cicRegex).toRegex())
}

/**
 * Represents Policy Receiver role which has permission to receive Policy.
 */
@CordaSerializable
class PolicyReceiverRole : BNRole("PolicyReceiver", setOf(ReceiverPermissions.CAN_RECEIVE_POLICY, ReceiverPermissions.CAN_RECEIVE_CLAIM))

/**
 * PolicyReceiver related permissions which can be given to a role.
 */
@CordaSerializable
enum class ReceiverPermissions : BNPermission {
    /** Enables Business Network member to receive [InsuranceState]s. **/
    CAN_RECEIVE_POLICY,

    /** Enables Business Network member to receive [Claim]s. **/
    CAN_RECEIVE_CLAIM
}

@CordaSerializable
enum class FirmType {
    InsuranceFirm,
    ClientFirm
}