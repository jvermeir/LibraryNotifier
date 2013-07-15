package com.xebia.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class AuthorTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  feature("The parser can parse author first and last names from a text") {
    info("As a family member")
    info("I want to use a text file to store the authors I'm interested in")
    info("So that I can look 'm up in the library")

    scenario("'Firstname Lastname' format is parsed") {
      Given("a author in 'Firstname Lastname' format")
      val authorAsString = "Dan Brown"
      When("the string is parsed")
      val author = Author(authorAsString)
      Then("one author is found")
      val expectedResult = Author("Dan", "Brown")
      expectedResult must be === author
    }

    scenario("'Firstname              Lastname' format is parsed even if there are lots of blanks between firstname and lastname)") {
      Given("a author in 'Firstname          Lastname' format")
      val authorAsString = "Dan                  Brown"
      When("the string is parsed")
      val author = Author(authorAsString)
      Then("one author is found")
      val expectedResult = Author("Dan", "Brown")
      expectedResult must be === author
    }

    scenario("'Lastname, Firstname' format is parsed") {
      Given("a author in 'Lastname, Firstname' format")
      val authorAsString = "Brown, Dan"
      When("the string is parsed")
      val author = Author(authorAsString)
      Then("one author is found")
      val expectedResult = Author("Dan", "Brown")
      expectedResult must be === author
    }

    scenario("'Lastname' format is parsed") {
      Given("a author in 'Lastname' format")
      val authorAsString = "Brown"
      When("the string is parsed")
      val author = Author(authorAsString)
      Then("one author is found")
      val expectedResult = Author("", "Brown")
      expectedResult must be === author
    }

    scenario("'Lastname, firstname (1964-)' format is parsed") {
      Given("a author in ''Lastname, firstname (1964-)' format")
      val authorAsString = "Brown, Dan (1964-)"
      When("the string is parsed")
      val author = Author(authorAsString)
      Then("one author is found")
      val expectedResult = Author("Dan", "Brown")
      expectedResult must be === author
    }

    scenario("List of strings with author names in various formats is parsed") {
      Given("a list of authors in 'Lastname, Firstname', 'Firstname Lastname', 'Lastname' format")
      val authorsAsStrings = List("Dan Brown1","Brown2, Dan","Brown3")
      When("the string is parsed")
      val authors = AuthorParser.parseAuthorsFromListOfStrings(authorsAsStrings)
      Then("two authors are found")
      val expectedResult = Map[String, Author]("Brown1" ->  Author("Dan", "Brown1"), "Brown2" ->  Author("Dan", "Brown2"), "Brown3" ->  Author("", "Brown3"))
      expectedResult must be === authors
    }

    scenario("File with list of authors can be parsed") {
      Given("a file with a number of lines that represent authors in various formats")
      val authorsAsStrings = List("Dan Brown1","Brown2, Dan","Brown3")
      When("the file is loaded and the list of authors is parsed")
      val authors = AuthorParser.loadAuthorsFromFile("data/testFileWith3Authors.txt")
      Then("three authors are found")
      val expectedResult = Map[String, Author]("Brown1" ->  Author("Dan", "Brown1"), "Brown2" ->  Author("Dan", "Brown2"), "Brown3" ->  Author("", "Brown3"))
      expectedResult must be === authors
    }

    scenario("File with list of authors and a lot of junk can be parsed") {
      Given("a file with a number of lines that represent authors in various formats as well as some empty lines and lines with leading spaces")
      val authorsAsStrings = List("Dan Brown1","Brown2, Dan","Brown3")
      When("the file is loaded and the list of authors is parsed")
      val authors = AuthorParser.loadAuthorsFromFile("data/testFileWithEmptyLinesAndLeadingSpaces.txt")
      Then("three authors are found")
      val expectedResult = Map[String, Author]("Brown1" ->  Author("Dan", "Brown1"), "Brown2" ->  Author("Dan", "Brown2"), "Brown3" ->  Author("", "Brown3"))
      expectedResult must be === authors
    }

    scenario("Match author name regardless of case") {
      Given("an Author instance")
      val author = Author("Dan", "Brown")
      When("compared to anothor author with different upper/lower case spelling")
      val areEqual=author.like(Author("dan","brown"))
      Then("the first author is considerd to be 'like' the second")
      areEqual must be === true
    }
  }
}
