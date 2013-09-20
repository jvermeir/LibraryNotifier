package com.library.service

import akka.actor.Actor
import spray.routing._
import com.library.{AuthorParser, Config}
import spray.http.MediaTypes._

/**
 * Service allows access to the BookShelf over HTTP
 */

class LibraryServiceActor extends Actor with LibraryService {
  // TODO: this causes reload from library which is rather slow.
  /*
    plan A: - load data from file and return that as first version
            - start actor in background to update the database
            -> cache pattern? find a cache implementation in Akka?
   */
  try {
    println("before start")
    start
  } catch {
    case e: Exception => println("help")
  }
  println("started")

  def actorRefFactory = context

  def receive = runRoute(libraryRoute)
}

trait LibraryService extends HttpService {
  lazy val bookShelf = Config.bookShelf

  def start: Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
    //    bookShelf.read
    bookShelf.refreshBooksFromLibrary(Config.libraryClient, authors)
    bookShelf.write
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