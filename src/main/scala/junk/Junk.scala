package junk

import com.library.{Author, DutchPublicLibrary}

class Euro(val amount:Int) {
  override def toString:String = "Euro: " + amount
  def +(other: Euro):Euro = new Euro(amount + other.amount)
}

class Dollar(val amount:Int) {
  override def toString:String = "Dollar: " + amount
  def +(other: Dollar):Dollar = new Dollar(amount + other.amount)
}

object Dollar {
  implicit  def dollarToEuro(dollar:Dollar):Euro = new Euro(dollar.amount)
}

object Test extends App {
  println("hello world")
  val e=new Euro(10)
  val d=new Dollar(5)
  println(e + " " + d)
  val ee = e + new Euro(11)
  println("ee: " + ee)
  val ed = e + d
  println("ed: " + ed)
}

object Junk {
  def main(args: Array[String]) = {
    val libraryClient = new DutchPublicLibrary
    val data2 = libraryClient.getBooksByAuthor(new Author("paul","harland",""))
    //    val data = libraryClient.getBooksByAuthor(new Author("Neil","Gaiman",""))
    println(data2)
    //val data = FileUtils.readFileToString(new File("data/walker.html"))
    //    val author = libraryClient.getAuthorUpdatedWithLink(data, new Author("Karen Thompson", "Walker",""))
    //    println(author)
    //    val books = libraryClient.getBooksByAuthor(author)
    //    println("books.size: " + books.size)
    //    println(books)
    ////    val data2 = FileUtils.readFileToString(new File("data/gaiman.html"))
    //    val booksByNeilGaiman = libraryClient.getBooksByAuthor(Author("Neil", "Gaiman",""))
    //    println(booksByNeilGaiman)
    //    val booksByKateWalker = libraryClient.getBooksByAuthor(Author("Kate", "Walker",""))
    //    println("Kate's books: " + booksByKateWalker)
  }
}