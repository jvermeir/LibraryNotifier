package com.library.service

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.library._
import com.library.service.ReloadActorProtocol.ReloadMessage
import com.library.service.RecommendationProtocol.GetRecommendationsMessage
import scala.concurrent.{ExecutionContext, Await}
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import akka.pattern.ask
import scala.language.postfixOps
import ExecutionContext.Implicits.global
import akka.util.Timeout

object Boot extends App  {

  private val HTTP_PORT: Int = 9181
  Config.libraryClient = new DutchPublicLibrary
  Config.bookShelf = new FileBasedBookShelf("data/boeken.dat")

  // TODO: Actor isn't restarted?
  implicit val system: ActorSystem = ActorSystem("on-spray-can")

  val libraryService = system.actorOf(Props[LibraryServiceActor], "library-service")
  val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")

  getRecommendations
//  reloadBooksFromLibrary

  IO(Http) ! Http.Bind(libraryService, interface = "0.0.0.0", port = HTTP_PORT)

  def getRecommendations: Unit = {
    val recommendationService = system.actorOf(Props(new RecommendationActor(Config.bookShelf)), "recommendation-service")
    recommendationService ! GetRecommendationsMessage
  }
  
  def reloadBooksFromLibrary: Unit = {
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


