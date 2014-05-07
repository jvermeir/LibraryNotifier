package com.library

import scala.io.Source._
import java.io.{PrintWriter, File}
import scala.util.parsing.json.JSON
import scala.language.postfixOps
import scala.util.Random
import com.library.service.LogHelper

/**
 * A Book shelf contains books I've read or haven't read yet.
 */

trait BookShelf extends LogHelper {
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

  def getBooksToRead: List[Book] = books.values.toList filter (_.status == Book.UNKNOWN)

  def getAllBooks: List[Book] = books.values.toList
  def findBook(key:String):Book = books.get(key).getOrElse {throw new BookNotFoundException(key)}

  def setStatusForBook(book: Book, newStatus: String): Unit = {
    val newBook = book.setStatus(newStatus)
    books += (newBook.getKey -> newBook)
  }

  def setLinkForBook(book: Book, newLink: String): Unit = {
    val newBook = book.setLink(newLink)
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

  def printAsJson: String = printAsJson(lessThanForWishList, includeAll)
  def printBooksWithUnknownStatusAsJson: String = printAsJson(lessThanForWishList, includeUnknownBooks)
  def includeAll(book:Book):Boolean = true
  def includeUnknownBooks(book:Book):Boolean = book.status == Book.UNKNOWN

  def printAsJson(lessThan: (Book, Book) => Boolean, include: Book => Boolean) = {
    val booksToPrint = books.values.toList filter (include(_))
    val sortedBooks = booksToPrint sortWith(lessThan(_, _))
    val booksAsJSON = sortedBooks map (_.asJSONString)
    val result = "{\"books\" : [{" + booksAsJSON.mkString("},\n{") + "}]}"
    logger.debug("return: " + result)
    result
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

  def getBooksFromJSON(booksAsText: String): List[Book] =
    for {
      Some(M(map)) <- List(JSON.parseFull(booksAsText))
      L(bookList) = map("books")
      book <- bookList
      myBook = Book.createFromParsedJSON(List(book))
    } yield myBook

  // TODO: when to call?
  def getNewBooks(booksOnShelf: List[Book], booksFromWeb: List[Book]): List[Book] = {
    val candidates = booksFromWeb.toSet
    val booksWithStatusReadOrWontRead = booksOnShelf.filter(book => book.status != Book.UNKNOWN)
    val newBooks = candidates -- booksWithStatusReadOrWontRead
    newBooks.toList
  }
}

class FileBasedBookShelf(val storeFileName: String) extends BookShelf {

  override def read: Unit = {
    emptyShelf
    val booksAsText = fromFile(storeFileName).mkString
    val booksFromFile = getBooksFromJSON(booksAsText)
    books ++= booksFromFile.map(book => book.getKey -> book)
    books.toMap
  }

  override def write: Unit = {
    val printWriter = new PrintWriter(new File(storeFileName))
    try {
      printWriter.print(printAsJson)
    } finally printWriter.close
  }

}
