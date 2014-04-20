package com.library

/**
 * Configuration for dependencies and stuff
 */

object Config {
  // TODO: How about ConfigFactory?
  // https://github.com/typesafehub/config

  var httpClient:MyHttpClient = null //new MyHttpClient
  // TODO: ugly init is necessary because bookshelf tries to load a file
  var libraryClient: Library = null
  // = new DutchPublicLibrary
  var bookShelf: BookShelf = null // = new FileBasedBookShelf("data/boeken.dat")

  def reload: Unit = {}
}

