package com.library

import scala.io.Source._
import java.io.File

/**
 * Books that are on my book shelf
 */

trait BookShelf {
  protected[library] val books:scala.collection.mutable.Map[String, Book] = scala.collection.mutable.Map[String, Book]()
  def read:Unit
  def write:Unit

  def getBookFromShelf(bookToSearchFor:String):Map[String, Book] = books.filter( _.toString == bookToSearchFor).toMap

  def getBooksToRead: List[Book] = books.values.toList filter(_.status == Book.UNKNOWN)

  def getAllBooks:List[Book]=books.values.toList

  def refreshBooksFromLibrary(library: Library, authors: Map[String, Author]) {
    val booksFromLibrary:Iterable[Book] = library.getBooksForAuthors(authors).values.flatten
    val newBooks = booksFromLibrary filter (book => !books.contains(book.getKey) )
    books ++= newBooks map (book => (book.getKey -> book))
    // TODO: remove books that are not available anymore??
  }

  def setStatusForBook(book:Book, newStatus:String):Unit = {
    val newBook = book.setStatus(newStatus)
    books += (newBook.getKey -> newBook)
  }

  def add(book:Book):Unit = books += (book.getKey -> book)

  def emptyShelf:Unit = books.retain((k,v) => false)

  def printAsWishList:String = getBooksToRead.sortWith(lessThanForWishList(_,_)) map (book => printBookToWishListItem(book)) mkString("\n")

  private def printBookToWishListItem(book: Book): String = book.author.lastName + ";" + book.author.firstName + ";"  + book.title

  private def lessThanForWishList(firstBook:Book, secondBook:Book):Boolean = {
    val lastNameLessOrEqualThan = firstBook.author.lastName <= secondBook.author.lastName
    if (lastNameLessOrEqualThan && firstBook.author.lastName == secondBook.author.lastName) {
      firstBook.title <= secondBook.title
    } else lastNameLessOrEqualThan
  }

}

class FileBasedBookShelf(val storeFileName:String) extends BookShelf {

  read

  override def read:Unit = books.++(readFromFile(storeFileName))

  override def write:Unit = writeBooksToFile(storeFileName, books.values.toList)

  private def readFromFile(fileName:String):Map[String, Book] = {
    emptyShelf
    val booksAsTextLines =  fromFile(fileName).getLines()
    books ++= booksAsTextLines map (book => {val b = Book(book); (b.getKey -> b) })
    books.toMap
  }

  private def writeBooksToFile(fileName:String, books:List[Book]):Unit = {
    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
      try { op(p) } finally { p.close() }
    }

    printToFile(new File(fileName))(line => {
      books.foreach(line.println)
    })
  }
}