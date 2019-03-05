package com.template

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class IOUContract : Contract {
    companion object {
        const val ID = "com.template.IOUContract"
    }

    // Our Create command.
    class Create : CommandData
    @Suspendable
    override fun verify(tx: LedgerTransaction) {
        //val command = tx.commands.requireSingleCommand<Create>()

        requireThat {
        }
    }
}

// *********
// * State *
// *********
object StateContract {
    @Suspendable
    @CordaSerializable
    class IOUState(val status: String,
                   val lender: Party,
                   val borrow: Party) : ContractState {
        override val participants get() = listOf(lender, borrow)
    }
}
