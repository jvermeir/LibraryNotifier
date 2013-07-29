package com.library

trait Library {
  def getBooksByAuthor(authorToSearchFor: Author): List[Book]

  def getBooksForAuthors(authors: Map[String, Author]): List[Book]
}
