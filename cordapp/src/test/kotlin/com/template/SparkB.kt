package com.template

import spark.Spark
import spark.Spark.*
import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import spark.ModelAndView
import spark.template.freemarker.FreeMarkerEngine
import java.util.HashMap

object SparkB {

    @JvmStatic
    fun main( args: Array<String>  ) {
        val freeMarkerEngine = FreeMarkerEngine()
        val freeMarkerConfiguration = Configuration()
        freeMarkerConfiguration.setTemplateLoader(ClassTemplateLoader(SparkB::class.java, "/templates/"))
        freeMarkerEngine.setConfiguration(freeMarkerConfiguration)

        val nodeAddressB = NetworkHostAndPort.parse("localhost:10009")
        val rpcConnectionB = CordaRPCClient(nodeAddressB).start("user1", "test")
        val cordaRPCOps = rpcConnectionB.proxy

        Spark.port(2345)

        Spark.get("/"
        ) { req, _ ->

            val vaultData = cordaRPCOps.vaultQuery(StateContract.IOUState::class.java)

            val model = HashMap<String, Any>()

            val statusArray = vaultData.states.map{it -> it.state.data.status}

            model["status"] = statusArray

            freeMarkerEngine.render(ModelAndView(model, "sparkB.ftl"))
        }

    }
}