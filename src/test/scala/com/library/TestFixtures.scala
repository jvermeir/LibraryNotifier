package com.library

/**
 * Some useful stuff for testing
 */
trait TestFixtures {

  def getBookShelfWithThreeBooks: BookShelf = {
    val book1 = new Book(new Author("first", "lastnameB", ""), "book1")
    val book2 = new Book(new Author("first", "lastnameA", ""), "book2")
    val book3 = new Book(new Author("first", "lastnameA", ""), "book3")
    val bookShelf = new TestBookShelf
    bookShelf.add(book1)
    bookShelf.add(book2)
    bookShelf.add(book3)
    bookShelf
  }

  def getBookShelfWithSixBooks: BookShelf = {
    val book1 = new Book(new Author("first", "lastnameB", ""), "book1", available=true)
    val book2 = new Book(new Author("first", "lastnameA", ""), "book2", available=true)
    val book3 = new Book(new Author("first", "lastnameA", ""), "book3", available=true)
    val book4 = new Book(new Author("first", "lastnameB", ""), "book4", available=true)
    val book5 = new Book(new Author("first", "lastnameA", ""), "book5", available=true)
    val book6 = new Book(new Author("first", "lastnameA", ""), "book6")
    val bookShelf = new TestBookShelf
    bookShelf.add(book1)
    bookShelf.add(book2)
    bookShelf.add(book3)
    bookShelf.add(book4)
    bookShelf.add(book5)
    bookShelf.add(book6)
    bookShelf
  }

  def getListOfFiveRecommendedBooks:List[Book] = getBookShelfWithSixBooks.getAllBooks filter(_.available)

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

class LibraryForTest extends Library with TestFixtures{
  val book1 = Book(author1, "title1")
  val book2 = Book(author2, "title2")
  val book3 = Book(author2, "title3")
  val books: Map[Author, List[Book]] = Map()

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = books.get(authorToSearchFor).getOrElse(List())

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = books

  override def isBookAvailable(book: Book): Boolean = false
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
