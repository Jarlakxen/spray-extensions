package com.github.jarlakxen.spray.rest

import spray.http.HttpEntity
import spray.routing._
import com.github.jarlakxen.utils.Config._

import scala.reflect.ClassTag

trait FiltersDirectives extends Directives {
  import FiltersDirectives._

  private val FilterSeparator = Config.get[String]("spray.extensions.rest.filter.separator") || ";"

  object resolver {
    val json4s = Json4sFilter
    val `spray-json` = SprayJsonFilter
  }

  def filterResponse(implicit jsonFilter: JsonFilter) = {
    parameter('filter ?).flatMap { filterParam =>
      filterParam.map(toFilter).map { filter =>
        mapHttpResponseEntity { entity =>
          import spray.http.ContentTypes.`application/json`
          entity.flatMap {
            case HttpEntity.NonEmpty(`application/json`, data) => {
              HttpEntity(`application/json`, jsonFilter.filter(entity.asString, filter))
            }
            case x => x
          }
        }
      } getOrElse {
        noop
      }
    }
  }

  private def toFilter(filterParam: String): Filter = {
    val filters = filterParam.split(FilterSeparator).span(!_.startsWith("!"))
    Filter(filters._1, filters._2)
  }
}

object FiltersDirectives {

  case class Filter(includedFields: Seq[String], excludedFields: Seq[String])

}

trait JsonFilter {

  import FiltersDirectives._

  def filter(json: String, filter: Filter): String

  def removeOneLevel(fields: Seq[String]) = fields.flatMap(substringAfter(".") _)

  def substringBefore(token: String)(string: String) =
    string.indexOf(token) match {
      case -1 => string
      case i => string.substring(0, i)
    }

  def substringAfter(token: String)(string: String): Option[String] = {
    string.indexOf(token) match {
      case -1 => None
      case i => Some(string.substring(i + token.length))
    }
  }

}

private[rest] object Json4sFilter extends JsonFilter {
  import FiltersDirectives._
  import org.json4s.JsonAST._
  import org.json4s.jackson.JsonMethods._

  def filter(json: String, filter: Filter): String = {
    val filteredJson = filteredAst(parse(json), filter.includedFields, filter.excludedFields)
    compact(filteredJson)
  }

  def filteredAst(ast: JValue, includedFields: Seq[String], excludedFields: Seq[String]): JValue = ast match {
    case x: JObject => filteredAst(x, includedFields, excludedFields)
    case x: JArray => filteredAst(x, includedFields, excludedFields)
    case x: JValue => x
  }

  def filteredAst(ast: JObject, includedFields: Seq[String], excludedFields: Seq[String]): JObject = {
    val include = includedFields.map(substringBefore(".")).toSet
    val filteredFirstPhase =
      if (!include.isEmpty) ast.copy(obj = ast.obj.filter { case (name, _) => include.contains(name) })
      else ast

    val exclude = excludedFields.filter(!_.contains(".")) // "meta.total" should not exclude the whole "meta"
    val filteredSecondPhase =
      if (!exclude.isEmpty) filteredFirstPhase.copy(obj = filteredFirstPhase.obj.filter { case (name, _) => !exclude.contains(name) })
      else filteredFirstPhase

    filteredSecondPhase.copy(
      obj = filteredSecondPhase.obj.map {
        case (name, value) =>
          (name, filteredAst(value, removeOneLevel(includedFields), removeOneLevel(excludedFields)))
      })
  }

  def filteredAst(ast: JArray, includedFields: Seq[String], excludedFields: Seq[String]): JArray =
    ast.copy(arr = ast.arr.map(filteredAst(_, includedFields, excludedFields)))
}

private[rest] object SprayJsonFilter extends JsonFilter {
  import FiltersDirectives._
  import spray.json._
  import DefaultJsonProtocol._
  def filter(json: String, filter: Filter): String = {
    val filteredJson = filteredAst(json.parseJson, filter.includedFields, filter.excludedFields)
    filteredJson.compactPrint
  }

  def filteredAst(ast: JsValue, includedFields: Seq[String], excludedFields: Seq[String]): JsValue = ast match {
    case x: JsObject => filteredAst(x, includedFields, excludedFields)
    case x: JsArray => filteredAst(x, includedFields, excludedFields)
    case x: JsValue => x
  }

  def filteredAst(ast: JsObject, includedFields: Seq[String], excludedFields: Seq[String]): JsObject = {
    val include = includedFields.map(substringBefore(".")).toSet
    val filteredFirstPhase =
      if (!include.isEmpty) ast.copy(fields = ast.fields.filter { case (name, _) => include.contains(name) })
      else ast

    val exclude = excludedFields.filter(!_.contains(".")) // "meta.total" should not exclude the whole "meta"
    val filteredSecondPhase =
      if (!exclude.isEmpty) filteredFirstPhase.copy(fields = filteredFirstPhase.fields.filter { case (name, _) => !exclude.contains(name) })
      else filteredFirstPhase

    filteredSecondPhase.copy(
      fields = filteredSecondPhase.fields.map {
        case (name, value) =>
          (name, filteredAst(value, removeOneLevel(includedFields), removeOneLevel(excludedFields)))
      })
  }

  def filteredAst(ast: JsArray, includedFields: Seq[String], excludedFields: Seq[String]): JsArray = {
    ast.copy(elements = ast.elements.map(filteredAst(_, includedFields, excludedFields)))
  }
}