package com.library

import scala.io.Source._
import java.io.{PrintWriter, File}
import scala.util.parsing.json.{JSON, JSONFormat, JSONArray, JSONObject}
import scala.language.postfixOps
import scala.util.Random

/**
 * A Book shelf contains books I've read or haven't read yet.
 */

trait BookShelf {
  def shouldReload: Boolean = !new File("dontReload").exists

  protected[library] lazy val books = scala.collection.mutable.Map[String, Book]()

  protected def read: Unit

  def write: Unit

  // TODO: sneaky init code below... Move to subclass??
  read

  def updateBooks(booksFromLibrary: Iterable[Book]): Unit = {
    val newBooks = booksFromLibrary filter (book => !books.contains(book.getKey))
    books ++= newBooks map (book => (book.getKey -> book))
    write
  }

  // TODO: why would this return a Map?
  def getBookFromShelf(bookToSearchFor: String): Map[String, Book] = books.filter(_.toString == bookToSearchFor).toMap

  def getBooksToRead: List[Book] = books.values.toList filter (_.status == Book.UNKNOWN)

  def getAllBooks: List[Book] = books.values.toList

  def setStatusForBook(book: Book, newStatus: String): Unit = {
    val newBook = book.setStatus(newStatus)
    books += (newBook.getKey -> newBook)
  }

  def add(book: Book): Unit = books += (book.getKey -> book)

  def emptyShelf: Unit = books.retain((k, v) => false)

  def printAsWishList: String = getBooksToRead.sortWith(lessThanForWishList(_, _)) map (book => printBookToWishListItem(book)) mkString ("\n")

  private def printBookToWishListItem(book: Book): String = book.author.lastName + ";" + book.author.firstName + ";" + book.title

  def printAsHtml: String = {
    val bookTableRowsAsString = getBooksToRead.sortWith(lessThanForWishList(_, _)) map (book => printBookToHtmlTableItem(book)) mkString ("\n")
    "<table>\n" + bookTableRowsAsString + "\n</table>"
  }

  def printAsJson: String = {
    val sortedBooks = getBooksToRead.sortWith(lessThanForWishList(_, _))
    val booksAsJSON = sortedBooks map (_.asJSONString)
    "{\"books\" : [{" + booksAsJSON.mkString("},\n{") + "}]}"
  }

  private def printBookToHtmlTableItem(book: Book): String = "<tr><td>" + book.author.lastName + "</td><td>" + book.author.firstName + "</td><td>" + book.title + "</td></tr>"

  private def lessThanForWishList(firstBook: Book, secondBook: Book): Boolean = {
    val lastNameLessOrEqual = firstBook.author.lastName <= secondBook.author.lastName
    val lastNamesEqual = firstBook.author.lastName == secondBook.author.lastName
    if (lastNameLessOrEqual && lastNamesEqual) {
      firstBook.title <= secondBook.title
    } else lastNameLessOrEqual
  }

  protected[library] def getRandomizedListOfBooks: List[Book] = Random.shuffle(getBooksToRead)

  def getRecommendations: List[Book] = recommendations.toList

  val recommendations = scala.collection.mutable.ListBuffer[Book]()

  def storeRecommendations(newRecommendations: List[Book]): Unit = recommendations ++= newRecommendations

  def printRecommendationsAsJson: String = {
    val booksAsJSON = getRecommendations map (_.asJSONString)
    "{\"books\" : [{" + booksAsJSON.mkString("},\n{") + "}]}"
  }

}

class FileBasedBookShelf(val storeFileName: String) extends BookShelf {

  override def read: Unit = {
    // TODO: remove this patch if we've moved to JSON
    if (storeFileName.endsWith("json")) {
       readFromJSONFile
    } else {
      emptyShelf
      if (new File(storeFileName).exists)
        books.++(readFromFile(storeFileName))
    }
  }

  override def write: Unit = writeBooksToFile(storeFileName, books.values.toList)

  private def readFromFile(fileName: String): Map[String, Book] = {
    emptyShelf
    val booksAsTextLines = fromFile(fileName).getLines()

    books ++= booksAsTextLines map (book => {
      val b = Book(book); (b.getKey -> b)
    })
    books.toMap
  }

  def readFromJSONFile: Map[String, Book] = {
    emptyShelf
    val booksAsText = fromFile(storeFileName).mkString

    val booksFromFile = for {
      Some(M(map)) <- List(JSON.parseFull(booksAsText))
      L(bookList) = map("books")
      book <- bookList
      myBook = Book.createFromParsedJSON(List(book))
    } yield myBook
    books ++= booksFromFile.map(book => book.getKey -> book)

    books.toMap
  }

  def writeBooksToFile:Unit = {
    val printWriter = new PrintWriter( new File(storeFileName))
    try {
      printWriter.print(printAsJson)
    } finally printWriter.close
  }

  private def writeBooksToFile(fileName: String, books: List[Book]): Unit = {
    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
      try {
        op(p)
      } finally {
        p.close()
      }
    }

    printToFile(new File(fileName))(line => {
      val sortedBooks = books.sortBy(_.toString)
      sortedBooks.foreach(line.println)
    })
  }
}
