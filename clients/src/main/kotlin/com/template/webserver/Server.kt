package com.template.webserver

import com.template.states.FirmType
import com.template.webserver.model.ERole
import com.template.webserver.model.User
import com.template.webserver.repository.RoleRepository
import com.template.webserver.repository.UserRepository
import com.template.webserver.service.BusinessNetworkService
import net.corda.client.rpc.RPCException
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.buildIterator

/**
 * Our Spring Boot application.
 */

@SpringBootApplication
@EntityScan("com.template.webserver.model")
@EnableJpaRepositories("com.template.webserver.repository")
private open class Starter


@Component
class Init(
        private val businessNetworkService: BusinessNetworkService,
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository,
        private val encoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        while (true) {
            try {
                println("Here we are")
                val id = businessNetworkService.createNetwork()
                val networkParty = businessNetworkService.getNetworkOperatorParty()
                businessNetworkService.requestMembershipInsurance(networkParty, id)
                businessNetworkService.requestMembershipClient(networkParty, id)
                val members = businessNetworkService.queryAllMembers()
                members.forEach { (name, idMember) ->
                    if (name != "NetworkOperator")
                        businessNetworkService.activeMembers(idMember)
                }
                businessNetworkService.createNetworkSubGroup(
                        networkId = id,
                        groupName = "APAC_Insurance_Alliance",
                        groupParticipants = members.map { it.second }.toSet()
                )
                val insuranceFirm = members.single { it.first == "Insurance" }
                val clientFirm = members.single { it.first == "Client" }
                businessNetworkService.assignBNIdentity(
                        firmType = FirmType.InsuranceFirm,
                        membershipId = insuranceFirm.second,
                        bnIdentity = "APACIN76CZX"
                )
                businessNetworkService.assignBNIdentity(
                        firmType = FirmType.ClientFirm,
                        membershipId = clientFirm.second,
                        bnIdentity = "APACCP44OJS"
                )

                businessNetworkService.assignPolicyIssuerRole(
                        membershipId = insuranceFirm.second,
                        networkId = id
                )
                businessNetworkService.assignPolicyIReceiverRole(
                        membershipId = clientFirm.second,
                        networkId = id
                )

                businessNetworkService.getAllMembershipState().forEach {
                    println("businessIdentity = > ${it.identity.businessIdentity.toString()}")
                    println("organisation => ${it.identity.cordaIdentity.name.organisation}")
                    println("roles => ${it.roles}")
                }

                val networkOperator = User(
                        username = "operator",
                        email = "operator@gamil.com",
                        password = encoder.encode("password"),
                        networkId = id,
                        roles = mutableSetOf(
                                roleRepository.findByName(ERole.ROLE_NETWORK_OPERATOR)
                                        .orElseThrow { error("Error: Role is not found.") }
                        )
                )

                val client = User(
                        username = "client",
                        email = "client@gmail.com",
                        password = encoder.encode("password"),
                        networkId = id,
                        roles = mutableSetOf(
                                roleRepository.findByName(ERole.ROLE_CLIENT)
                                        .orElseThrow { error("Error: Role is not found.") }
                        )
                )

                val insurance = User(
                        username = "insurance",
                        email = "insurance@gmil.com",
                        password = encoder.encode("password"),
                        networkId = id,
                        roles = mutableSetOf(
                                roleRepository.findByName(ERole.ROLE_INSURANCE)
                                        .orElseThrow { error("Error: Role is not found.") }
                        )
                )
                userRepository.save(networkOperator)
                userRepository.save(client)
                userRepository.save(insurance)
            } catch (e: RPCException) {
                println("Try again.....")
                continue
            }
            break

        }
    }

}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}
