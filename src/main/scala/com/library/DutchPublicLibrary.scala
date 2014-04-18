package com.library

import scala.language.postfixOps
import scala.Some
import com.library.service.LogHelper

/**
 * Access the website for the public library in Ede to find out if there are any new books by authors of interest.
 */

class DutchPublicLibrary extends Library with LogHelper {

  val myHttpClient = Config.httpClient

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = {
    logger.info("Get books for: " + authorToSearchFor)
    val author = updateAuthorWithLinkToBooks(authorToSearchFor)
    val result = if (author.like(authorToSearchFor)) {
      val bookpage = myHttpClient.getBookPageAsHtmlByAuthor(author)
      val titlesAndLinks = getBooksFromHtmlPage(bookpage, author)
      titlesAndLinks map {
//        titleAndLink => Book(author, titleAndLink._1, titleAndLink._2)
          titleAndLink => Book(author, titleAndLink)
      }
    } else List()
    logger.debug(result.size + " books found")
    result
  }

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = {
    val books = authors.values map (author => author -> getBooksByAuthor(author))
    books.toMap
  }

  protected[library] def updateAuthorWithLinkToBooks(author: Author): Author = {
    val authorSearchResultPage = myHttpClient.getResultOfSearchByAuthor(author.toFirstNameLastNameString)
    getAuthorUpdatedWithLink(authorSearchResultPage, author)
  }

  protected[library] def getAuthorUpdatedWithLink(webPage: String, authorWithoutLink: Author): Author = {
    logger.debug("getAuthorUpdatedWithLink " + authorWithoutLink)
    val singleLineAuthorFragmentPattern = """(?m)<td class="thsearch_wordlink">(.*?)</td>"""
    val multiLineAuthorFragmentPattern = """(?m)<td class="thsearch_wordlink">.*\n(.*)\n.*</td>"""
    val result = getAuthorUpdatedWithLink(webPage, singleLineAuthorFragmentPattern, authorWithoutLink)
    result match {
      case None => getAuthorUpdatedWithLink(webPage, multiLineAuthorFragmentPattern, authorWithoutLink) getOrElse new UnknownAuthor
      case _ => result getOrElse new UnknownAuthor
    }
  }

  protected[library] def getAuthorUpdatedWithLink(webPage: String, pattern: String, author: Author): Option[Author] = {
    logger.debug("getAuthorUpdatedWithLink: " + author)
    val authorFragment = pattern.r
      .findFirstMatchIn(webPage)
      .map(_ group 1).getOrElse("")
    val authorLinkPattern = """<a href="(.*?)">(.*?)<""".r
    val link = authorLinkPattern
      .findFirstMatchIn(authorFragment)
      .map(_ group 1).getOrElse("")
    val authorAsString = authorLinkPattern
      .findFirstMatchIn(authorFragment)
      .map(_ group 2).getOrElse("")
    val authorFromWebPage = Author(authorAsString)
    val result = if (author.like(authorFromWebPage)) {
      val linkWithAantalFieldSetTo60 = link.replaceFirst("aantal=10", "aantal=60")
      Some(Author(author, linkWithAantalFieldSetTo60))
    } else None
    logger.debug("getAuthorUpdatedWithLink(result): " + result)
    result
  }

  // TODO: get link to book page as well as title from this method
  protected[library] def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[String] = {
    logger.debug("getBooksFromHtmlPage for author: " + author)
    val patternString = """<a class="title" title="(.*?)""""
    val pattern = patternString.r
    val books = pattern.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
    val result = books.length match {
      case 0 => {
        val p2 = """<meta xmlns:og="http://ogp.me/ns#" name="title" content="(.*?)"""".r
        p2.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
      }
      case _ => books
    }
    logger.debug("getBooksFromHtmlPage result: " + result)
    result
  }

  def getNewBooks(myBooks: List[Book], booksFromWeb: List[Book]): List[Book] = {
    val candidates = booksFromWeb.toSet
    val booksWithStatusReadOrWontRead = myBooks.filter(book => book.status != Book.UNKNOWN)
    val newBooks = candidates -- booksWithStatusReadOrWontRead
    newBooks.toList
  }

  override def isBookAvailable(book: Book): Boolean = true

}
