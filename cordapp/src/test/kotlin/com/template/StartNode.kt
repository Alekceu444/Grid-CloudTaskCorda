package com.template

import com.template.ExampleFlow.IOUFlow
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val nodeAddressA = NetworkHostAndPort.parse("localhost:10005")
    val rpcConnectionA = CordaRPCClient(nodeAddressA).start("user1", "test")
    val proxyA = rpcConnectionA.proxy

    while(true) {
        val url = "https://www.metaweather.com/api/location/44418/"
        val obj = URL(url)
        var res = StringBuilder()
        val connection = obj.openConnection() as HttpURLConnection
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
        } finally {
            connection.disconnect()
        }

        proxyA.startFlow(::IOUFlow, res.toString())
                .returnValue.getOrThrow()

        TimeUnit.SECONDS.sleep(1000)
    }
}