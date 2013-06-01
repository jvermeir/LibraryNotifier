
case class Author(val firstName:String, val lastName:String)

object Author {
  def apply(authorAsString:String):Author = {
    val commaPattern = "(.*),(.*)".r
    val blankPattern = "(.*) (.*)".r
    authorAsString match {
      case commaPattern(lastName, firstName) => Author(firstName.trim,lastName.trim)
      case blankPattern(firstName, lastName) => Author(firstName.trim,lastName.trim)
      case _ => Author("", authorAsString.trim)
    }
  }

  def main(args: Array[String]): Unit = {
    val authors = AuthorParser.loadAuthorsFromFile("data/authors.txt")
    println(authors)
  }
}