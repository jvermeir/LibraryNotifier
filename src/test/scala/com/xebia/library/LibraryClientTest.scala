package com.xebia.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import scala.io.Source._

@RunWith(classOf[JUnitRunner])
class LibraryClientTest extends LibraryClient with FeatureSpec with GivenWhenThen with MustMatchers {

  feature("The HTTP client finds books written by a list of authors and reports on their availability") {
    info("As a family member")
    info("I want to be notified when a new book by one of my favourite authors becomes available at the library")
    info("So that I can go get it")
    val libraryClient = new LibraryClient

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
      Then("the BICAT_SID cookie is set to a 5-part string")
      val bicatCookie = libraryClient.bicatCookie
      5 must be === bicatCookie.getValue.split("-").size
    }

    scenario("get the page with the list of writers that satisfy a author search criterium") {
      Given("A LibraryClient")
      When("we search for 'Dan Brown")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      Then("we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      data must include regex ("Brown, Dan.*(1964-)")
    }

    scenario("get the list of writers that satisfy a author search criterium") {
      Given("A LibraryClient")
      When("we search for 'Dan Brown' we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      Then("we get the list of authors from that page and the first author in the list is 'Brown, Dan.*(1964-)'")
      val author = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      author.equalsIgnoreLink(Author("Dan", "Brown")) must be === true
    }

    scenario("get the page with the list of books for an author") {
      Given("A LibraryClient")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      val danBrown = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml:String = libraryClient.getBookPageAsHtmlByAuthor(danBrown)
      bookPageAsHtml must include ("The Da Vinci code")
    }

    scenario("get the list of books from a page for an author") {
      Given("A html page with the list of books for Dan Brown")
      val bookPageAsHtml: String = fromFile("data/danBrownBooks.html").mkString
      When("we get the list of books")
      val listOfBooks:List[String] = libraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length 3")
      listOfBooks must contain ("The Da Vinci code")
      listOfBooks.size must be === 3
    }

    scenario("get the list of books from the Internet for an author") {
      Given("A LibraryClient")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      val danBrown = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml:String = libraryClient.getBookPageAsHtmlByAuthor(danBrown)
      val listOfBooks:List[String] = libraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length of at least 15")
      listOfBooks must contain ("The Da Vinci code")
      listOfBooks.size must be > 15
    }

    scenario("get the books written by a list of authors from the local library") {
      Given("A map of authors")
      val authors = Map("Gaiman, Neil" -> Author("Gaiman, Neil"), "Coupland, Douglas" -> Author("Coupland, Douglas"))
      When("we get the books for all authors")
      val books = libraryClient.getBooksForAuthors(authors)
      Then("the result must contain 'JPod' and 'Amerikaanse goden' and has length 9+12")
      books.size must be === 21
      books.filter(bookFilter(_)).size must be === 2
    }

    def bookFilter(book:Book):Boolean = book.title == "JPod" || book.title == "Amerikaanse goden"

  }
}
