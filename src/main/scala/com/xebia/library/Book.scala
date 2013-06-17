package com.xebia.library

import scala.io.Source._
import java.io.File

case class Book (val author:Author, val title:String) {
  override def toString = author.lastName + ";" + author.firstName + ";" + title
}

object Book {
  def apply(bookAsString:String):Book = {
    val parts:Array[String]= bookAsString.split(";") map (part => part.trim)
    Book(new Author (firstName = parts(1),lastName = parts(0),""),parts(2))
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


