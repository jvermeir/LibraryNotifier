package com.library.service

import akka.actor.{Props, ActorSystem, Actor}
import com.library._

class RecommendationActor(bookShelf: BookShelf) extends Actor {

  import RecommendationProtocol._
  import FindOutIfBookIsAvailableProtocol._

  implicit val system: ActorSystem = ActorSystem("Recommendations")
  val NUMBER_OF_RESULTS: Int = 5
  val NUMBER_OF_WORKERS: Int = 2
  val bookFinders = for (i <- 1 until NUMBER_OF_WORKERS) yield system.actorOf(Props[FindOutIfBookIsAvailableActor], "Recommendations" + i)
  val result = scala.collection.mutable.MutableList[Book]()

  def receive = {
    case BookAvailableMessage(book) => {
      result += book
      if (result.length >= NUMBER_OF_RESULTS) {
        bookShelf.storeRecommendations(List().++(result))
        //        sender ! RecommendationResultMessage(List().++(result))
        context.stop(self)
      }
    }
    case GetRecommendationsMessage => {
      val randomizedListOfBooks: List[Book] = bookShelf.getRandomizedListOfBooks
      if (randomizedListOfBooks.size > 0) {
        val groupsOfBooks = List().++(randomizedListOfBooks grouped (randomizedListOfBooks.length / NUMBER_OF_WORKERS))
        for (i <- 1 until groupsOfBooks.length)
          bookFinders.map(_ ! FindOutIfBookIsAvailableMessage(groupsOfBooks(i)))
      }
    }
  }
}

object RecommendationProtocol {

  object GetRecommendationsMessage

  case class RecommendationResultMessage(recommendations: List[Book])

}

object FindOutIfBookIsAvailableProtocol {

  case class FindOutIfBookIsAvailableMessage(books: List[Book])

  case class BookAvailableMessage(book: Book)

  case class BookNotAvailableMessage(book: Book)

}

class FindOutIfBookIsAvailableActor extends Actor {

  import FindOutIfBookIsAvailableProtocol._

  def receive = {
    case FindOutIfBookIsAvailableMessage(books) =>
      for (book <- books)
        if (isBookAvailable(book)) sender ! BookAvailableMessage(book)
        else sender ! BookNotAvailableMessage(book)
  }

  private def isBookAvailable(book: Book): Boolean = {
    val libraryClient = Config.libraryClient
    libraryClient.isBookAvailable(book)
  }
}
