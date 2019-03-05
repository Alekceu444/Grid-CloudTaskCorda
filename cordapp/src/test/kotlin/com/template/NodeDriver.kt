package com.template

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.client.rpc.CordaRPCClient
import net.corda.testing.node.User

fun main(args: Array<String>) {
    val rpcUsers = listOf(User("user1", "test", permissions = setOf("ALL")))

    driver(DriverParameters(startNodesInProcess = true, waitForAllNodesToFinish = true, isDebug=true,
            extraCordappPackagesToScan = listOf("net.template.WindContract"))
    )  {
        //node for requests
        startNode(providedName = CordaX500Name("PartyA", "London", "GB"), rpcUsers = rpcUsers).getOrThrow()
        //node for windy cities
        startNode(providedName = CordaX500Name("PartyB", "New York", "US"), rpcUsers = rpcUsers).getOrThrow()
        //node for requests
        startNode(providedName = CordaX500Name("PartyC", "London", "GB"), rpcUsers = rpcUsers).getOrThrow()
        //node for not windy cities
        startNode(providedName = CordaX500Name("PartyD", "New York", "US"), rpcUsers = rpcUsers).getOrThrow()
    }
}
