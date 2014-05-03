package com.library

import org.apache.commons.io.FileUtils

import scala.io.Source._
import java.io.File
import scala.util.parsing.json.JSON
import scala.language.postfixOps


case class Author(val firstName: String, val lastName: String, val linkToListOfBooks: String) {
  def equalsIgnoreLink(other: Author): Boolean = {
    firstName.equals(other.firstName) && lastName.equals(other.lastName)
  }

  def toLastNameCommaFirstNameString: String = lastName + ", " + firstName

  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[Author]) {
      val other = that.asInstanceOf[Author]
      other.toLastNameCommaFirstNameString.equals(toLastNameCommaFirstNameString)
    } else false
  }

  def like(that: Author): Boolean = toLastNameCommaFirstNameString.toLowerCase.startsWith(that.toLastNameCommaFirstNameString.toLowerCase)

  def asJSONString: String = {
    "\"author\" : {\"firstName\" : \"" + firstName + "\",\n" +
      "\"lastName\" : \"" + lastName + "\",\n" +
      "\"link\" : \"" + linkToListOfBooks + "\"}\n"
  }
}

object Author {

  def apply(authorAsString: String): Author = {
    val parenthesisPattern = """(.*),(.*)\(.*""".r
    val commaPattern = "(.*),(.*)".r
    val blankPattern = "(.*) (.*)".r
    authorAsString match {
      case parenthesisPattern(lastName, firstName) => Author(firstName.trim, lastName.trim, "")
      case commaPattern(lastName, firstName) => Author(firstName.trim, lastName.trim, "")
      case blankPattern(firstName, lastName) => Author(firstName.trim, lastName.trim, "")
      case _ => Author("", authorAsString.trim, "")
    }
  }

  def apply(author: Author, linkToListOfBooks: String): Author = {
    Author(author.firstName, author.lastName, linkToListOfBooks)
  }

  def apply(firstName: String, lastName: String): Author = {
    Author(firstName, lastName, "")
  }

  def fromJSONString(authorAsJSONString: String): Author =
    createFromParsedJSON(List(JSON.parseFull(authorAsJSONString).get))

  def createFromParsedJSON(jsonObject: List[Any]): Author = {
    val author = for {
      M(author) <- jsonObject
      M(authorMap) = author("author")
      S(firstName) = authorMap("firstName")
      S(lastName) = authorMap("lastName")
      S(link) = authorMap("link")
    } yield Author(firstName, lastName, link)
    author(0)
  }

  def authorsAsJSONString(authors:Map[String, Author]):String = {
    val authorsAsJSON = authors.values.map(_.asJSONString)
    """{"authors" : [{""" + authorsAsJSON.mkString("},\n{") + """}]}"""
  }

  def parseAuthorsFromListOfStrings(authors: List[String]):Map[String, Author] = {
    val authorList:List[Author] = authors map (Author(_))
    val lastNameList =  authorList map (_.lastName)
    (lastNameList zip authorList).toMap
  }

  def loadAuthorsFromFile(fileName:String): Map[String, Author] = {
    val authorsAsText = FileUtils.readFileToString(new File(fileName))
    parseAuthorsFromListOfStrings(authorsAsText.split("\n").toList.filter(_.trim.length>0))
  }

  def loadAuthorsFromJSONFile(fileName:String): Map[String, Author] = {
    val authorsAsText = fromFile(fileName).mkString
    val authors = getAuthorsFromJSON(authorsAsText)
    val result = authors.map(author => author.toLastNameCommaFirstNameString -> author)
    result.toMap
  }

  def getAuthorsFromJSON(authorsAsText:String):List[Author] = {
    for {
      Some(M(map)) <- List(JSON.parseFull(authorsAsText))
      L(authorList) = map("authors")
      author <- authorList
      myAuthor = Author.createFromParsedJSON(List(author))
    } yield myAuthor
  }


}

class UnknownAuthor extends Author("Unknown", "Author", "")