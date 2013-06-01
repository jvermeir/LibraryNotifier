package com.xebia

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class LibraryClientTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  feature("The HTTP client finds books written by a list of authors and reports on their availability") {
    info("As a family member")
    info("I want to be notified when a new book by one of my favourite authors becomes available at the library")
    info("So that I can go get it")

    scenario("Reading data from a URL") {
      Given("A url")
      val url = "http://bicat.cultura-ede.nl/"
      When("the page is loaded from the url")
      val data:String = LibraryClient.readTextFromUrl(url);
      Then("the text 'Hoofdmenu' appears in the page data")
      val hoofdmenuFound = data.contains("Hoofdmenu")
      true must be === hoofdmenuFound
    }
  }
}
