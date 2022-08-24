package com.template.states

import com.template.contracts.InsuranceContract
import com.template.schema.InsuranceSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDate
import java.util.*

@BelongsToContract(InsuranceContract::class)
data class InsuranceState(
        val networkId: String,
        val owner: String,
        val policyNumber: String,
        val insuredValue: Double,
        val policyType: PolicyType,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val insurance: Party,
        val client: Party,
        val propertyData: PropertyData,
        val claims: List<Claim> = listOf(),
        override val participants: List<AbstractParty> = listOf(client, insurance)
) : QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is InsuranceSchemaV1) {
            val persistentClaims: MutableList<InsuranceSchemaV1.PersistentClaim> = mutableListOf()
            if (claims.isNotEmpty()) {
                for (item in claims) {
                    persistentClaims += (InsuranceSchemaV1.PersistentClaim(
                            item.claimNumber,
                            item.claimDescription,
                            item.claimAmount,
                            item.claimStatus
                    ))
                }
            }

            val property = InsuranceSchemaV1.PersistentProperty(
                    propertyData.address,
                    propertyData.propertyType,
                    propertyData.area,
                    propertyData.location
            )

            return InsuranceSchemaV1.PersistentInsurance(
                    networkId,
                    owner,
                    policyNumber,
                    insuredValue,
                    policyType,
                    startDate,
                    endDate,
                    property,
                    persistentClaims
            )
        } else
            throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(InsuranceSchemaV1)
}


@CordaSerializable
data class Claim(
        val claimNumber: String,
        val claimDescription: String,
        val claimAmount: Double,
        val claimStatus: ClaimStatus = ClaimStatus.Waiting
)

@CordaSerializable
data class PropertyData(
        val address: String,
        val propertyType: PropertyType,
        val area: Double,
        val location: String
)

@CordaSerializable
enum class PropertyType {
    Apartment, House, Villa
}

@CordaSerializable
enum class PolicyType {
    Silver, Gold, Platinum
}

@CordaSerializable
enum class ClaimStatus {
    Approved, Refuse, Waiting
}

// DTOs
@CordaSerializable
data class InsuranceInfo(
        val owner: String? = null,
        val networkId: String? = null,
        val policyNumber: String = UUID.randomUUID().toString(),
        val insuredValue: Double,
        val policyType: PolicyType,
        val startDate: LocalDate,
        val endDate: LocalDate = startDate.plusYears(1),
        val propertyInfo: PropertyInfo
) {
    constructor() : this(null,
            null,
            "",
            0.0,
            PolicyType.Gold,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            PropertyInfo())
}


@CordaSerializable
data class PropertyInfo(
        val address: String,
        val propertyType: PropertyType,
        val area: Double,
        val location: String
) {
    constructor() : this("", PropertyType.House, 0.0, "")
}

@CordaSerializable
data class ClaimInfo(
        val claimNumber: String = UUID.randomUUID().toString(),
        val claimDescription: String,
        val claimAmount: Double,
        val claimStatus: ClaimStatus = ClaimStatus.Waiting
) {
    constructor() : this("", "", 0.0, ClaimStatus.Waiting)
}

@CordaSerializable
data class ClaimUpdate(
        val claimNumber: String,
        val policyNumber: String,
        val claimStatus: ClaimStatus
) {
    constructor() : this("", "", ClaimStatus.Waiting)
}

fun PropertyInfo.toPropertyData() = PropertyData(address, propertyType, area, location)

fun PropertyData.toPropertyInfo() = PropertyInfo(address, propertyType, area, location)

fun InsuranceState.toInsuranceInfo() = InsuranceInfo(
        owner,
        networkId,
        policyNumber,
        insuredValue,
        policyType,
        startDate,
        endDate,
        propertyData.toPropertyInfo()
)

fun List<StateAndRef<InsuranceState>>.toInsuranceInfo() = map { it.state.data.toInsuranceInfo() }

fun ClaimInfo.toClaim() = Claim(claimNumber, claimDescription, claimAmount, claimStatus)

fun Claim.toClaimInfo() = ClaimInfo(claimNumber, claimDescription, claimAmount, claimStatus)

@JvmName("toClaimInfoClaim")
fun List<Claim>.toClaimInfo() = map { it.toClaimInfo() }

fun List<StateAndRef<InsuranceState>>.toClaimInfo() = flatMap { it.state.data.claims.toClaimInfo() }

fun InsuranceInfo.addUsers(owner: String) = copy(owner = owner)

fun InsuranceInfo.toInsuranceState(networkId: String, insurance: Party, client: Party, propertyData: PropertyData) = InsuranceState(
        networkId = networkId,
        owner = owner!!,
        policyNumber = policyNumber,
        insuredValue = insuredValue,
        policyType = policyType,
        startDate = startDate,
        endDate = endDate,
        insurance = insurance,
        client = client,
        propertyData = propertyData
)