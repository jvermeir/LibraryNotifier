package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ActorTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  feature("The Actor loads data from a library in the background and returns a list of books based on a file until the real data is available") {
    info("As a family member")
    info("I want to see as many books a possible as soon as possible while the data is refreshed in the background")
    info("So that I can use the app right away")

    ignore ("The app starts and shows cached data") {
      Given("The data stored in 'boeken.dat'")
      When("The app starts")
      Then("The stored data is retured immediately")
    }

    ignore ("The app refreshes data in the background") {
      Given("The data stored in 'boeken.dat'")
      When("The app starts")
      Then("The stored data is refreshed in the background")
    }

    ignore ("Updated data is shown instead of cached data a soon as it becomes available") {
      Given("The data stored in 'boeken.dat' and new books from the library are loaded")
      When("The rest url http://[host]:[port]/boeken is accessed")
      Then("The updated list of books is shown")
    }

    ignore ("Actors are restarted wheb an error occurs") {
      Given("A running web service")
      When("The service crashes")
      Then("It is automatically restarted and service is resumed")
    }

  }
}
