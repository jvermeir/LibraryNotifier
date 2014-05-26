package com.library.service

import akka.actor._
import com.library._

class RecommendationActor(bookShelf: BookShelf) extends Actor with LogHelper {

  import RecommendationProtocol._
  import FindOutIfBookIsAvailableProtocol._

  implicit val system: ActorSystem = ActorSystem("Recommendations")
  val NUMBER_OF_RESULTS: Int = 5
  val NUMBER_OF_WORKERS: Int = 2
  val bookFinders = for (i <- 1 to NUMBER_OF_WORKERS) yield system.actorOf(Props[FindOutIfBookIsAvailableActor], "FindOutIfBookIsAvailableActor" + i)
  val result = scala.collection.mutable.MutableList[Book]()

  def receive = {
    case BookAvailableMessage(book) => {
      logger.info("Adding book: " + book)
      result += book
      if (result.length >= NUMBER_OF_RESULTS) {
        logger.info("found enough books")
        bookShelf.storeRecommendations(List().++(result))
        bookFinders map {
          actor => logger.info("Stop actor " + actor + " from " + this)
            system.stop(actor)
        }
        system.stop(self)
      } else {
        logger.info((NUMBER_OF_RESULTS - result.length) + " books to go")
      }

    }
    case GetRecommendationsMessage => {
      val randomizedListOfBooks: List[Book] = bookShelf.getRandomizedListOfBooks
      if (randomizedListOfBooks.size > 0) {
        val groupsOfBooks = List().++(randomizedListOfBooks grouped (randomizedListOfBooks.length / NUMBER_OF_WORKERS))
        for (i <- 0 until groupsOfBooks.length) {
          val groupOfBooks = groupsOfBooks(i)
          groupOfBooks.map(bookFinders(i) ! FindOutIfBookIsAvailableMessage(_))
        }
      }
    }
  }
}

object RecommendationProtocol {

  object GetRecommendationsMessage

  case class RecommendationResultMessage(recommendations: List[Book])

}

object FindOutIfBookIsAvailableProtocol {

  case class FindOutIfBookIsAvailableMessage(books: Book)

  case class BookAvailableMessage(book: Book)

  case class BookNotAvailableMessage(book: Book)

}

class FindOutIfBookIsAvailableActor extends Actor with LogHelper {

  import FindOutIfBookIsAvailableProtocol._

  val libraryClient = Config.libraryClient

  def receive = {
    case FindOutIfBookIsAvailableMessage(book) =>
      val available = libraryClient.isBookAvailable(book)
      logger.debug(self + "book: " + book + " available: " + available)
      if (available) sender ! BookAvailableMessage(book)
      else sender ! BookNotAvailableMessage(book)
  }
}
