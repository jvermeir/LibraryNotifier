package com.library.library

/**
  * Configuration for dependencies and stuff
  */

object Config {
  var libraryClient = new DutchPublicLibrary
  def reload:Unit = { }
}
