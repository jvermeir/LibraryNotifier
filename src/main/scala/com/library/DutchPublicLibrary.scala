package com.library

import org.apache.http.client.fluent.Request
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.apache.http.client.params.{ClientPNames, CookiePolicy}
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.cookie.Cookie
import scala.collection.JavaConversions._
import org.apache.http.util.EntityUtils
import scala.language.postfixOps

/**
 * Access the website for the public library in Ede to find out if there are any new books by authors of interest.
 */

class DutchPublicLibrary extends Library {

  val bicatStartOfSessionUrl = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
  val cookieStore = new BasicCookieStore
  val httpContext = new BasicHttpContext
  val httpclient: HttpClient = new DefaultHttpClient
  httpclient.getParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH)
  httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore)

  lazy val sid = startBicatSessionAndReturnSid
  lazy val bicatCookie = getBicatCookie

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = {
    println("Get books for: " + authorToSearchFor)
    val author = updateAuthorWithLinkToBooks(authorToSearchFor)
    val result = if (author.like(authorToSearchFor)) {
      val bookpage = getBookPageAsHtmlByAuthor(author)
      val titles = getBooksFromHtmlPage(bookpage, author)
      titles map {
        title => Book(author, title)
      }
    } else List()
    println(result.size + " books found")
    result
  }

  def getBooksForAuthors(authors: Map[String, Author]): Map[Author, List[Book]] = {
    val books = authors.values map (author => author -> getBooksByAuthor(author))
    // TODO: replace with logging framework
    println("Books read from library")
    books.toMap
  }

  protected[library] def updateAuthorWithLinkToBooks(author: Author): Author = {
    val authorSearchResultPage = getResultOfSearchByAuthor(author.toFirstNameLastNameString)
    getAuthorUpdatedWithLink(authorSearchResultPage, author)
  }

  protected[library] def getResultOfSearchByAuthor(authorName: String): String = {
    val formParameters = getParametersForAuthorQuery(authorName, sid)
    val entity = new UrlEncodedFormEntity(formParameters, "UTF-8")
    val httpPost = new HttpPost("http://bicat.cultura-ede.nl/cgi-bin/bx.pl")
    httpPost.setEntity(entity)
    val response = httpclient.execute(httpPost, httpContext)
    EntityUtils.toString(response.getEntity)
  }

  protected[library] def getParametersForAuthorQuery(authorName: String, sid: String): List[BasicNameValuePair] =
    new BasicNameValuePair("qs", authorName) :: new BasicNameValuePair("sid", sid) :: fixedParamatersForAuthorsQuery

  protected[library] def getAuthorUpdatedWithLink(webPage: String, authorWithoutLink: Author): Author = {
    val singleLineAuthorFragmentPattern = """(?m)<td class="thsearch_wordlink">(.*?)</td>"""
    val multiLineAuthorFragmentPattern = """(?m)<td class="thsearch_wordlink">.*\n(.*)\n.*</td>"""
    val result = getAuthorUpdatedWithLink(webPage, singleLineAuthorFragmentPattern, authorWithoutLink)
    result match {
      case None => getAuthorUpdatedWithLink(webPage, multiLineAuthorFragmentPattern, authorWithoutLink) getOrElse new UnknownAuthor
      case _ => result getOrElse new UnknownAuthor
    }
  }

  protected[library] def getAuthorUpdatedWithLink(webPage: String, pattern: String, author: Author): Option[Author] = {
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
    if (author.like(authorFromWebPage)) {
      val linkWithAantalFieldSetTo60 = link.replaceFirst("aantal=10", "aantal=60")
      Some(Author(author, linkWithAantalFieldSetTo60))
    } else None
  }

  protected[library] def startBicatSessionAndReturnSid: String = {
    val httpget = new HttpGet(bicatStartOfSessionUrl)
    val response = httpclient.execute(httpget, httpContext)
    val page = EntityUtils.toString(response.getEntity)
    val pattern = """";sid=.*?;"""".r
    pattern.findFirstMatchIn(page).map(_ group 1).getOrElse("")
  }

  protected[library] def getBicatCookie: Cookie = {
    val cookies: Set[Cookie] = cookieStore.getCookies.toSet
    val bicatCookies = cookies filter (cookie => cookie.getName.startsWith("BICAT_SID"))
    bicatCookies.head
  }

  protected[library] def getBookPageAsHtmlByAuthor(author: Author): String = {
    val link = author.linkToListOfBooks
    Request.Get("http://bicat.cultura-ede.nl" + link).execute().returnContent().asString()
  }

  protected[library] def getBooksFromHtmlPage(bookPageAsHtml: String, author: Author): List[String] = {
    val patternString = """<a class="title" title="(.*?)""""
    val pattern = patternString.r
    val books = pattern.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
    books.length match {
      case 0 => {
        val p2 = """<meta xmlns:og="http://ogp.me/ns#" name="title" content="(.*?)"""".r
        p2.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
      }
      case _ => books
    }
  }

  val fixedParamatersForAuthorsQuery: List[BasicNameValuePair] = List(new BasicNameValuePair("zoek_knop", "Zoek"), new BasicNameValuePair("zl_v", "vest"), new BasicNameValuePair("nr", "8399")
    , new BasicNameValuePair("var", "portal"), new BasicNameValuePair("taal", "1"), new BasicNameValuePair("sn", "10"),
    new BasicNameValuePair("prt", "INTERNET"), new BasicNameValuePair("ingang", "01TS0"),
    new BasicNameValuePair("groepfx", "10"), new BasicNameValuePair("event", "search"), new BasicNameValuePair("dcat", "1"),
    new BasicNameValuePair("cdef", "002"), new BasicNameValuePair("aantal", "50")
  )

  def readTextFromUrl(url: String): String = {
    val content = Request.Get(url).execute().returnContent()
    content.asString()
  }

  def getNewBooks(myBooks: List[Book], booksFromWeb: List[Book]): List[Book] = {
    val candidates = booksFromWeb.toSet
    val booksWithStatusReadOrWontRead = myBooks.filter(book => book.status != Book.UNKNOWN)
    val newBooks = candidates -- booksWithStatusReadOrWontRead
    newBooks.toList
  }

}
