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

class CC[T] { def unapply(a:Any):Option[T] = Some(a.asInstanceOf[T]) }

object M extends CC[Map[String, Any]]
object L extends CC[List[Any]]
object S extends CC[String]
object D extends CC[Double]
object B extends CC[Boolean]

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
    val book = for {
      Some(M(map)) <- List(JSON.parseFull(bookAsString))
      M(book) = map
      M(authorMap) = book("author")
      S(firstName) = authorMap("firstName")
      S(lastName) = authorMap("lastName")
      S(title) = book("title")
      S(status) = book("status")
      S(link) = book("link")
    } yield (Book(Author(firstName,lastName), title, status, link))


    val book2 = JSON.parseFull(bookAsString) match {
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
    book(0)
  }
}

object DummyBook extends Book(Author("dummy, dummy"), "dummy", Book.UNKNOWN, "unknown")
