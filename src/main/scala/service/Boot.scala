package com.library.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.library.{FileBasedBookShelf, DutchPublicLibrary, Config}

object Boot extends App {

  private val HTTP_PORT: Int = 9181
  Config.libraryClient = new DutchPublicLibrary
  Config.bookShelf = new FileBasedBookShelf("data/boeken.dat")

  // TODO: Actor isn't restarted?
  implicit val system = ActorSystem("on-spray-can")

  val service = system.actorOf(Props[LibraryServiceActor], "library-service")

  IO(Http) ! Http.Bind(service, interface = "localhost", port = HTTP_PORT)
}