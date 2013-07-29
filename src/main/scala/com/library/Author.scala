package com.library

import scala.io.Source._

case class Author(val firstName: String, val lastName: String, val linkToListOfBooks: String)  {
  def equalsIgnoreLink(other:Author):Boolean = {
    firstName.equals(other.firstName) && lastName.equals(other.lastName)
  }
  def toFirstNameLastNameString:String = lastName + ", " + firstName
  override def equals (that:Any):Boolean = {
    if (that.isInstanceOf[Author]) {
      val other = that.asInstanceOf[Author]
      other.toFirstNameLastNameString.equals(toFirstNameLastNameString)
    } else false
  }
  def like(that:Author):Boolean = toFirstNameLastNameString.toLowerCase.startsWith(that.toFirstNameLastNameString.toLowerCase)
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
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.txt")
    println(authors)
  }
}

class UnknownAuthor extends Author("Unknown", "Author", "")