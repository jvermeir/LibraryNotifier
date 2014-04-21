package com.library

/**
 * Configuration for dependencies and stuff
 */

object Config {
  // TODO: How about ConfigFactory?
  // https://github.com/typesafehub/config

  var httpClient:MyHttpClient = new MyHttpClient
  // TODO: fix ugly init in bookshelf that tries to load a file
  var libraryClient: Library = new DutchPublicLibrary
  var bookShelf: BookShelf = new FileBasedBookShelf("data/boeken.dat")

  def reload: Unit = {}

  def print:String = "httpClient: " + httpClient + " - libraryClient: " + libraryClient + " - bookShelf: " + bookShelf
}

