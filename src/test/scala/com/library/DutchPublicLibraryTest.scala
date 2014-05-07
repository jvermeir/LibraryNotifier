package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, GivenWhenThen}
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import com.library.service.LogHelper

@RunWith(classOf[JUnitRunner])
class DutchPublicLibraryTest extends DutchPublicLibrary with FeatureSpec with GivenWhenThen with MustMatchers with LogHelper{

  Config.libraryClient = this
  override val httpClient = new MockHttpClient
  Config.httpClient = httpClient
  
  val neilGaiman = Author("Gaiman, Neil")
  val alastairReynolds = Author("Reynolds, Alastair")
  val singleBookAuthor =
    """{"author" : {"firstName" : "Alastair",
      |"lastName" : "Reynolds"}
      |, "title" : "Terminal world"
      |, "status" : "unknown"
      |, "link" : "mylink"
      |}
    """.stripMargin


  feature("The HTTP client finds books written by a list of authors and reports on their availability") {
    info("As a family member")
    info("I want to be notified when a new book by one of my favourite authors becomes available at the library")
    info("So that I can go get it")

    scenario("get the book for an author who has written only one book") {
      Given("An author who wrote only a single book (in the library in Ede, that is...)")
      val author = alastairReynolds
      When("We get his books from the library")
      val books = getBooksByAuthor(author)
      Then("Terminal world is returned as the only result")
      val terminalWorld = Book(singleBookAuthor)
      List(terminalWorld) must be === books
    }

    scenario("all books retrieved from the library have a link to their detail page") {
      Given("two authors who wrote a couple of books")
      val listOfAuthors = Map(alastairReynolds.lastName -> alastairReynolds, neilGaiman.lastName -> neilGaiman)
      When("We get their books from the library")
      val books = getBooksForAuthors(listOfAuthors)
      Then("each book has its link field set")
      val numberOfBooksWithoutLinkFieldSet = books.values.flatten filter (_.title.length == 0)
      List() must be === numberOfBooksWithoutLinkFieldSet
    }

    scenario("get the list of books for an author who has written multiple books") {
      Given("An author with several books in the library")
      val author = neilGaiman
      When("We get his books from the library")
      val books = getBooksByAuthor(author)
      Then("Terminal world is returned as the only result")
      13 must be === books.size
    }

    scenario("get the availability status of a book that is available") {
      Given("A book that is available")
      val author = neilGaiman
      val book = getBooksByAuthor(author).head
      When("We get the availability of this book")
      val availability = book.available
      Then("The result is true")
      true must be === availability
    }

    scenario("get the availability status of a book that is not available") {
      Given("A book that is not available")
      val author = alastairReynolds
      val book = getBooksByAuthor(author).head
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
      val booksToBeRead: List[Book] = getNewBooks(booksFromFile, booksFromWeb)
      Then("only books with status 'UNKNOWN' are returned")
      booksToBeRead must be === List(unknownBook)
    }

    scenario("replace sid in a books link") {
      Given("A book link and an sid")
      val sid = httpClient.startBicatSessionAndReturnSid
      val link = """http://bicat.cultura-ede.nl/cgi-bin/bx.pl?dcat=1;wzstype=;extsdef=01;event=tdetail;wzsrc=;woord=Atwood%2C%20Margaret;titcode=294174;rubplus=TS0;vv=JN;vestfiltgrp=;sid=5e4b3e16-e6b2-420e-b026-b247fe387488;groepfx=10;vestnr=8399;prt=INTERNET;taal=1;zl_v=N;sn=12;var=portal"""
      When("We call the setSid method")
      val newLink = httpClient.setSidInLink(link)
      Then("The result contains the new sid")
      true must be === newLink.contains(httpClient.sid)
    }

  }
}
