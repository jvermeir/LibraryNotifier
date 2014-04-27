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
      val book = Book.createFromJSONString(jsonBook)
      Then("We get a book with title 'book2' and link 'mylink'")
      val expectedBook = Book(Author("first", "lastnameA"), "book2", Book.UNKNOWN, "mylink")
      expectedBook must be === book
    }
  }
}
