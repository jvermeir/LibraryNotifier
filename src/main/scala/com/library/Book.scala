package com.library

import scala.io.Source._
import java.io.File

case class Book (author:Author, title:String, status:String = Book.UNKNOWN) {
  override def toString = author.lastName + ";" + author.firstName + ";" + title
  def setStatus(newStatus:String):Book = new Book(author,title,newStatus)
  override def equals(o:Any):Boolean = {
    val other = o.asInstanceOf[Book]
    toString == other.toString
  }

  override def hashCode: Int = toString.hashCode
}

object Book {
  val UNKNOWN = "unknown"
  val READ = "read"
  val WONT_READ = "wontRead"

  def apply(bookAsString:String):Book = {
    val parts:Array[String]= bookAsString.split(";") map (part => part.trim)
    val status = if (parts.length>3) parts(3) else UNKNOWN
    Book(new Author (firstName = parts(1),lastName = parts(0),""),parts(2), status)
  }

  def readFromFile(fileName:String):List[Book] = {
    val booksAsTextLines =  fromFile(fileName).getLines()
    val books = booksAsTextLines map { book => Book(book)}
    books.toList
  }

  def writeBooksToFile(fileName:String, books:List[Book]):Unit = {
    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
      try { op(p) } finally { p.close() }
    }

    printToFile(new File(fileName))(line => {
      books.foreach(line.println)
    })
  }
}


