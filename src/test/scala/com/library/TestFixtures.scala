package com.library

/**
 * Some useful stuff for testing
 */
trait TestFixtures {

  def getBookShelfWithThreeBooks: BookShelf = {
    val book1 = new Book(new Author("first", "lastnameB", ""), "book1", link = "mylink")
    val book2 = new Book(new Author("first", "lastnameA", ""), "book2")
    val book3 = new Book(new Author("first", "lastnameA", ""), "book3")
    val bookShelf = new TestBookShelf
    bookShelf.add(book1)
    bookShelf.add(book2)
    bookShelf.add(book3)
    bookShelf
  }

  def getBookShelfWithSixBooks: BookShelf = {
    val book1 = new Book(new Author("first", "lastnameB", ""), "book1")
    val book2 = new Book(new Author("first", "lastnameA", ""), "book2")
    val book3 = new Book(new Author("first", "lastnameA", ""), "book3")
    val book4 = new Book(new Author("first", "lastnameB", ""), "book4")
    val book5 = new Book(new Author("first", "lastnameA", ""), "book5")
    val book6 = new AvailableBook
    val bookShelf = new TestBookShelf
    bookShelf.add(book1)
    bookShelf.add(book2)
    bookShelf.add(book3)
    bookShelf.add(book4)
    bookShelf.add(book5)
    bookShelf.add(book6)
    bookShelf.storeRecommendations(bookShelf.getBooksToRead)
    bookShelf
  }
  class AvailableBook extends Book(new Author("first", "lastnameA", ""), "title") {val availabe = true}

  def getListOfOneRecommendedBook:List[Book] = getBookShelfWithSixBooks.getAllBooks filter(_.available)

  val author1 = Author("firstName1", "lastName2")
  val author2 = Author("firstName2", "lastName2")
  val authors = Map(author1.lastName -> author1, author2.lastName -> author2)

}

class TestBookShelf extends BookShelf {

  override def read = scala.collection.mutable.Map[String, Book] ()

  override def write: Unit = {}
}

class TestBookShelfThatContainsABookWithStatusRead extends BookShelf with TestFixtures {
  override lazy val books: scala.collection.mutable.Map[String, Book] = scala.collection.mutable.Map[String, Book]()
  val book1 = Book(author1, "title1", Book.READ)
  val book2 = Book(author2, "title2")
  books.put(book1.getKey, book1)
  books.put(book2.getKey, book2)

  override def read = scala.collection.mutable.Map[String, Book] ()

  def write: Unit = {}
}
          /*     TODO:
class BookShelfForRecommendationTest extends BookShelf {
  val bookShelf = new FileBasedBookShelf("data/books.json")
  val temp = bookShelf.getAllBooks.take(6)
  override lazy val books = temp map (book => book.author -> book )
  override def getAllBooks:List[Book] = books map (_._2)
  override def write:Unit = ???
  override def read:Unit = ???
}           */

class LibraryForRecommendationTest extends DutchPublicLibrary {
  val bookShelf = new FileBasedBookShelf("data/books.json")
  val books = bookShelf.getAllBooks.take(6)

  override def getBooksByAuthor(authorToSearchFor: Author): List[Book] = books

  override def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = Map{Author("a","b") -> books}

  override def isBookAvailable(book: Book): Boolean = true

  override def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[Book] = ???
}

class LibraryForTest extends Library with TestFixtures{
  val book1 = Book(author1, "title1")
  val book2 = Book(author2, "title2")
  val book3 = Book(author2, "title3")
  val books: Map[Author, List[Book]] = Map()

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = books.get(authorToSearchFor).getOrElse(List())

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = books

  override def isBookAvailable(book: Book): Boolean = true

  override def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[Book] = ???
}

class FirstLibraryForTest extends LibraryForTest {
  override val books = Map(author1 -> List(book1), author2 -> List(book2))
}

class SecondLibraryForTest extends LibraryForTest {
  override val books = Map(author1 -> List(book1), author2 -> List(book2, book3))
}

class FirstLibraryForStatusTest extends LibraryForTest {
  override val book2 = Book(author2, "title2", Book.READ)
  override val books = Map(author1 -> List(book1), author2 -> List(book2))
}

class SecondLibraryForStatusTest extends LibraryForTest {
  override val book2 = Book(author2, "title2")
  override val books = Map(author1 -> List(book1), author2 -> List(book2, book3))
}

class LibraryForAvailabilityTest extends LibraryForTest with TestFixtures {
  val bookShelf = getBookShelfWithSixBooks
  override val books:Map[Author, List[Book]] = Map ()
  override def isBookAvailable(book: Book): Boolean = {
    val bookFromLibrary = bookShelf.getAllBooks filter (_ == book)
    if (bookFromLibrary.length>0) bookFromLibrary.head.available
    else false
  }
}
