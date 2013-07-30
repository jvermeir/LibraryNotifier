package com.library

import scala.io.Source._
import java.io.File
import scala.collection.mutable.Set

/**
 * Books that are on my book shelf
 */

trait BookShelf {
  protected[library] val books:Set[Book] = Set[Book]()
  def read:Unit
  def write:Unit
  def getBookFromShelf(bookToSearchFor:String):Set[Book] = books.filter( _.toString == bookToSearchFor)
  def getUnreadBooks:List[Book] = List()
  def getAllBooks:List[Book]=books.toList
  def refreshBooksFromLibrary(library:Library, authors:Map[String, Author]):Unit = {}
}

class FileBasedBookShelf(val storeFileName:String) extends BookShelf {

  override def read:Unit = books.++(readFromFile(storeFileName))

  override def write:Unit = writeBooksToFile(storeFileName, books.toList)

  private def readFromFile(fileName:String):Set[Book] = {
    val booksAsTextLines =  fromFile(fileName).getLines()
    booksAsTextLines map { book => books.add(Book(book))}
    books
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