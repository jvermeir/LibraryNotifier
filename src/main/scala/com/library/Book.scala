package com.library

import scala.io.Source._
import java.io.File

case class Book (author:Author, title:String, status:String = Book.UNKNOWN) {
  override def toString = getKey + ";" + status

  def setStatus(newStatus:String):Book = new Book(author,title,newStatus)
  override def equals(o:Any):Boolean = {
    val other = o.asInstanceOf[Book]
    getKey == other.getKey
  }

  def getKey:String = author.lastName + ";" + author.firstName + ";" + title

  override def hashCode: Int = toString.hashCode

  def asJSONString:String = {
      author.asJSONString +
    ", \"title\" : \"" + title + "\", \"status\" : " + status + "\n"
  }
}

object Book {
  // TODO: this should be some type
  val UNKNOWN = "unknown"
  val READ = "read"
  val WONT_READ = "wontRead"

  def apply(bookAsString:String):Book = {
    val parts:Array[String]= bookAsString.split(";") map (part => part.trim)
    val status = if (parts.length>3) parts(3) else UNKNOWN
    Book(new Author (firstName = parts(1),lastName = parts(0),""),parts(2), status)
  }

}


