package com.library.service

import akka.actor.Actor
import spray.routing._
import com.library.Config
import spray.http.MediaTypes._

/**
 * Service allows access to the BookShelf over HTTP and returns a JSON object.
 */

class LibraryServiceActor extends Actor with LibraryService {

  def actorRefFactory = context

  def receive = runRoute(libraryRoute)
}

trait LibraryService extends HttpService with LogHelper {
  def bookShelf = Config.bookShelf

  val libraryRoute =
    path("books") {
      logger.info("handling 'books' request")
      get {
        respondWithMediaType(`application/json`) {
          complete {
            bookShelf.printBooksWithUnknownStatusAsJson
          }
        }
      }
    } ~
      path("allbooks") {
        logger.info("handling 'allbooks' request")
        get {
          respondWithMediaType(`application/json`) {
            complete {
              bookShelf.printAsJson
            }
          }
        }
      } ~
      path("recommendations") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              bookShelf.printRecommendationsAsJson
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
