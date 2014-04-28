package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import com.library.service.LogHelper

@RunWith(classOf[JUnitRunner])
class BookTest extends FeatureSpec with GivenWhenThen with MustMatchers with TestFixtures with LogHelper {

  feature("A book represents info on a book in the library") {
    info("As a family member")
    info("I want the Book to know some details")
    info("So I know if I've read it or not and where to find it on the website")

    scenario("A book can be created from its JSON representation") {
      Given("A book as a JSON string")
      val jsonBook =
        """{"author" : {"firstName" : "first",
          |"lastName" : "lastnameA"}
          |, "title" : "book2"
          |, "status" : "unknown"
          |, "link" : "mylink"
          |}
        """.stripMargin
      When("The JSON string is used to create a Book")
      val book = Book(jsonBook)
      Then("We get a book with title 'book2' and link 'mylink'")
      val expectedBook = Book(Author("first", "lastnameA"), "book2", Book.UNKNOWN, "mylink")
      expectedBook must be === book
    }

    scenario("A book can be created with a 'read' status") {
      Given("A book created with status 'read'")
      val book = new Book(new Author("first", "last", ""), "book", Book.READ)
      When("we read it's status")
      val status = book.status
      Then("the result is 'read'")
      "read" must be === status
    }

    scenario("Changing a books status results in a new instance") {
      Given("A bool with status 'read'")
      val book = new Book(new Author("first", "last", ""), "book", Book.READ)
      When("we change it's status to 'unknown'")
      val updatedBook:Book = book.setStatus(Book.UNKNOWN)
      Then("the old book instance has status 'read' and the new book instance has status 'unknown'")
      Book.READ must be === book.status
      Book.UNKNOWN must be === updatedBook.status
    }

  }
}
