package com.library

import scala.io.Source._

case class Author(val firstName: String, val lastName: String, val linkToListOfBooks: String)  {
  def equalsIgnoreLink(other:Author):Boolean = {
    firstName.equals(other.firstName) && lastName.equals(other.lastName)
  }
  def toLastNameCommaFirstNameString:String = lastName + ", " + firstName
  override def equals (that:Any):Boolean = {
    if (that.isInstanceOf[Author]) {
      val other = that.asInstanceOf[Author]
      other.toLastNameCommaFirstNameString.equals(toLastNameCommaFirstNameString)
    } else false
  }
  def like(that:Author):Boolean = toLastNameCommaFirstNameString.toLowerCase.startsWith(that.toLastNameCommaFirstNameString.toLowerCase)

  def asJSONString:String = {
    "\"author\" : {\"firstName\" : \"" + firstName +"\",\n" +
      "\"lastName\" : \"" + lastName +"\"}\n"
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

  def main(args: Array[String]): Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
    println(authors)
  }

}

class UnknownAuthor extends Author("Unknown", "Author", "")