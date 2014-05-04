package com.library

import org.apache.http.client.fluent.Request
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.{BasicCookieStore, DefaultHttpClient}
import org.apache.http.client.methods.{ HttpPost, HttpGet}
import org.apache.http.client.params.{ClientPNames, CookiePolicy}
import org.apache.http.protocol.{BasicHttpContext}
import org.apache.http.client.protocol.ClientContext
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.cookie.Cookie
import scala.collection.JavaConversions._
import org.apache.http.util.EntityUtils
import scala.language.postfixOps
import org.apache.http.params.{HttpParams, BasicHttpParams}
import scala.io.Codec
import com.library.service.LogHelper

/**
 * All the stuff we need to connect to a web server, using Apache HTTP client.
 */
class MyHttpClient extends LogHelper{

  val bicatStartOfSessionUrl = "http://bicat.cultura-ede.nl/cgi-bin/bx.pl?taal=1&xdoit=y&groepfx=10&vestnr=8399&cdef=002"
  val cookieStore = new BasicCookieStore
  val httpContext = new BasicHttpContext
  lazy val httpclient = initHttpClient
  lazy val sid = startBicatSessionAndReturnSid
  lazy val bicatCookie = getBicatCookie

  def initHttpClient: HttpClient = {
    val client = new DefaultHttpClient
    client.getParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH)
    httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore)
    client
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

  protected[library] def getResultOfSearchByAuthor(authorName: String): String = {
    val formParameters = getParametersForAuthorQuery(authorName, sid)
    val entity = new UrlEncodedFormEntity(formParameters, "UTF-8")
    val httpPost = new HttpPost("http://bicat.cultura-ede.nl/cgi-bin/bx.pl")
    val httpParams: HttpParams = new BasicHttpParams
    httpParams.setParameter("Content-Type", "text/plain; charset=ISO-8859-15")
    val httpclient = new DefaultHttpClient(httpParams)
    httpPost.setEntity(entity)
    val response = httpclient.execute(httpPost, httpContext)
    EntityUtils.toString(response.getEntity)
  }

  protected[library] def getParametersForAuthorQuery(authorName: String, sid: String): List[BasicNameValuePair] =
    new BasicNameValuePair("qs", authorName) :: new BasicNameValuePair("sid", sid) :: fixedParametersForAuthorsQuery

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
    logger.debug("getBookPageAsHtmlByAuthor: " + result)
    result
  }

  def getBookPageAsHtmlFromBookUrl(book: Book): String = {
    logger.debug("isBookAvailable: " + book)
    val httpParams:HttpParams  = new BasicHttpParams
    httpParams.setParameter("Content-Type","text/plain; charset=ISO-8859-15")
    val httpclient = new DefaultHttpClient(httpParams)
    val url = "http://bicat.cultura-ede.nl" + book.link
    val localContext = new BasicHttpContext
    val httpget = new HttpGet(url)
    val response = httpclient.execute(httpget, localContext)
    val result = scala.io.Source.fromInputStream(response.getEntity.getContent)(Codec.ISO8859).mkString("")
    logger.debug("result: " + result)
    result
  }

}
