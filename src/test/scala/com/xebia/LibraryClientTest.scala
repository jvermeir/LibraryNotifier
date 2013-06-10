package com.xebia

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import scala.io.Source._

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
      val data: String = LibraryClient.readTextFromUrl(url);
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
      5 must be === bicatCookie.getValue.split("-").size
    }

    scenario("get the page with the list of writers that satisfy a author search criterium") {
      Given("A LibraryClient")
      LibraryClient.startBicatSession
      When("we search for 'Dan Brown")
      val data: String = LibraryClient.getContentsOfSearchResult("Brown, Dan")
      Then("we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      data must include regex ("Brown, Dan.*(1964-)")
    }

    scenario("get the list of writers that satisfy a author search criterium") {
      Given("A LibraryClient")
      LibraryClient.startBicatSession
      When("we search for 'Dan Brown' we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      val data: String = LibraryClient.getContentsOfSearchResult("Brown, Dan")
      Then("we get the list of authors from that page and the first author in the list is 'Brown, Dan.*(1964-)'")
      val author = LibraryClient.getAuthorFromAWebPage(data)
      author.equalsIgnoreLink(Author("Dan", "Brown")) must be === true
    }

    scenario("get the page with the list of books for an author") {
      Given("A LibraryClient")
      LibraryClient.startBicatSession
      val data: String = LibraryClient.getContentsOfSearchResult("Brown, Dan")
      val danBrown = LibraryClient.getAuthorFromAWebPage(data)
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml:String = LibraryClient.getBookPageAsHtmlByAuthor(danBrown)
      bookPageAsHtml must include ("The Da Vinci code")
    }

    scenario("get the list of books from a page for an author") {
      Given("A html page with the list of books for Dan Brown")
      val bookPageAsHtml: String = fromFile("data/danBrownBooks.html").mkString
      When("we get the list of books")
      val listOfBooks:List[String] = LibraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length 3")
      listOfBooks must contain ("The Da Vinci code")
      listOfBooks.size must be === 3
    }

    scenario("get the list of books from the Internet for an author") {
      Given("A LibraryClient")
      LibraryClient.startBicatSession
      val data: String = LibraryClient.getContentsOfSearchResult("Brown, Dan")
      val danBrown = LibraryClient.getAuthorFromAWebPage(data)
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml:String = LibraryClient.getBookPageAsHtmlByAuthor(danBrown)
      val listOfBooks:List[String] = LibraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length of at least 15")
      listOfBooks must contain ("The Da Vinci code")
      listOfBooks.size must be > 15
    }

    // Don't really need this stuff...
    scenario("replace sid in a link retrieved from the web page") {
      Given("given a link to the page of an author")
      val link = """"/cgi-bin/bx.pl?wzstype=;zl_v=N;woord=Brown%2C%20Dan;vestfiltgrp=;dcat=1;nieuw=;extsdef=01;event=titelset;qs=brown%2C%20dan;wzsrc=;recent=N;rubplus=TS0;recno=20543796;sid=ae42610b-a6ca-4ce1-89a5-7bb303db17bd;groepfx=10;vestnr=8399;prt=INTERNET;cdef=002;taal=1;sn=48;var=portal;aantal=10"""
      When("we replace the sid")
      Then("we get a new Author with the correct sid in its link field")
      val authorParsedFromAWebPage = Author("X", "Y", "/cgi-bin/bx.pl?wzstype=;zl_v=N;woord=Brown%2C%20Dan;vestfiltgrp=;dcat=1;nieuw=;extsdef=01;event=titelset;qs=brown%2C%20dan;wzsrc=;recent=N;rubplus=TS0;recno=20543796;sid=ae42610b-a6ca-4ce1-89a5-7bb303db17bd;groepfx=10;vestnr=8399;prt=INTERNET;cdef=002;taal=1;sn=48;var=portal;aantal=10")
      val authorWithsidReplaced = Author.replaceSid(authorParsedFromAWebPage, "newid")
      val expectedAuthor = Author("X", "Y", "/cgi-bin/bx.pl?wzstype=;zl_v=N;woord=Brown%2C%20Dan;vestfiltgrp=;dcat=1;nieuw=;extsdef=01;event=titelset;qs=brown%2C%20dan;wzsrc=;recent=N;rubplus=TS0;recno=20543796;sid=newid;groepfx=10;vestnr=8399;prt=INTERNET;cdef=002;taal=1;sn=48;var=portal;aantal=10")
      expectedAuthor must be === authorWithsidReplaced
    }

  }

}
