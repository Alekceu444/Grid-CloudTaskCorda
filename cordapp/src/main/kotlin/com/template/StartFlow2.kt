package com.template

import com.template.ExampleFlow.WindFlow
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val nodeAddressC = NetworkHostAndPort.parse("localhost:10013")
    val rpcConnectionC = CordaRPCClient(nodeAddressC).start("user1", "test")
    val proxyC = rpcConnectionC.proxy
    var counter =0
    while(true) {

        var url = arrayListOf<String>("https://www.metaweather.com/api/location/2123260/", //Spb
                "https://www.metaweather.com/api/location/1118370/", // Tokyo
                "https://www.metaweather.com/api/location/924938/", //Kiev
                "https://www.metaweather.com/api/location/2122265/", //Moscow
                "https://www.metaweather.com/api/location/753692/") //Barcelona

                proxyC.startFlow(::WindFlow,url[counter])
                        .returnValue.getOrThrow()

        counter=(counter+1)%5
        TimeUnit.SECONDS.sleep(5)
    }
}