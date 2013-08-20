package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class BookShelfTest extends FeatureSpec with GivenWhenThen with MustMatchers {

  val author1 = Author("firstName1", "lastName2")
  val author2 = Author("firstName2", "lastName2")
  val authors = Map(author1.lastName -> author1, author2.lastName -> author2)

  feature("The bookshelf shows all books by authors I'm interested in. It also says whether I've read, won't read or haven't read them yet") {
    info("As a family member")
    info("I want to list books on my bookshelf that I haven't read yet")
    info("So I can go get 'm from the library")

    scenario("the bookshelf accesses the library to find out if there are any new books") {
      Given("a bookshelf with 1 book by author1 and one by author2")
      Config.bookShelf = new TestBookShelf
      Config.libraryClient = new FirstLibraryForTest
      val bookShelf = Config.bookShelf
      bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
      2 must be === bookShelf.getBooksToRead.size
      When("it refreshes itself from a library with an extra book for author2")
      Config.libraryClient = new SecondLibraryForTest
      bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
      Then("it finds three books")
      3 must be === bookShelf.getBooksToRead.size
    }

    scenario("Books can be read from a file") {
      Given("a file with a list of books in 'FirstName;LastName;Title' format")
      val fileName = "data/test/testFileForBookListTest.txt"
      When("the file is parsed")
      val bookShelf = new FileBasedBookShelf(fileName)
      bookShelf.read
      val books: List[Book] = bookShelf.getAllBooks
      Then("the list of books contains 3 books by 2 authors")
      val expectedBooks = List(new Book(new Author("firstname1", "lastname1", ""), "book1")
        , new Book(new Author("firstname1", "lastname1", ""), "book2")
        , new Book(new Author("firstname3", "lastname3", ""), "book3"))
      0 must be === (expectedBooks.toSet -- books).size
      3 must be === books.length
      val authors = books map (book => book.author)
      2 must be === authors.toSet.size
    }

    scenario("Books can be written to a file") {
      Given("a list of books")
      val expectedBooks = List(new Book(new Author("firstname1", "lastname1", ""), "book1")
        , new Book(new Author("firstname1", "lastname1", ""), "book2")
        , new Book(new Author("firstname3", "lastname3", ""), "book3"))
      val fileName = "data/test/tmp.txt"
      When("the list is saved in a temp file, the shelf is emptied and the file read back")
      val bookShelf = new FileBasedBookShelf(fileName)
      bookShelf.emptyShelf
      expectedBooks.foreach(bookShelf.add(_))
      3 must be === bookShelf.getAllBooks.size
      bookShelf.write
      bookShelf.emptyShelf
      0 must be === bookShelf.getAllBooks.size
      Then("if we read the file we get the same books as before")
      bookShelf.read
      val books: List[Book] = bookShelf.getAllBooks
      0 must be === (expectedBooks.toSet -- books.toSet).size
      3 must be === books.length
      val authors = books map (book => book.author)
      2 must be === authors.toSet.size
    }

    scenario("The bookshelf can get a list of books I can read") {
      Given("a list of books")
      val book1 = new Book(new Author("firstname1", "lastname1", ""), "book1", Book.READ)
      val book2 = new Book(new Author("firstname1", "lastname1", ""), "book2")
      val book3 = new Book(new Author("firstname1", "lastname1", ""), "book3", Book.WONT_READ)
      val book4 = new Book(new Author("firstname1", "lastname1", ""), "book4", Book.UNKNOWN)
      val books = List(book1, book2, book3, book4)
      // TODO: comment, refactor?
      When("the list is saved in a temp file, the shelf is emptied and the file read back")
      val bookShelf = new TestBookShelf
      bookShelf.emptyShelf
      books.foreach(bookShelf.add(_))
      Then("if we get unread books only book2 and book4 are returned")
      val booksToRead = bookShelf.getBooksToRead.toSet
      2 must be === booksToRead.size
      true must be === booksToRead.contains(book2)
      true must be === booksToRead.contains(book4)
    }

    scenario("The status of a book can be changed and saved in the data store") {
      Given("a list of books on a book shelf")
      val book1 = new Book(new Author("firstname1", "lastname1", ""), "book1", Book.READ)
      val book2 = new Book(new Author("firstname1", "lastname1", ""), "book2")
      val bookShelf = new TestBookShelf
      bookShelf.add(book1)
      bookShelf.add(book2)
      When("if we update the status of book2 to WONT_READ")
      bookShelf.setStatusForBook(book2, Book.WONT_READ)
      Then("the booksToRead method returns 0")
      0 must be === bookShelf.getBooksToRead.toSet.size
    }

  }

  class TestBookShelf extends BookShelf {
    val book1 = Book(author1, "title1")
    val book2 = Book(author2, "title2")
    def read: Unit = {
      books.retain((k,v) => false)
      books ++= Map((book1.getKey -> book1),(book2.getKey -> book2))
    }

    def write: Unit = {}

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
