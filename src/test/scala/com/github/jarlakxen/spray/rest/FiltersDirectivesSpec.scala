package com.github.jarlakxen.spray.rest

import scala.concurrent.duration.FiniteDuration
import spray.http._
import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FiltersDirectivesSpec extends Specification with Specs2RouteTest with FiltersDirectives {

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(FiniteDuration(10, "s"))

  val JsonArrayFull = """[
    {
    	"name":"user1",
    	"password":"1234", 
    	"meta":{"email":"user1@email.com", "age": 12}
    }, 
    {
    	"name":"user2", 
    	"password":"0000",
    	"meta":{"email":"user2@email.com", "age": 20}
    }
  ]"""

  // //////////////////////////////////////////////
  // ROUTE
  // //////////////////////////////////////////////

  def route(ret: String) =
    path("filter-test") {
      filterResponse(resolver.json4s) {
        complete { HttpEntity(ContentTypes.`application/json`, ret) }
      }
    }

  // //////////////////////////////////////////////
  // SPEC
  // //////////////////////////////////////////////

  "using filters" should {

    "not filter anything if no filter is given" in {

      Get("/filter-test") ~> route(JsonArrayFull) ~> check {
        status === StatusCodes.OK
        response.entity.asString === JsonArrayFull
      }
    }

    "show only name" in {
      val JsonArrayOnlyName = """[{"name":"user1"},{"name":"user2"}]"""
      Get("/filter-test?filter=name") ~> route(JsonArrayFull) ~> check {
        status === StatusCodes.OK
        response.entity.asString === JsonArrayOnlyName
      }
    }

    "show only name and age" in {
      val JsonArrayOnlyNameAndAge = """[{"name":"user1","meta":{"age":12}},{"name":"user2","meta":{"age":20}}]"""
      Get("/filter-test?filter=name;meta;!meta.email") ~> route(JsonArrayFull) ~> check {
        status === StatusCodes.OK
        response.entity.asString === JsonArrayOnlyNameAndAge
      }
    }
  }

}