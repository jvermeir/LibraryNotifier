package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import scala.collection.mutable.Set

@RunWith(classOf[JUnitRunner])
class BookShelfTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  val author1 = Author("firstName1", "lastName2")
  val author2 = Author("firstName2", "lastName2")
  val authors = Map(author1.lastName -> author1, author2.lastName -> author2)

  feature("The bookshelf shows all books by authors I'm interested in. It also says whether I've read, won't read or haven't read them yet") {
    info("As a family member")
    info("I want to list books on mu bookshelf that I haven't read yet")
    info("So I can go get 'm from the library")

    scenario("the bookshelf accesses the library to find out if there are any new books") {
      Given("a bookshelf with 1 book by author1 and one by author2")
      Config.bookShelf = new TestBookShelf
      Config.libraryClient = new FirstLibraryForTest
      val bookShelf = Config.bookShelf
      When("it refreshes itself from a library with an extra book for author2")
      Config.libraryClient = new SecondLibraryForTest
      bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
      Then("it finds three books")
      val unreadBooks = bookShelf.getUnreadBooks
      3 must be === unreadBooks.size
    }

    //    scenario("Books can be read from a file") {
    //      Given("a file with a list of books in 'FirstName;LastName;Title' format")
    //      val fileName = "data/test/testFileForBookListTest.txt"
    //      When("the file is parsed")
    //      val bookShelf = new FileBasedBookShelf(fileName)
    //      bookShelf.read
    //      val books:List[Book] = bookShelf.getAllBooks
    //      Then("the list of books contains 3 books by 2 authors")
    //      val expectedBooks = List(new Book(new Author("firstname1","lastname1",""), "book1")
    //        ,new Book(new Author("firstname1","lastname1",""), "book2")
    //        ,new Book(new Author("firstname3","lastname3",""), "book3"))
    //      0 must be === (expectedBooks.toSet -- books).size
    //      books.length must be === 3
    //      val authors = books map ( book => book.author)
    //      2 must be === authors.toSet.size
    //    }
    //
    //    scenario("Books can be written to a file") {
    //      Given("a list of books")
    //      val books = List(new Book(new Author("firstname1","lastname1",""), "book1")
    //        ,new Book(new Author("firstname1","lastname1",""), "book2")
    //        ,new Book(new Author("firstname3","lastname3",""), "book3"))
    //      val fileName="data/test/tmp.txt"
    //      When("the list is saved in a temp file")
    //      Book.writeBooksToFile(fileName, books)
    //      Then("if we read the file we get the same books as before")
    //      val booksReadFromDisk:List[Book] = Book.readFromFile(fileName)
    //      0 must be === (books.toSet -- booksReadFromDisk).size
    //      booksReadFromDisk.length must be === 3
    //      val authors = booksReadFromDisk map ( book => book.author)
    //      2 must be === authors.toSet.size
    //    }
    //


  }

  class TestBookShelf extends BookShelf {
    def read: Unit = {
      books.retain((_ => false))
      books ++= Set(new Book(author1, "book1"), new Book(author2, "book2"))
    }

    override def refreshBooksFromLibrary(library: Library, authors: Map[String, Author]) {
      books.retain((_ => false))
      books ++= library.getBooksForAuthors(authors).values.flatten
    }

    def write: Unit = {}

    override def getUnreadBooks: List[Book] = books.toList
  }

  class LibraryForTest extends Library {
    val book1 = Book(author1, "title1")
    val book2 = Book(author2, "title2")
    val book3 = Book(author2, "title3")
    val books: Map[Author, List[Book]] = Map()

    def getBooksByAuthor(authorToSearchFor: Author): List[Book] = books.get(authorToSearchFor).getOrElse(List())

    def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = books
  }

  class FirstLibraryForTest extends LibraryForTest {
    override val books = Map(author1 -> List(book1), author2 -> List(book2))
  }

  class SecondLibraryForTest extends LibraryForTest {
    override val books = Map(author1 -> List(book1), author2 -> List(book2, book3))
  }

}
