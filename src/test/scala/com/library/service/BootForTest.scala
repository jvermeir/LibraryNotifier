package com.library.service

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.library._
import com.library.service.ReloadActorProtocol.ReloadMessage

object BootForTest extends App {

  private val HTTP_PORT: Int = 9181

  Config.libraryClient = new LibraryForTest
  Config.bookShelf = new FileBasedBookShelf("data/books.json")

  // TODO: Actor isn't restarted?
  implicit val system: ActorSystem = ActorSystem("on-spray-can")

  val service = system.actorOf(Props[LibraryServiceActor], "library-service")

  reloadBooksFromLibrary

  IO(Http) ! Http.Bind(service, interface = "192.168.10.129", port = HTTP_PORT)

  def reloadBooksFromLibrary: Unit = {
    val authors = Author.loadAuthorsFromFile("data/books.json")
    val reloadService = system.actorOf(Props[ReloadActor], "reload-service")
    reloadService ! new ReloadMessage(authors, Config.bookShelf)
  }

}

class ReloadActor extends Actor {
  // TODO: this Actor should probably create a new BookShelf instance, but then we need a way
  // to update the bookShelf used by the LibraryService
  // TODO: we're sharing an object here, change in two-way message.

  import ReloadActorProtocol._

  def receive = {
    case ReloadMessage(authors: Map[String, Author], bookShelf: BookShelf) => reload(authors, bookShelf)
  }

  private def reload(authors: Map[String, Author], bookShelf: BookShelf):Unit = {
    val libraryClient = Config.libraryClient
    val booksFromLibrary:Iterable[Book] = libraryClient.getBooksForAuthors(authors).values.flatten
    bookShelf.updateBooks(booksFromLibrary)
  }
}

object ReloadActorProtocol {

  case class ReloadMessage(authors: Map[String, Author], bookShelf: BookShelf)

}


