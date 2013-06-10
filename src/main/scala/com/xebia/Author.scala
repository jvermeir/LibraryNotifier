package com.xebia


case class Author(val firstName: String, val lastName: String, val linkToListOfBooks: String)  {
  def equalsIgnoreLink(other:Author):Boolean = {
    firstName.equals(other.firstName) && lastName.equals(other.lastName)
  }
  def toFirstNameLastNameString:String = lastName + ", " + firstName
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

  def replaceSid(author:Author, newSid:String): Author = {
    val newLink = author.linkToListOfBooks.replaceAll(";sid=.*?;", ";sid="+newSid+";")
    Author(author.firstName, author.lastName, newLink)
  }

  def main(args: Array[String]): Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.txt")
    println(authors)
  }
}