package com.template

import co.paralleluniverse.fibers.Suspendable
import com.template.WindContract.Companion.ID
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.net.URL

// *********
// * Flows *
// *********
object ExampleFlow {
    @InitiatingFlow
    @StartableByRPC
    class WindFlow(val url:String) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            var res = URL(url).readText()

            var wind_speed = res.substringAfter("\"wind_speed\":")
            wind_speed = wind_speed.substringBefore(',')
            var time = res.substringAfter("\"time\":\"")
            time = time.substringBefore('.')
            var city = res.substringBeforeLast("\",\"location_type\"")
            city = city.substringAfterLast("\"")

            val otherParty: Party
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
            val notary = serviceHub.networkMapCache.notaryIdentities[0]
            val outputState = StateContract.WindState(res, serviceHub.myInfo.legalIdentities.first(), otherParty)
            val command = Command(WindContract.Create(),outputState.participants.map { it.owningKey })

            var txBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(outputState, WindContract.ID)
                    .addCommand(command)

            txBuilder.verify(serviceHub)

            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            val otherPartySession = initiateFlow(otherParty)

            val fullySignedTx = subFlow(CollectSignaturesFlow(signedTx, setOf(otherPartySession)))

            return subFlow(FinalityFlow(fullySignedTx))


        }
    }

    @InitiatedBy(ExampleFlow.WindFlow::class)
    class WindFlowResponder(val otherPartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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