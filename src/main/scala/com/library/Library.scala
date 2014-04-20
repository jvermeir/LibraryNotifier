package com.library

trait Library {
  def isBookAvailable(book: Book): Boolean

  def getBooksByAuthor(authorToSearchFor: Author): List[Book]

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]]

  def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[Book]

  def getNewBooks(myBooks: List[Book], booksFromWeb: List[Book]): List[Book]
}
