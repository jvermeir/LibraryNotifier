package com.library

import scala.language.postfixOps
import scala.Some
import com.library.service.LogHelper

/**
 * Access the website for the public library in Ede to find out if there are any new books by authors of interest.
 */

class DutchPublicLibrary extends Library with LogHelper {

  val httpClient = Config.httpClient
  
  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = {
    logger.debug("Get books for: " + authorToSearchFor)
    val author = updateAuthorWithLinkToBooks(authorToSearchFor)
    val result = if (author.like(authorToSearchFor)) {
      val bookpage = httpClient.getBookPageAsHtmlByAuthor(author)
      getBooksFromHtmlPage(bookpage, author)
    } else List()
    logger.debug(result.size + " books found")
    result
  }

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = {
    val books = authors.values map (author => author -> getBooksByAuthor(author))
    books.toMap
  }

  protected[library] def updateAuthorWithLinkToBooks(author: Author): Author = {
    val authorSearchResultPage = httpClient.getResultOfSearchByAuthor(author.toLastNameCommaFirstNameString)
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

  protected def createBooksWithLink(books: List[String], author: Author) = {
    books map (Book(author, _, link=author.linkToListOfBooks))
  }  

  // TODO: cleanup, extract stuff
  def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[Book] = {
    logger.debug("getBooksFromHtmlPage for author: " + author)
    val patternString = """<h3 class="anoniem_titel"><strong id="anoniem_titel_titel"><img class="stat_icons" title="Boek" alt="bvm_b__.gif" src="/images/bvm_b__.gif">(.*?)</strong></h3>"""
    val pattern = patternString.r
    val books = pattern.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
    val result = books.length match {
      case 0 => {
        val p="""<a href="(.*?)" title="(.*?)" class="title">""".r
        val links =  p.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
        val titles =  p.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
        val linksAndTitles = links zip titles
        linksAndTitles map (b => Book (author, b._1, link=b._2 ))
      }
      case _ => createBooksWithLink(books, author)
    }
    logger.debug("getBooksFromHtmlPage result: " + result)
    result
  }

  // TODO: this is a strange place for this code because it doesn't actually use the library. Move to BookShelf?
  def getNewBooks(myBooks: List[Book], booksFromWeb: List[Book]): List[Book] = {
    val candidates = booksFromWeb.toSet
    val booksWithStatusReadOrWontRead = myBooks.filter(book => book.status != Book.UNKNOWN)
    val newBooks = candidates -- booksWithStatusReadOrWontRead
    newBooks.toList
  }

  val beschikbaarExpression = """class="staticons">(.*)</div>""".r
  val locationExpression = """"exemplaar_vest">(.*)</div>""".r

  override def isBookAvailable(book: Book): Boolean = {
    val bookPage = httpClient.getBookPageAsHtmlFromBookUrl(book)
    val indexOfExemplaarInfoTag = bookPage.indexOf("""class="exemplaarinfo""")
    val indexOfEldersTag = bookPage.indexOf("""<div id="elders_button"""")
    if (indexOfEldersTag>indexOfExemplaarInfoTag) {
      val fragment = bookPage.substring(indexOfExemplaarInfoTag, indexOfEldersTag)
      val lines = fragment.split("\n")
      val availabilityStatuses = lines filter (_.indexOf("""img alt="""")>0) map (beschikbaarExpression.findFirstMatchIn(_) map (_ group 1))
      val locations = lines filter (_.indexOf("""div class="exemplaar_vest""")>0) filter (_.indexOf("Vestiging")<0) map (locationExpression.findFirstMatchIn(_) map (_ group 1))
      val availableInEde = availabilityStatuses zip locations filter (isAvailableInEde(_))
      availableInEde.length>0
    } else false
  }

  private def isAvailableInEde(a:(Option[String],Option[String])):Boolean = {
    val available = a._1 getOrElse ("N/A") equals "Beschikbaar"
    val inEde = if (available) {
      a._2 getOrElse("N/A") equals("Ede Centrale")
    } else false
    inEde
  }

}
