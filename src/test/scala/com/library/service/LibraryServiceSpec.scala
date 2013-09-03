package com.library.service

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import com.library.{TestFixtures, FirstLibraryForTest, Config}

class LibraryServiceSpec extends Specification with Specs2RouteTest with LibraryService with TestFixtures {

  Config.bookShelf = getBookShelfWithThreeBooks
  Config.libraryClient = new FirstLibraryForTest

  def actorRefFactory = system

  "LibraryService" should {

    "return a list of all unread books as HTML" in {
      Get("/books") ~> libraryRoute ~> check {
        entityAs[String] must contain(expectedBooksAsHTML)
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> libraryRoute ~> check {
        handled must beFalse
      }
    }

//    "return a MethodNotAllowed error for PUT requests to the root path" in {
//      Put() ~> sealRoute(libraryRoute) ~> check {
//        status === MethodNotAllowed
//        entityAs[String] === "HTTP method not allowed, supported methods: GET"
//      }
//    }
  }
  val expectedBooksAsHTML = """<table>
<tr><td>lastnameA</td><td>first</td><td>book2</td></tr>
<tr><td>lastnameA</td><td>first</td><td>book3</td></tr>
<tr><td>lastnameB</td><td>first</td><td>book1</td></tr>
</table>"""
}