package com.template.schema

import com.template.states.PolicyType
import com.template.states.PropertyType
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDate
import java.util.*
import javax.persistence.*

/**
 * The family of schemas for IOUState.
 */
object InsuranceSchema

/**
 * An IOUState schema.
 */
object InsuranceSchemaV1 : MappedSchema(
        schemaFamily = InsuranceSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentClaim::class.java, PersistentInsurance::class.java, PersistentProperty::class.java)) {

    override val migrationResource: String?
        get() = "insurance.changelog-master";

    @Entity
    @Table(name = "CLAIM_DETAIL")
    class PersistentClaim(
            @Id @Column(name = "Id")
            @Type(type = "uuid-char")
            val uuid: UUID,

            @Column(name = "claimNumber")
            var claimNumber: String,

            @Column(name = "claimDescription")
            var claimDescription: String,

            @Column(name = "claimAmount")
            var claimAmount: Int
    ) {
        // Default constructor required by hibernate.
        constructor(claimNumber: String, claimDescription: String, claimAmount: Int) : this(
                UUID.randomUUID(), claimNumber, claimDescription, claimAmount
        )

        constructor() : this(UUID.randomUUID(), "", "", 0)
    }

    @Entity
    @Table(name = "PROPERTY")
    class PersistentProperty(
            @Id @Column(name = "Id")
            @Type(type = "uuid-char")
            val uuid: UUID,

            @Column(name = "address")
            val address: String,

            @Column(name = "propertyType")
            val propertyType: PropertyType,

            @Column(name = "area")
            val area: Double,

            @Column(name = "location")
            val location: String

    ) : Serializable {
        // Default constructor required by hibernate.
        constructor(address: String, propertyType: PropertyType, area: Double, location: String) : this(UUID.randomUUID(), address, propertyType, area, location)
        constructor() : this(UUID.randomUUID(), "", PropertyType.House, 0.0, "")
    }


    @Entity
    @Table(name = "INSURANCE_DETAIL")
    class PersistentInsurance(

            @Column(name = "networkId")
            val networkId: String,

            @Column(name = "owner")
            val owner: String,

            @Column(name = "policyNumber")
            val policyNumber: String,

            @Column(name = "insuredValue")
            val insuredValue: Double,

            @Column(name = "policyType")
            val policyType: PolicyType,

            @Column(name = "startDate")
            val startDate: LocalDate,

            @Column(name = "endDate")
            val endDate: LocalDate,

            @OneToOne(cascade = [CascadeType.PERSIST])
            @JoinColumns(JoinColumn(name = "id", referencedColumnName = "id"), JoinColumn(name = "address", referencedColumnName = "address"))
            val property: PersistentProperty?,

            @OneToMany(cascade = [CascadeType.PERSIST])
            @JoinColumns(JoinColumn(name = "output_index", referencedColumnName = "output_index"), JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"))
            val claims: List<PersistentClaim>
    ) : PersistentState(), Serializable {
        constructor() : this("",
                "",
                "",
                0.0,
                PolicyType.Gold,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                PersistentProperty(),
                listOf(PersistentClaim()))
    }
}
