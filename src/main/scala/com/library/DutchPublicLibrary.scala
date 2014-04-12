package com.library

import org.apache.http.client.fluent.Request
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http.client.methods.{HttpHead, HttpPost, HttpGet}
import org.apache.http.client.params.{ClientPNames, CookiePolicy}
import org.apache.http.protocol.{HttpContext, BasicHttpContext}
import org.apache.http.client.protocol.ClientContext
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.cookie.Cookie
import scala.collection.JavaConversions._
import org.apache.http.util.EntityUtils
import scala.language.postfixOps
import org.apache.http.params.{HttpParams, HttpConnectionParams, BasicHttpParams}
import scala.io.Codec
import scala.Some
import scala.util.matching.Regex
import com.library.service.LogHelper

/**
 * Access the website for the public library in Ede to find out if there are any new books by authors of interest.
 */

class DutchPublicLibrary extends Library with LogHelper {

  val bicatStartOfSessionUrl = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
  val cookieStore = new BasicCookieStore
  val httpContext = new BasicHttpContext
  val httpclient: HttpClient = new DefaultHttpClient
  httpclient.getParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH)
  httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore)

  lazy val sid = startBicatSessionAndReturnSid
  lazy val bicatCookie = getBicatCookie

  def getBooksByAuthor(authorToSearchFor: Author): List[Book] = {
    logger.info("Get books for: " + authorToSearchFor)
    val author = updateAuthorWithLinkToBooks(authorToSearchFor)
    val result = if (author.like(authorToSearchFor)) {
      val bookpage = getBookPageAsHtmlByAuthor(author)
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
    val authorSearchResultPage = getResultOfSearchByAuthor(author.toFirstNameLastNameString)
    getAuthorUpdatedWithLink(authorSearchResultPage, author)
  }

  protected[library] def getResultOfSearchByAuthor(authorName: String): String = {
    val formParameters = getParametersForAuthorQuery(authorName, sid)
    val entity = new UrlEncodedFormEntity(formParameters, "UTF-8")
    val httpPost = new HttpPost("http://bicat.cultura-ede.nl/cgi-bin/bx.pl")
    val httpParams:HttpParams  = new BasicHttpParams
    httpParams.setParameter("Content-Type","text/plain; charset=ISO-8859-15")
    val httpclient = new DefaultHttpClient(httpParams)
    httpPost.setEntity(entity)
    val response = httpclient.execute(httpPost, httpContext)
    EntityUtils.toString(response.getEntity)
  }

  protected[library] def getParametersForAuthorQuery(authorName: String, sid: String): List[BasicNameValuePair] =
    new BasicNameValuePair("qs", authorName) :: new BasicNameValuePair("sid", sid) :: fixedParametersForAuthorsQuery

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
    logger.debug("getBookPageAsHtmlByAuthor: " + author)
    val link = author.linkToListOfBooks
    val httpParams:HttpParams  = new BasicHttpParams
    httpParams.setParameter("Content-Type","text/plain; charset=ISO-8859-15")
    val httpclient = new DefaultHttpClient(httpParams)
    val url = "http://bicat.cultura-ede.nl" + link
    val localContext = new BasicHttpContext
    val httpget = new HttpGet(url)
    val response = httpclient.execute(httpget, localContext)
    val result = scala.io.Source.fromInputStream(response.getEntity.getContent)(Codec.ISO8859).mkString("")
    //Request.Get("http://bicat.cultura-ede.nl" + link).execute().returnContent().asString()
    logger.debug("getBookPageAsHtmlByAuthor: " + result)
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

  private def results(a:Regex.Match):(String, String) = {
    (a group 1, a group 2)
  }
  private def results1(a:Regex.Match):(String, String) = {
    (a group 1, "")
  }

  val fixedParametersForAuthorsQuery: List[BasicNameValuePair] = List(new BasicNameValuePair("zoek_knop", "Zoek"), new BasicNameValuePair("zl_v", "vest"), new BasicNameValuePair("nr", "8399")
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

  /*
  check the books page, return true if it contains a line with 'BeschikbaarBeschikbaar' and 'Ede Centrale'
<div class="exemplaar_row">
<div class="exemplaar_stat">
<img class="staticons" alt="Beschikbaar" title="Beschikbaar" src="/images/statgroen.gif">
Beschikbaar
</div>
<div class="exemplaar_vest">Ede Centrale</div>
<div class="exemplaar_kast">Spannend & actief Thriller KAVA</div>
</div>

link to a book from the page of books for an author:
<a href="/cgi-bin/bx.pl?dcat=1;wzstype=;extsdef=01;event=tdetail;wzsrc=;woord=Janssen%2C%20Roel;titcode=342879;rubplus=TS001;vv=NJ;vestfiltgrp=;sid=a30bcdc7-1ea8-4ea7-b85b-8d68c7ada131;vestnr=8399;prt=INTERNET;sn=11;fmt=xml;var=portal"

 title="De euro : twintig jaar na het verdrag van Maastricht" class="title">De euro : twintig jaar na het verdrag van Maastricht</a>
   */
  override def isBookAvailable(book: Book): Boolean = true

}
