package com.template.webserver

import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Wraps RPC connections to a set of Corda nodes.
 */
@Component
open class NodeRPCConnection(
        @Value("\${corda-rpc.networkOperator.host}") private val networkOperatorHost: String,
        @Value("\${corda-rpc.networkOperator.username}") private val networkOperatorUsername: String,
        @Value("\${corda-rpc.networkOperator.password}") private val networkOperatorPassword: String,
        @Value("\${corda-rpc.networkOperator.port}") private val networkOperatorRpcPort: Int,

        @Value("\${corda-rpc.insurance.host}") private val insuranceHost: String,
        @Value("\${corda-rpc.insurance.username}") private val insuranceUsername: String,
        @Value("\${corda-rpc.insurance.password}") private val insurancePassword: String,
        @Value("\${corda-rpc.insurance.port}") private val insuranceRpcPort: Int,

        @Value("\${corda-rpc.client.host}") private val clientHost: String,
        @Value("\${corda-rpc.client.username}") private val clientUsername: String,
        @Value("\${corda-rpc.client.password}") private val clientPassword: String,
        @Value("\${corda-rpc.client.port}") private val clientRpcPort: Int
) : AutoCloseable {

    private val logger: Logger = LoggerFactory.getLogger(NodeRPCConnection::class.java)

    lateinit var networkOperatorRpcConnection: CordaRPCConnection
    lateinit var networkOperatorProxy: CordaRPCOps

    lateinit var insuranceRpcConnection: CordaRPCConnection
    lateinit var insuranceProxy: CordaRPCOps

    lateinit var clientRpcConnection: CordaRPCConnection
    lateinit var clientProxy: CordaRPCOps

    @PostConstruct
    fun initialiseNodeRPCConnection() {

        logger.info("Connecting to insurance node on $insuranceHost:$insuranceRpcPort as $insuranceUsername")
        val rpcClientInsurance = CordaRPCClient(NetworkHostAndPort(insuranceHost, insuranceRpcPort))
        insuranceRpcConnection = rpcClientInsurance.start(insuranceUsername, insurancePassword)
        insuranceProxy = insuranceRpcConnection.proxy

        logger.info("Connecting to networkOperator node on $networkOperatorHost:$networkOperatorRpcPort as $networkOperatorUsername")
        val rpcClientNetworkOperator = CordaRPCClient(NetworkHostAndPort(networkOperatorHost, networkOperatorRpcPort))
        networkOperatorRpcConnection = rpcClientNetworkOperator.start(networkOperatorUsername, networkOperatorPassword)
        networkOperatorProxy = networkOperatorRpcConnection.proxy

        logger.info("Connecting to client node on $clientHost:$clientRpcPort as $clientUsername")
        val rpcClientClient = CordaRPCClient(NetworkHostAndPort(clientHost, clientRpcPort))
        clientRpcConnection = rpcClientClient.start(clientUsername, clientPassword)
        clientProxy = clientRpcConnection.proxy
    }

    @PreDestroy
    override fun close() {
        networkOperatorRpcConnection.notifyServerAndClose()
        clientRpcConnection.notifyServerAndClose()
    }
}