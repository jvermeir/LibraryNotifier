package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import scala.io.Source._
import scala.io.Codec
import com.library.service.LogHelper

@RunWith(classOf[JUnitRunner])
class DutchPublicLibraryTest extends DutchPublicLibrary with FeatureSpec with GivenWhenThen with MustMatchers with LogHelper{

  logger.info("init DutchPublicLibraryTest")
  Config.httpClient = new MockHttpClient
  Config.libraryClient = new DutchPublicLibrary
  val libraryClient = Config.libraryClient
  logger.info("client from DPL: " + new DutchPublicLibrary().myHttpClient )
  logger.info("end DutchPublicLibraryTest")

  val neilGaiman = Author("Gaiman, Neil")
  val alastairReynolds = Author("Reynolds, Alastair")

  feature("The HTTP client finds books written by a list of authors and reports on their availability") {
    info("As a family member")
    info("I want to be notified when a new book by one of my favourite authors becomes available at the library")
    info("So that I can go get it")

    scenario("get the book for an author who has written only one book") {
      Given("An author who wrote only a single book (in the library in Ede, that is...)")
      val author = alastairReynolds
      When("We get his books from the library")
      val books = libraryClient.getBooksByAuthor(author)
      Then("Terminal world is returned as the only result")
      val terminalWorld = Book("Reynolds;Alastair;Terminal world")
      List(terminalWorld) must be === books
    }

    scenario("all books retrieved from the library have a link to their detail page") {
      Given("two authors who wrote a couple of books")
      val listOfAuthors = Map(alastairReynolds.lastName -> alastairReynolds, neilGaiman.lastName -> neilGaiman)
      When("We get their books from the library")
      val books = libraryClient.getBooksForAuthors(listOfAuthors)
      Then("each book has its link field set")
      val numberOfBooksWithoutLinkFieldSet = books.values.flatten filter (_.title.length == 0)
      List() must be === numberOfBooksWithoutLinkFieldSet
    }

    scenario("get the list of books for an author who has written multiple books") {
      Given("An author with several books in the library")
      val author = neilGaiman
      When("We get his books from the library")
      val books = libraryClient.getBooksByAuthor(author)
      Then("Terminal world is returned as the only result")
      10 must be === books.size
    }

    scenario("get the availability status of a book that is available") {
      Given("A book that is available")
      logger.info("client in test: " + Config.httpClient)
      val author = neilGaiman
      val book = libraryClient.getBooksByAuthor(author).head
      When("We get the availability of this book")
      val availability = book.available
      Then("The result is true")
      true must be === availability
    }

    scenario("get the availability status of a book that is not available") {
      Given("A book that is not available")
      val author = alastairReynolds
      val book = libraryClient.getBooksByAuthor(author).head
      When("We get the availability of this book")
      val availability = book.available
      Then("The result is false")
      false must be === availability
    }

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
  }
}
