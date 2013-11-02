package com.library
// TODO: what about this object?

/**
 * Load new books for the list of authors I'm interested in and display the books I haven't read yet.
 */
object Main {
  def main(args: Array[String]) = {
    // TODO: Move into Config
    Config.libraryClient = new DutchPublicLibrary
    Config.bookShelf = new FileBasedBookShelf("data/boeken.dat")

    val bookShelf = Config.bookShelf
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.dat")
    bookShelf.updateBooks(Config.libraryClient.getBooksForAuthors(authors).values.flatten)
    bookShelf.write
    val booksToReadAsString = bookShelf.printAsWishList
    println (booksToReadAsString)
  }
}
