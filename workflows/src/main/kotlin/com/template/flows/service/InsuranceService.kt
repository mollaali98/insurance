package com.template.flows.service

import com.template.schema.InsuranceSchemaV1
import com.template.states.InsuranceState
import net.corda.core.contracts.StateAndRef
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.serialization.SingletonSerializeAsToken


@CordaService
class InsuranceService(private val serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    fun findInsuranceByPolicyNumber(policyNumber: String): StateAndRef<InsuranceState> {
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
        val insuranceStateAndRefs = serviceHub.vaultService.queryBy(InsuranceState::class.java, criteria)
        /***************************************************************************************/

        if (insuranceStateAndRefs.states.isEmpty())
            throw IllegalArgumentException("Policy not found")

        return insuranceStateAndRefs.states.singleOrNull()
                ?: throw IllegalArgumentException("Should have only one policy with the given number")
    }
}