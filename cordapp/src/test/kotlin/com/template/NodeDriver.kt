package com.template

import com.template.ExampleFlow.IOUFlow
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Allows you to run your nodes through an IDE (as opposed to using deployNodes). Do not use in a production
 * environment.
 */
fun main(args: Array<String>) {
    val rpcUsers = listOf(User("user1", "test", permissions = setOf("ALL")))

    driver(DriverParameters(startNodesInProcess = true, waitForAllNodesToFinish = true, isDebug=true)) {
        startNode(providedName = CordaX500Name("PartyA", "London", "GB"), rpcUsers = rpcUsers).getOrThrow()
        startNode(providedName = CordaX500Name("PartyB", "New York", "US"), rpcUsers = rpcUsers).getOrThrow()
        startNode(providedName = CordaX500Name("PartyC", "London", "GB"), rpcUsers = rpcUsers).getOrThrow()
        startNode(providedName = CordaX500Name("PartyD", "New York", "US"), rpcUsers = rpcUsers).getOrThrow()

        val nodeAddressC = NetworkHostAndPort.parse("localhost:10013")
        val rpcConnectionC = CordaRPCClient(nodeAddressC).start("user1", "test")
        val proxyC = rpcConnectionC.proxy

        while(true) {

            val url = "https://www.metaweather.com/api/location/2123260/"
            val obj = URL(url)

            var res = StringBuilder()
            val connection =obj.openConnection() as HttpURLConnection
            try {
                // optional default is GET
                connection.requestMethod = "GET"


                println("\nSending 'GET' request to URL : $connection.url")
                println("Response Code : $connection.responseCode")

                connection.inputStream.bufferedReader().use {
                    var response = StringBuffer()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    res = StringBuilder(response.toString())
                }
            }finally {
                connection.disconnect()
            }

            proxyC.startFlow(::IOUFlow,res.toString())
                    .returnValue.getOrThrow()

            TimeUnit.SECONDS.sleep(1000)
        }
    }
}
