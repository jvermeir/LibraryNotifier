package com.xebia

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

object LibraryClient {
  val bicatStartOfSessionUrl = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
  val cookieStore = new BasicCookieStore
  val httpContext = new BasicHttpContext
  val httpclient: HttpClient = new DefaultHttpClient
  httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
  httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
  val fixedParamatersForAuthorsQuery: List[BasicNameValuePair] = List(new BasicNameValuePair("zoek_knop", "Zoek"), new BasicNameValuePair("zl_v", "vest"), new BasicNameValuePair("nr", "8399")
    , new BasicNameValuePair("var", "portal"), new BasicNameValuePair("taal", "1"), new BasicNameValuePair("sn", "10"),
    new BasicNameValuePair("prt", "INTERNET"), new BasicNameValuePair("ingang", "01TS0"),
    new BasicNameValuePair("groepfx", "10"), new BasicNameValuePair("event", "search"), new BasicNameValuePair("dcat", "1"),
    new BasicNameValuePair("cdef", "002"), new BasicNameValuePair("aantal", "10")
  )

  def getAuthorFromAWebPage(webPage: String): Author = {
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
    Author(authorWithoutWebLink, link)
  }

  def getAuthorLinkFromAWebPage(webPage: String): String = {
    val pattern = """(?m)<td class="thsearch_wordlink">.*\n(.*)\n.*</td>""".r
    pattern
      .findFirstMatchIn(webPage)
      .map(_ group 1).getOrElse("")
  }

  def startBicatSession: Unit = {
    val httpget = new HttpGet(bicatStartOfSessionUrl)
    httpclient.execute(httpget, httpContext)
    httpget.abort
  }

  def getContentsOfSearchResult(authorName: String): String = {
    val formParameters = new BasicNameValuePair("qs", authorName) :: new BasicNameValuePair("sid", "eb485df0-5d61-478e-a1fa-9c45cf966cd1") :: fixedParamatersForAuthorsQuery
    val entity = new UrlEncodedFormEntity(formParameters, "UTF-8")
    val httpPost = new HttpPost("http://bicat.cultura-ede.nl/cgi-bin/bx.pl")
    httpPost.setEntity(entity)
    val response = httpclient.execute(httpPost, httpContext)
    EntityUtils.toString(response.getEntity)
  }

  def getBicatCookie: Cookie = {
    val cookies: Set[Cookie] = cookieStore.getCookies.toSet
    val bicatCookies = cookies filter (cookie => cookie.getName.startsWith("BICAT_SID"))
    bicatCookies.head
  }

  def getBookPageAsHtmlByAuthor(author: Author): String = {
    Request.Get("http://bicat.cultura-ede.nl" + author.linkToListOfBooks).execute().returnContent().asString()
  }

  def getBooksFromHtmlPage(bookPageAsHtml: String, author:Author): List[String] = {
    val patternString = """<a class="(?m)title" title="(.*?)".*\n<li><span class="vet">""" + author.toFirstNameLastNameString +  """</span>"""
    val pattern = patternString.r
    pattern.findAllMatchIn(bookPageAsHtml).map(_ group 1).toSet.toList
  }

  def main(args: Array[String]) {
    val res = readTextFromUrl("http://localhost/")
    println("res: " + res)
  }

  def readTextFromUrl(url: String): String = {
    val content = Request.Get(url).execute().returnContent()
    content.asString()
  }
}
