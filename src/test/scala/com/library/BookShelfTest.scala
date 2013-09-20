package com.library

import org.scalatest.junit.JUnitRunner
import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class BookShelfTest extends FeatureSpec with GivenWhenThen with MustMatchers with TestFixtures{

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

    scenario("if the bookshelf contains a book with status other than UNKNOWN, this status is not affected by a refreshBooksFromLibrary call") {
      Given("a bookshelf with a book with status READ")
      Config.bookShelf = new TestBookShelf
      Config.libraryClient = new FirstLibraryForStatusTest
      val bookShelf = Config.bookShelf
      bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
      1 must be === bookShelf.getBooksToRead.size
      When("it refreshes itself from a library with an extra book for author2")
      Config.libraryClient = new SecondLibraryForStatusTest
      bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
      Then("it finds two books to read")
      2 must be === bookShelf.getBooksToRead.size
    }

    scenario("Books can be read from a file") {
      Given("a file with a list of books in 'FirstName;LastName;Title' format")
      val fileName = "data/test/testFileForBookListTest.txt"
      When("the file is parsed")
      val bookShelf = new FileBasedBookShelf(fileName)
//      bookShelf.read
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

    scenario("The list of books to read is printed in alphabetic order by author last name and then by title") {
      Given("a list of books to read from the bookshelf")
      val bookShelf = getBookShelfWithThreeBooks
      When("the list is printed as a shopping list")
      val actualShoppingList = bookShelf.printAsWishList
      Then("it is ordered by the last name of the author and then by title")
      val expectedShoppingList = "lastnameA;first;book2\n" + "lastnameA;first;book3\n" + "lastnameB;first;book1"
      expectedShoppingList must be === actualShoppingList
    }
  }

  scenario("The list of books to read is printed as html page, in alphabetic order by author last name and then by title") {
    Given("a list of books to read from the bookshelf")
    val bookShelf = getBookShelfWithThreeBooks
    When("the list is printed as a html")
    val actualShoppingListAsHtml = bookShelf.printAsHtml
    Then("it is ordered by the last name of the author and then by title")
    val expectedShoppingListAsHtml = "<table>\n<tr><td>lastnameA</td><td>first</td><td>book2</td></tr>\n" +
      "<tr><td>lastnameA</td><td>first</td><td>book3</td></tr>\n" +
      "<tr><td>lastnameB</td><td>first</td><td>book1</td></tr>\n</table>"
    expectedShoppingListAsHtml must be === actualShoppingListAsHtml
  }
}
