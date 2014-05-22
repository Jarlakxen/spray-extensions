package com.github.jarlakxen.spray.rest

import akka.actor._
import spray.routing._
import spray.routing.directives._
import spray.http._
import spray.http.MediaTypes._
import spray.http.HttpHeaders._
import scala.concurrent._
import scala.util.Success

trait RestDirectives extends PaginationDirectives with FiltersDirectives {

}