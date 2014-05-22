package com.github.jarlakxen.spray.rest

import spray.httpx.unmarshalling._
import spray.routing._
import spray.routing.Directives._
import spray.httpx.marshalling._
import spray.http.HttpHeaders._
import com.github.jarlakxen.utils.Config._

trait PaginationDirectives {

  private val IndexParam = Config.get[String]("spray.extensions.rest.pagination.index-param-name") || "page"
  private val SizeParam = Config.get[String]("spray.extensions.rest.pagination.size-param-name") || "size"
  private val SortParam = Config.get[String]("spray.extensions.rest.pagination.sort-param-name") || "sort"

  private val AscParam = Config.get[String]("spray.extensions.rest.pagination.asc-param-name") || "asc"
  private val DescParam = Config.get[String]("spray.extensions.rest.pagination.desc-param-name") || "desc"

  private val TotalElements = Config.get[String]("spray.extensions.rest.pagination.totalelements-header-name") || "total_elements"

  private val SortingSeparator = Config.get[String]("spray.extensions.rest.pagination.sorting-separator") || ";"
  private val OrderSeparator = Config.get[Char]("spray.extensions.rest.pagination.order-separator") || ','

  def pagination: Directive1[Option[PageRequest]] =
    parameterMap.flatMap { params =>
      (params.get(IndexParam).map(_.toInt), params.get(SizeParam).map(_.toInt)) match {
        case (Some(index), Some(size)) => provide(Some(deserializatePage(index, size, params.get(SortParam))))
        case (Some(index), None) => reject(MalformedPaginationRejection("Missing page size parameter", None))
        case (None, Some(size)) => reject(MalformedPaginationRejection("Missing page index parameter", None))
        case (_, _) => provide(None)
      }
    }

  def completePage[T](response: PageResponse[T])(implicit entitiesMarshaller: Marshaller[Traversable[T]]) =
    respondWithHeader(RawHeader(TotalElements, response.totalElements.toString)) {
      complete(response.elements)
    }

  private def deserializatePage(index: Int, size: Int, sorting: Option[String]) = {
    
    val sortingParam = sorting.map(_.split(SortingSeparator).map(_.span(_ != OrderSeparator)).collect {
      case (field, sort) if sort == ',' + AscParam => (field, Order.Asc)
      case (field, sort) if sort == ',' + DescParam => (field, Order.Desc)
    }.toMap)

    PageRequest(index, size, sortingParam.getOrElse(Map.empty))
  }

  case class MalformedPaginationRejection(errorMsg: String, cause: Option[Throwable] = None) extends Rejection

}

sealed trait Order

object Order {
  case object Asc extends Order
  case object Desc extends Order
}

case class PageRequest(index: Int, size: Int, sort: Map[String, Order])

case class PageResponse[T](elements: Traversable[T], totalElements: Int)
