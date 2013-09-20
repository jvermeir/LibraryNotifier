package com.library.service

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import com.library.{FileBasedBookShelf, DutchPublicLibrary, Config}

object Boot extends App {

  Config.libraryClient = new DutchPublicLibrary
  Config.bookShelf = new FileBasedBookShelf("data/boeken.dat")

  // TODO: Actor isn't restarted?
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[LibraryServiceActor], "demo-service")

  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 9181)
}