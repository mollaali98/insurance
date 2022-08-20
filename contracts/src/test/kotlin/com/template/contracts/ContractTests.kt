//package com.template.contracts
//
//import net.corda.core.identity.CordaX500Name
//import net.corda.testing.core.TestIdentity
//import net.corda.testing.node.MockServices
//import net.corda.testing.node.ledger
//import org.junit.Test
//import com.template.states.InsuranceState
//
//class ContractTests {
//    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
//    var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
//    var bob = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
//
//    @Test
//    fun dummytest() {
//        val state = InsuranceState("Hello-World", alice.party, bob.party)
//        ledgerServices.ledger {
//            // Should fail bid price is equal to previous highest bid
//            transaction {
//                //failing transaction
//                input(InsuranceContract.ID, state)
//                output(InsuranceContract.ID, state)
//                command(alice.publicKey, InsuranceContract.Commands.Create())
//                fails()
//            }
//            //pass
//            transaction {
//                //passing transaction
//                output(InsuranceContract.ID, state)
//                command(alice.publicKey, InsuranceContract.Commands.Create())
//                verifies()
//            }
//        }
//    }
//}