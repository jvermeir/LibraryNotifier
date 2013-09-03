package com.library.service

import akka.actor.Actor
import spray.routing._
import com.library.{AuthorParser, Config}

/**
 * Service allows access to the BookShelf over HTTP
 */

class LibraryServiceActor extends Actor with LibraryService {
  // TODO: this causes reload from library which is rather slow.
  // Find a way to run update in the background and refresh data later.
  start

  def actorRefFactory = context

  def receive = runRoute(libraryRoute)
}

trait LibraryService extends HttpService {
  lazy val bookShelf = Config.bookShelf

  def start: Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
    bookShelf.read
    bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
    bookShelf.write
  }

  val libraryRoute =
    path("books") {
      get {
        complete {
          bookShelf.printAsHtml
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