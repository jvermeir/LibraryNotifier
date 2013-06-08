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
      val url = "http://localhost/"
      When("the page is loaded from the url")
      val data:String = LibraryClient.readTextFromUrl(url);
      Then("the text 'It works' appears in the page data")
      val itWorksFound = data.contains("It works")
      true must be === itWorksFound
    }

    scenario("Reading data from http://bicat.cultura-ede.nl/") {
      Given("A url")
      val url = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
      When("the page is loaded from the url")
      val data = LibraryClient.readTextFromUrl(url)
      Then("the text 'Hoofdmenu' appears in the page data")
      val hoofdmenuFound = data.contains("Hoofdmenu")
      true must be === hoofdmenuFound
    }

    scenario("getting bicat_sid cookie from http://bicat.cultura-ede.nl/") {
      Given("A LibraryClient")
      When("the start page is loaded from the url")
      LibraryClient.startBicatSession
      Then("the BICAT_SID cookie is set to a 5-part string")
      val bicatCookie = LibraryClient.getBicatCookie
      5 must be ===bicatCookie.getValue.split("-").size
    }

    scenario("get the list of writers that satisfy a author search criterium") {
      Given("A LibraryClient")
      LibraryClient.startBicatSession
      When("we search for 'Dan Brown")
      val data:String = LibraryClient.getContentsOfSearchResult("Brown, Dan")
      Then("we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      data must include regex ("Brown, Dan.*(1964-)")
    }

    scenario("Getting the first writer in a list of writers based on a Author instance") {
      Given("A Author")
      val author = Author ("dan", "brown")
      When("we search for the author on bicat.cultura-ede.nl")
      val data:String = LibraryClient.getListOfAuthorUrlsBasedOnAAuthorInstance(author)
      Then("a link to Dan Brown is returned")
      val hoofdmenuFound = data.contains("Hoofdmenu")
      true must be === hoofdmenuFound
    }
  }
}
