package com.library

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
      val authors = Author.parseAuthorsFromListOfStrings(authorsAsStrings)
      Then("two authors are found")
      val expectedResult = Map[String, Author]("Brown1" ->  Author("Dan", "Brown1"), "Brown2" ->  Author("Dan", "Brown2"), "Brown3" ->  Author("", "Brown3"))
      expectedResult must be === authors
    }

    scenario("File with list of authors can be parsed") {
      Given("a file with a number of lines that represent authors in various formats")
      val authorsAsStrings = List("Dan Brown1","Brown2, Dan","Brown3")
      When("the file is loaded and the list of authors is parsed")
      val authors = Author.loadAuthorsFromFile("data/test/testFileWith3Authors.txt")
      Then("three authors are found")
      val expectedResult = Map[String, Author]("Brown1" ->  Author("Dan", "Brown1"), "Brown2" ->  Author("Dan", "Brown2"), "Brown3" ->  Author("", "Brown3"))
      expectedResult must be === authors
    }

    scenario("File with list of authors and a lot of junk can be parsed") {
      Given("a file with a number of lines that represent authors in various formats as well as some empty lines and lines with leading spaces")
      val authorsAsStrings = List("Dan Brown1","Brown2, Dan","Brown3")
      When("the file is loaded and the list of authors is parsed")
      val authors = Author.loadAuthorsFromFile("data/test/testFileWithEmptyLinesAndLeadingSpaces.txt")
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

    // TODO: ??? doesn't fail while it happend once in real life. maybe when reading from a file?
    scenario("Author names starting with a blank are trimmed") {
      Given("an author name staring with a blank")
      val authorNameStartingWithABlank = "  lastname, firstname"
      When("the name is parsed")
      val author = Author.apply(authorNameStartingWithABlank)
      Then("leading blanks are removed")
      Author("firstname", "lastname") must be === author
    }

    scenario("Author can be parsed from JSON string") {
      Given("an Author as a JSON string")
      val author = """{"author" : {"firstName" : "firstName",
                     |"lastName" : "lastName",
                     |"link" : "link"}
                     |}""".stripMargin
      When("An Author is created from the JSON string")
      val authorFromJSON = Author.fromJSONString(author)
      Then("it must match {firstName, lastName, link}")
      Author("firstName", "lastName", "link") must be === authorFromJSON
    }

    scenario("List of Authors can be parsed from JSON string") {
      Given("a list of Authors as a JSON string")
      When("Authors are created from the JSON string")
      val authorsFromJSON = Author.getAuthorsFromJSON(authors)
      Then("The list must contain 2 authors")
      2 must be === authorsFromJSON.size
    }

    scenario("A list of authors written as JSON can be read as JSON") {
      Given("some Authors in 'authors'")
      val authorFromJSON = Author.getAuthorsFromJSON(authors)
      When("The list of authors is converted to a string and parsed again")
      val l = authorFromJSON map (author => author.toLastNameCommaFirstNameString -> author)
      val authorsAsString = Author.authorsAsJSONString(l.toMap)
      Then("if they're parsed again there are still 2 left")
      val reparsedAuthors = Author.getAuthorsFromJSON(authorsAsString)
      2 must be === reparsedAuthors.size
    }
  }
  val authors = """{"authors" : [{"author" : {"firstName" : "firstName",
                  |"lastName" : "lastName",
                  |"link" : "link"}},
                  |{"author" : {"firstName" : "firstName2",
                  |"lastName" : "lastName2",
                  |"link" : "link"}}]}
                  |""".stripMargin
}
