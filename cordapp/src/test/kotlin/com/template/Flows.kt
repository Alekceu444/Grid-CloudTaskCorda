package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// *********
// * Flows *
// *********
object ExampleFlow {
    @InitiatingFlow
    @StartableByRPC
    class IOUFlow(val res:String) : FlowLogic<SignedTransaction>() {

        /** The flow logic is encapsulated within the call() method. */
        @Suspendable
        override fun call(): SignedTransaction {


            var wind_speed = res.toString().substringAfter("\"wind_speed\":")
            wind_speed = wind_speed.substringBefore(',')
            var time = res.toString().substringAfter("\"time\":\"")
            time = time.substringBefore('.')
            var city = res.toString().substringBeforeLast("\",\"location_type\"")
            city = city.substringAfterLast("\"")

            val otherParty: Party
            val res:String
            if (wind_speed.toDouble() > 8) {
                val name = CordaX500Name("PartyB", "New York", "US")

                otherParty = serviceHub.networkMapCache.allNodes.filter { nodeInfo -> nodeInfo.legalIdentitiesAndCerts[0].party.name == name }[0]
                        .legalIdentitiesAndCerts[0].party
                res = "In "+city+" are windy at "+time+
                        ". Speed = "+wind_speed+" m/ph"
            } else {
                val name = CordaX500Name("PartyD", "New York", "US")

                otherParty = serviceHub.networkMapCache.allNodes.filter { nodeInfo -> nodeInfo.legalIdentitiesAndCerts[0].party.name == name }[0]
                        .legalIdentitiesAndCerts[0].party
                res = "In "+city+" aren't windy at "+time+
                        ". Speed = "+wind_speed+" m/ph"
            }
            // We retrieve the notary identity from the network map.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            // We create the transaction components.
            val outputState = StateContract.IOUState(res, serviceHub.myInfo.legalIdentities.first(), otherParty)
            val command = Command(IOUContract.Create(),outputState.participants.map { it.owningKey })

            // We create a transaction builder and add the components.
            var txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(outputState, IOUContract.ID)
                    .addCommand(command)

            // Verifying the transaction.
            txBuilder.verify(serviceHub)

            // Signing the transaction.
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            // Creating a session with the other party.
            val otherPartySession = initiateFlow(otherParty)

            // Obtaining the counterparty's signature.
            val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, setOf(otherPartySession)))

            // Finalising the transaction.
            return subFlow(FinalityFlow(fullySignedTx))


        }
    }

    @InitiatedBy(ExampleFlow.IOUFlow::class)
    class IOUFlowResponder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {

                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}