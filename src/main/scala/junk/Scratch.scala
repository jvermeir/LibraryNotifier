package junk

import org.apache.http.message.BasicNameValuePair
import com.library._
import org.apache.http.client.fluent.Request

/**
 * Object to use for temporary exeperiments
 *
 */
object Scratch {

    val fixedParamatersForAuthorsQuery: List[BasicNameValuePair] = List(new BasicNameValuePair("zoek_knop", "Zoek"), new BasicNameValuePair("zl_v", "vest"), new BasicNameValuePair("nr", "8399")
      , new BasicNameValuePair("var", "portal"), new BasicNameValuePair("taal", "1"), new BasicNameValuePair("sn", "10"),
      new BasicNameValuePair("prt", "INTERNET"), new BasicNameValuePair("ingang", "01TS0"),
      new BasicNameValuePair("groepfx", "10"), new BasicNameValuePair("event", "search"), new BasicNameValuePair("dcat", "1"),
      new BasicNameValuePair("cdef", "002"), new BasicNameValuePair("aantal", "50")
    )

    def main(args: Array[String]) {
      val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
      println("authors: " + authors)

      val library = new DutchPublicLibrary
      val books = library.getBooksForAuthors(authors)
//      Book.writeBooksToFile("data/books.txt", books)
      println("books: " + books)
    }

    def readTextFromUrl(url: String): String = {
      val content = Request.Get(url).execute().returnContent()
      content.asString()
    }

    def getNewBooks(myBooks: List[Book], booksFromWeb: List[Book]): List[Book] = {
      val candidates = booksFromWeb.toSet
      val booksWithStatusReadOrWontRead = myBooks.filter(book => book.status != Book.UNKNOWN)
      val newBooks = candidates -- booksWithStatusReadOrWontRead
      newBooks.toList
    }
}

object MoreJunk {
  object LibraryThingy {
    def main(args: Array[String]) {
      val client = Config.libraryClient
//      val books = client.getBooksForAuthorsInFile("data/authors.dat")
//      for (book <- books) println(book)
    }
  }

}