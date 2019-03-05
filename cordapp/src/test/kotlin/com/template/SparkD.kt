package com.template

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.utilities.NetworkHostAndPort
import spark.ModelAndView
import spark.Spark
import spark.template.freemarker.FreeMarkerEngine
import java.util.HashMap

object SparkD {

    @JvmStatic
    fun main( args: Array<String>) {
        val freeMarkerEngine = FreeMarkerEngine()
        val freeMarkerConfiguration = Configuration()
        freeMarkerConfiguration.setTemplateLoader(ClassTemplateLoader(SparkD::class.java, "/templates/"))
        freeMarkerEngine.setConfiguration(freeMarkerConfiguration)

        val nodeAddressD = NetworkHostAndPort.parse("localhost:10017")
        val rpcConnectionD = CordaRPCClient(nodeAddressD).start("user1", "test")
        val cordaRPCOpsD = rpcConnectionD.proxy

        Spark.port(1234)

        Spark.get("/"
        ) { req, _ ->


            val vaultData = cordaRPCOpsD.vaultQuery(StateContract.IOUState::class.java)

            val model = HashMap<String, Any>()

            val statusArray = vaultData.states.map { it -> it.state.data.status }

            model["status"] = statusArray

            freeMarkerEngine.render(ModelAndView(model, "sparkD.ftl"))
        }
    }
}