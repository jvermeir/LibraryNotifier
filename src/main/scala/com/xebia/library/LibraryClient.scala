package com.xebia.library

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

class LibraryClient {

  val bicatStartOfSessionUrl = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
  val cookieStore = new BasicCookieStore
  val httpContext = new BasicHttpContext
  val httpclient: HttpClient = new DefaultHttpClient
  httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
  httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

  val sid = startBicatSessionAndReturnSid
  val bicatCookie = getBicatCookie

  def getBooksByAuthor(authorToSearchFor:Author): List[Book] = {
    def author = updateAuthorWithLinkToBooks(authorToSearchFor)
    def bookpage = getBookPageAsHtmlByAuthor(author)
    def titles = getBooksFromHtmlPage(bookpage, author)
    def books = titles map {title => Book(author, title)}
    books
  }

  def getBooksForAuthors(authors:Map[String, Author]): List[Book] = {
    val x = authors.values map (author => getBooksFromHtmlPage(getBookPageAsHtmlByAuthor(author),author) map (book => Book(author, book)))
    (x flatten).toList
  }

  protected[library] def updateAuthorWithLinkToBooks(author:Author): Author = {
    val authorSearchResultPage = getResultOfSearchByAuthor(author.toFirstNameLastNameString)
    val authorWithLink = Author(author.firstName, author.lastName, getAuthorLinkFromAWebPage(authorSearchResultPage))
    authorWithLink
  }

  protected[library] def getResultOfSearchByAuthor(authorName: String): String = {
    val formParameters = getParametersForAuthorQuery(authorName, sid)
    val entity = new UrlEncodedFormEntity(formParameters, "UTF-8")
    val httpPost = new HttpPost("http://bicat.cultura-ede.nl/cgi-bin/bx.pl")
    httpPost.setEntity(entity)
    val response = httpclient.execute(httpPost, httpContext)
    EntityUtils.toString(response.getEntity)
  }

  protected[library] def getParametersForAuthorQuery(authorName:String, sid:String):List[BasicNameValuePair] =
    new BasicNameValuePair("qs", authorName) :: new BasicNameValuePair("sid", sid) :: LibraryClient.fixedParamatersForAuthorsQuery

  protected[library] def getAuthorLinkFromAWebPage(webPage: String): String = {
    val pattern = """(?m)<td class="thsearch_wordlink">.*\n(.*)\n.*</td>""".r
    val result = pattern
      .findFirstMatchIn(webPage)
      .map(_ group 1).getOrElse("")
    result
  }

  protected[library] def startBicatSessionAndReturnSid: String = {
    val httpget = new HttpGet(bicatStartOfSessionUrl)
    val response = httpclient.execute(httpget, httpContext)
    val page = EntityUtils.toString(response.getEntity)
    val pattern = """";sid=.*?;"""".r
    val sid = pattern.findFirstMatchIn(page).map(_ group 1).getOrElse("")
    sid
  }

  protected[library] def getBicatCookie: Cookie = {
    val cookies: Set[Cookie] = cookieStore.getCookies.toSet
    val bicatCookies = cookies filter (cookie => cookie.getName.startsWith("BICAT_SID"))
    bicatCookies.head
  }

  protected[library] def getBookPageAsHtmlByAuthor(author: Author): String = {
    val link = author.linkToListOfBooks
    val y = Request.Get("http://bicat.cultura-ede.nl" + link).execute().returnContent().asString()
    y
  }

  protected[library] def getBooksFromHtmlPage(bookPageAsHtml: String, author:Author): List[String] = {
    val patternString = """<a class="(?m)title" title="(.*?)".*\n<li><span class="vet">""" + author.toFirstNameLastNameString +  """</span>"""
    val pattern = patternString.r
    pattern.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
  }

  protected[library] def getBooksForAuthors(dataFileName:String): List[Book] = {
    val authors = AuthorParser.loadAuthorsFromFile(dataFileName)
    getBooksForAuthors(authors)
  }

  protected[library] def getAuthorFromAWebPage(webPage: String): Author = {
    val authorLink = getAuthorLinkFromAWebPage(webPage)
    val pattern = """<a href="(.*?)">(.*?)<""".r
    val link = pattern
      .findFirstMatchIn(authorLink)
      .map(_ group 1).getOrElse("")
    val authorAsString = pattern
      .findFirstMatchIn(authorLink)
      .map(_ group 2).getOrElse("")
    val authorWithoutWebLink = Author(authorAsString)
    // TODO: find out about extractor pattern
    val linkWithAantalFieldSetTo60=link.replaceFirst("aantal=10","aantal=60")
    Author(authorWithoutWebLink, linkWithAantalFieldSetTo60)
  }
}

object LibraryClient {

  val fixedParamatersForAuthorsQuery: List[BasicNameValuePair] = List(new BasicNameValuePair("zoek_knop", "Zoek"), new BasicNameValuePair("zl_v", "vest"), new BasicNameValuePair("nr", "8399")
    , new BasicNameValuePair("var", "portal"), new BasicNameValuePair("taal", "1"), new BasicNameValuePair("sn", "10"),
    new BasicNameValuePair("prt", "INTERNET"), new BasicNameValuePair("ingang", "01TS0"),
    new BasicNameValuePair("groepfx", "10"), new BasicNameValuePair("event", "search"), new BasicNameValuePair("dcat", "1"),
    new BasicNameValuePair("cdef", "002"), new BasicNameValuePair("aantal", "50")
  )

  def main(args: Array[String]) {
    val res = readTextFromUrl("http://localhost/")
    println("res: " + res)
  }

  def readTextFromUrl(url: String): String = {
    val content = Request.Get(url).execute().returnContent()
    content.asString()
  }
}