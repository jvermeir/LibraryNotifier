package com.xebia.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import scala.io.Source._

@RunWith(classOf[JUnitRunner])
class DutchPublicLibraryTest extends DutchPublicLibrary with FeatureSpec with GivenWhenThen with MustMatchers {

  val libraryClient = Config.libraryClient
  // Start a session here because sid was changed to lazy so we need to make sure a session exists
  // TODO: maybe we should just use the DutchPublicLibrary class here?
  libraryClient.startBicatSessionAndReturnSid

  feature("The HTTP client finds books written by a list of authors and reports on their availability") {
    info("As a family member")
    info("I want to be notified when a new book by one of my favourite authors becomes available at the library")
    info("So that I can go get it")

    scenario("Reading data from a URL") {
      Given("A url")
      val url = "http://localhost/"
      When("the page is loaded from the url")
      val data: String = libraryClient.readTextFromUrl(url);
      Then("the text 'It works' appears in the page data")
      val itWorksFound = data.contains("It works")
      true must be === itWorksFound
    }

    scenario("Reading data from http://bicat.cultura-ede.nl/") {
      Given("A url")
      val url = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
      When("the page is loaded from the url")
      val data = libraryClient.readTextFromUrl(url)
      Then("the text 'Hoofdmenu' appears in the page data")
      val hoofdmenuFound = data.contains("Hoofdmenu")
      true must be === hoofdmenuFound
    }

    scenario("getting bicat_sid cookie from http://bicat.cultura-ede.nl/") {
      Given("A libraryClient")
      When("the start page is loaded from the url")
      Then("the BICAT_SID cookie is set to a 5-part string")
      val bicatCookie = libraryClient.bicatCookie
      5 must be === bicatCookie.getValue.split("-").size
    }

    scenario("get the page with the list of writers that satisfy a author search criterium") {
      Given("A libraryClient")
      When("we search for 'Dan Brown")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      Then("we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      data must include regex ("Brown, Dan.*(1964-)")
    }

    scenario("get the list of writers that satisfy a author search criterium") {
      Given("A libraryClient")
      When("we search for 'Dan Brown' we get a webpage that contains the text 'Brown, Dan  (1964-)'")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      Then("we get the list of authors from that page and the first author in the list is 'Brown, Dan.*(1964-)'")
      val author = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      author.equalsIgnoreLink(Author("Dan", "Brown")) must be === true
    }

    scenario("get the page with the list of books for an author") {
      Given("A libraryClient")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      val danBrown = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml: String = libraryClient.getBookPageAsHtmlByAuthor(danBrown)
      bookPageAsHtml must include("The Da Vinci code")
    }

    scenario("get the list of books from a page for an author") {
      Given("A html page with the list of books for Dan Brown")
      val bookPageAsHtml: String = fromFile("data/danBrownBooks.html").mkString
      When("we get the list of books")
      val listOfBooks: List[String] = libraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length 6")
      listOfBooks must contain("The Da Vinci code")
      listOfBooks.size must be === 6
    }

    scenario("get the list of books from the Internet for an author") {
      Given("A libraryClient")
      val data: String = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
      val danBrown = libraryClient.getAuthorUpdatedWithLink(data, new Author("Dan", "Brown", "link"))
      When("we get the books for 'Brown, Dan'")
      val bookPageAsHtml: String = libraryClient.getBookPageAsHtmlByAuthor(danBrown)
      val listOfBooks: List[String] = libraryClient.getBooksFromHtmlPage(bookPageAsHtml, Author("Brown, Dan"))
      Then("the result contains 'The Da Vinci code' and has length of at least 15")
      listOfBooks must contain("The Da Vinci code")
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

    def bookFilter(book: Book): Boolean = book.title == "JPod" || book.title == "Amerikaanse goden"

    scenario("Comparing the list of books stored on disk and the list retrieved from the Internet, create a list of books to read") {
      Given("A list of books and their status on disk and a list of books retrieved from the Internet")
      val unknownBook: Book = new Book(Author("", "b"), "unknown", Book.UNKNOWN)
      val wontReadBook: Book = new Book(Author("a", "b"), "wont read", Book.WONT_READ)
      val readBook: Book = new Book(Author("a", "b"), "read", Book.READ)
      val booksFromFile: List[Book] = List(readBook, wontReadBook)
      val booksFromWeb: List[Book] = List(readBook, wontReadBook, unknownBook)
      When("the lists are compared")
      val booksToBeRead: List[Book] = libraryClient.getNewBooks(booksFromFile, booksFromWeb)
      Then("only books with status 'UNKNOWN' are returned")
      booksToBeRead must be === List(unknownBook)
    }

    scenario("Get list of candidate books by reading a file and loading 'm of the web") {
      Given("a file with the books I've read and a list of books from my favourite writers")
      val myBooks = Book.readFromFile("data/booksForGetListOfCandidateBooks...test.txt")
      val allBooks = libraryClient.getBooksForAuthorsInFile("data/authorsForGetListOfCandidateBooks...test.txt")
      When("we get the list of books I might want to read")
      val booksToRead:List[Book] = libraryClient.getNewBooks(myBooks, allBooks)
      Then("the result is 1 book by Bennie Mols, 7 books by Douglas Coupland, 10 books by Neil Gaiman (yeah), 38 books by Thomas Ross and no books by Paul Harland (snif)")
      booksToRead.filter( book => book.author == Author("Bennie", "Mols","")).size must be === 1
      booksToRead.filter( book => book.author == Author("Douglas", "Coupland","")).size must be === 7
      booksToRead.filter( book => book.author == Author("Neil", "Gaiman","")).size must be === 10
      booksToRead.filter( book => book.author == Author("Tomas", "Ross","")).size must be === 45
      booksToRead.filter( book => book.author == Author("Paul", "Harland","")).size must be === 0
      booksToRead.size must be === 63
    }
  }
}
