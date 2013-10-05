package com.library.service

import akka.actor.{ActorSystem, Props, Actor}
import spray.routing._
import com.library.{BookShelf, AuthorParser, Author, Config}
import spray.http.MediaTypes._
import com.library.service.ReloadActorProtocol.ReloadMessage

/**
 * Service allows access to the BookShelf over HTTP
 */

class LibraryServiceActor extends Actor with LibraryService {
  reloadBooksFromLibrary

  def actorRefFactory = context

  def receive = runRoute(libraryRoute)
}

trait LibraryService extends HttpService {
  lazy val bookShelf = Config.bookShelf

  def reloadBooksFromLibrary: Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
    implicit val system = ActorSystem("library")
    val reloadService = system.actorOf(Props[ReloadActor], "reload-service")
    reloadService ! new ReloadMessage(authors, bookShelf)
  }

  val libraryRoute =
    path("books") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            bookShelf.printAsHtml
          }
        }
      }
    } ~
      pathPrefix("book" / RestPath) {
        part =>
          path("") {
            complete {
              " he " + part + " ho"
            }
          }
      }
}

class ReloadActor extends Actor {
  // TODO: this Actor should probably create a new BookShelf instance, but then we need a way
  // to update the bookShelf used by the LibraryService

  import ReloadActorProtocol._

  def receive = {
    case ReloadMessage(authors: Map[String, Author], bookShelf: BookShelf) => reload(authors, bookShelf)
  }

  private def reload(authors: Map[String, Author], bookShelf: BookShelf): BookShelf = {
    bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
    bookShelf.write
    bookShelf
  }
}

object ReloadActorProtocol {

  case class ReloadMessage(authors: Map[String, Author], bookShelf: BookShelf)

}