package com.template

import com.template.ExampleFlow.WindFlow
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.core.utilities.getOrThrow
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val nodeAddressA = NetworkHostAndPort.parse("localhost:10005")
    val rpcConnectionA = CordaRPCClient(nodeAddressA).start("user1", "test")
    val proxyA = rpcConnectionA.proxy
    var counter = 0
    while(true) {
        var url = arrayListOf<String>("https://www.metaweather.com/api/location/44418/", //London
        "https://www.metaweather.com/api/location/2514815/", //Washington DC
        "https://www.metaweather.com/api/location/116545/", //Mexico
        "https://www.metaweather.com/api/location/1591691/", //Cape Town
        "https://www.metaweather.com/api/location/1940345/") //Dubai


        proxyA.startFlow(::WindFlow, url[counter])
                .returnValue.getOrThrow()
        counter=(counter+1)%5
        TimeUnit.SECONDS.sleep(5)
    }
}