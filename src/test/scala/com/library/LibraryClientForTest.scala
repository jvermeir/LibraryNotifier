package com.library

class LibraryForTest extends Library {
  val author1 = Author("firstName1", "lastName2")
  val author2 = Author("firstName2", "lastName2")
  val book1 = Book(author1, "title1")
  val book2 = Book(author2, "title2")
  val book3 = Book(author2, "title3")
  val books = Map (author1 -> List(book1), author2 -> List(book2, book3))

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = books.get(authorToSearchFor).getOrElse(List())

  def getBooksForAuthors(authors: Map[String, Author]): List[Book] = List(book1, book2)
}
