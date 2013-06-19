package com.xebia.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class BookListTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  feature("Books can be stored on disk so we can ignore stuff we've read allready") {
    info("As a family member")
    info("I want to use a text file to store the books I've read")
    info("So I can ignore them in the search for new books")

    scenario("Book is read from a text string") {
      Given("a book in 'FirstName;LastName;Title' format")
      val bookAsString = "Brown; Dan; The Da Vinci code"
      When("the string is parsed")
      val book = Book(bookAsString)
      Then("'Dan Brown - The Da Vinci Code' is found")
      val expectedResult = Book(Author("Dan", "Brown"), "The Da Vinci code")
      expectedResult must be === book
    }

    scenario("Books can be read from a file") {
      Given("a file with a list of books in 'FirstName;LastName;Title' format")
      val fileName = "data/testFileForBookListTest.txt"
      When("the file is parsed")
      val books:List[Book] = Book.readFromFile(fileName)
      Then("the list of books contains 3 books by 2 authors")
      val expectedBooks = List(new Book(new Author("firstname1","lastname1",""), "book1")
          ,new Book(new Author("firstname1","lastname1",""), "book2")
          ,new Book(new Author("firstname3","lastname3",""), "book3"))
      0 must be === (expectedBooks.toSet -- books).size
      books.length must be === 3
      val authors = books map ( book => book.author)
      2 must be === authors.toSet.size
    }

    scenario("Books can be written to a file") {
      Given("a list of books")
      val books = List(new Book(new Author("firstname1","lastname1",""), "book1")
        ,new Book(new Author("firstname1","lastname1",""), "book2")
        ,new Book(new Author("firstname3","lastname3",""), "book3"))
      val fileName="data/tmp.txt"
      When("the list is saved in a temp file")
      Book.writeBooksToFile(fileName, books)
      Then("if we read the file we get the same books as before")
      val booksReadFromDisk:List[Book] = Book.readFromFile(fileName)
      0 must be === (books.toSet -- booksReadFromDisk).size
      booksReadFromDisk.length must be === 3
      val authors = booksReadFromDisk map ( book => book.author)
      2 must be === authors.toSet.size
    }

    scenario("A new book has status 'unknown'") {
      Given("A book created without an explicit status")
      val bookAsString = "Brown; Dan; The Da Vinci code"
      When("we try to parse the book from a string")
      val book = Book(bookAsString)
      Then("a valid book refernce is returned with status 'unknown'")
      book.title must be === "The Da Vinci code"
      book.status must be === Book.UNKNOWN
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