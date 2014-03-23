package com.library

object X extends App {
  val libraryClient = new DutchPublicLibrary
  libraryClient.startBicatSessionAndReturnSid

  val data = libraryClient.getResultOfSearchByAuthor("Brown, Dan")
  println("data init'd: " + data)
  println("index of: " + data.indexOf("Brown, Dan"))
}
