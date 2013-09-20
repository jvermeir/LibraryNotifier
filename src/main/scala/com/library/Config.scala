package com.library

/**
  * Configuration for dependencies and stuff
  */

object Config {
  // TODO: ugly init is necessary because bookshelf tries to load a file

  var libraryClient:Library = null // = new DutchPublicLibrary
  var bookShelf:BookShelf = null // = new FileBasedBookShelf("data/boeken.dat")

  def reload:Unit = { }
}
