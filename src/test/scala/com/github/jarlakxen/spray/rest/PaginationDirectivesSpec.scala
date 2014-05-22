package com.github.jarlakxen.spray.rest

import scala.concurrent.duration.FiniteDuration
import spray.http._
import spray.routing.Directives._
import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PaginationDirectivesSpec extends Specification with Specs2RouteTest with PaginationDirectives {

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(FiniteDuration(10, "s"))

  // //////////////////////////////////////////////
  // ROUTE
  // //////////////////////////////////////////////

  def route =
    path("filter-test") {
      pagination { page =>
        complete {
          page match {
            case Some(page) => page.toString
            case None => "NoPage"
          }
        }
      }
    }

  // //////////////////////////////////////////////
  // SPEC
  // //////////////////////////////////////////////

  "using pagination" should {

    "not have page if no page is requested" in {
      Get("/filter-test") ~> route ~> check {
        status === StatusCodes.OK
        response.entity.asString === "NoPage"
      }
    }

    "not have page if page requested have incomplete parameters" in {
      Get("/filter-test?page=1") ~> route ~> check {
        rejection must beAnInstanceOf[MalformedPaginationRejection]
      }
    }

    "return the page object that was requested" in {
      Get("/filter-test?page=1&size=10&sort=name,asc;age,desc") ~> route ~> check {
        status === StatusCodes.OK
        response.entity.asString === PageRequest(1, 10, Map("name" -> Order.Asc, "age" -> Order.Desc)).toString
      }
    }
  }

}