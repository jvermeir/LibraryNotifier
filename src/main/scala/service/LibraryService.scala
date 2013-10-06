package com.library.service

import akka.actor.{ActorSystem, Props, Actor}
import spray.routing._
import com.library.{BookShelf, AuthorParser, Author, Config}
import spray.http.MediaTypes._

/**
 * Service allows access to the BookShelf over HTTP
 */

class LibraryServiceActor extends Actor with LibraryService {

  def actorRefFactory = context

  def receive = runRoute(libraryRoute)
}

trait LibraryService extends HttpService {
  def bookShelf = Config.bookShelf

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
