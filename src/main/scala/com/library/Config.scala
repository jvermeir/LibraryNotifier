package com.library

/**
  * Configuration for dependencies and stuff
  */

object Config {
  var libraryClient:Library = new DutchPublicLibrary
  var bookShelf:BookShelf = new FileBasedBookShelf("data/boeken.dat")

  def reload:Unit = { }
}
