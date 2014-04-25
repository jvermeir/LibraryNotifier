package com.library

import scala.util.parsing.json.JSON

case class Book(author: Author, title: String, status: String = Book.UNKNOWN, link: String = "unknown") {

  override def toString = getKey + ";" + status

  def setStatus(newStatus: String): Book = new Book(author, title, newStatus)

  override def equals(o: Any): Boolean = {
    val other = o.asInstanceOf[Book]
    getKey == other.getKey
  }

  def getKey: String = author.lastName + ";" + author.firstName + ";" + title

  override def hashCode: Int = toString.hashCode

  def asJSONString: String = {
    author.asJSONString +
      ", \"title\" : \"" + title + "\", \"status\" : " + status +
      ", \"link\" : \"" + link + "\"" + "\n"
  }

  lazy val available = isAvailable

  private def isAvailable: Boolean = Config.libraryClient.isBookAvailable(this)

}

object Book {
  // TODO: this should be some type
  val UNKNOWN = "unknown"
  val READ = "read"
  val WONT_READ = "wontRead"

  def apply(bookAsString: String): Book = {
    val parts: Array[String] = bookAsString.split(";") map (part => part.trim)
    val status = if (parts.length > 3) parts(3) else UNKNOWN
    Book(new Author(firstName = parts(1), lastName = parts(0), ""), parts(2), status)
  }

  // Temporary to help refactoring
  def createFromJSONString(bookAsString: String): Book = {
    val book = JSON.parseFull(bookAsString) match {
      case Some(x) => {
        val m = x.asInstanceOf[Map[String, Any]]
        val a = m("author").asInstanceOf[Map[String, String]]
        val firstName = a("firstName")
        val lastName = a("lastName")
        val author = Author(firstName, lastName)
        val title = m("title").asInstanceOf[String]
        val link = m("link").asInstanceOf[String]
        Book(author, title, link)
      }
      case None => DummyBook
    }
    book
  }
}

object DummyBook extends Book(Author("dummy, dummy"), "dummy", Book.UNKNOWN, "unknown")
