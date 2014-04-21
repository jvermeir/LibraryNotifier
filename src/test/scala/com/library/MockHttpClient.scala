package com.library

import scala.io.Source._
import scala.io.Codec
import com.library.service.LogHelper

class MockHttpClient extends MyHttpClient with LogHelper {

  override def getBookPageAsHtmlFromBookUrl(book: Book): String = {
    logger.debug("getBookPageAsHtmlFromBookUrl: " + book)
    val page = book.author.lastName match {
      case "Gaiman" => fromFile("data/test/pageForBookDetailsForAvailableBook.html")(Codec.ISO8859).mkString
      case "Reynolds" => fromFile("data/test/pageForBookDetailsForUnAvailableBook.html")(Codec.ISO8859).mkString
    }
    page
  }

  override def getBookPageAsHtmlByAuthor(author: Author): String = {
    logger.debug("getBookPageAsHtmlByAuthor: " + author)
    val page = author.lastName match {
      case "Gaiman" => fromFile("data/test/pageForAuthorWithMoreThanOneBook.html")(Codec.ISO8859).mkString
      case "Reynolds" => fromFile("data/test/pageForAuthorWithOneBook.html")(Codec.ISO8859).mkString
    }
    page
  }

  override def getResultOfSearchByAuthor(authorName: String): String = {
    val page = authorName match {
      case "Gaiman, Neil" => fromFile("data/test/resultPageForGaimainSearch.html")(Codec.ISO8859).mkString
      case "Reynolds, Alastair" => fromFile("data/test/resultPageForReynoldsSearch.html")(Codec.ISO8859).mkString
    }
    page
  }
}
