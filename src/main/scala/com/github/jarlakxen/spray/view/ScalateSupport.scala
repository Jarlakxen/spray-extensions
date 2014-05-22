package com.github.jarlakxen.spray.view

import spray.http._
import spray.http.StatusCodes._
import spray.routing._
import spray.routing.Directives._
import MediaTypes._
import org.fusesource.scalate.{ Binding, TemplateEngine }
import scala.concurrent.ExecutionContext
import java.util.ResourceBundle
import com.github.jarlakxen.utils.Config._

trait ScalateSupport {

  private val templateEngine = new TemplateEngine

  private val templatesPath = Config.get[String]("spray.extensions.view.templates-path") || "templates"
  private val defaultTemplatesFormat = Config.get[String]("spray.extensions.view.default-format") || "mustache"
  private val errorTemplatesPath = Config.get[String]("spray.extensions.view.templates-error-path") || "errors"

  def ssp(uri: String, attributes: Map[String, Any] = Map.empty)(implicit ec: ExecutionContext) = render(uri, "ssp", attributes)

  def jade(uri: String, attributes: Map[String, Any] = Map.empty)(implicit ec: ExecutionContext) = render(uri, "jade", attributes)

  def scaml(uri: String, attributes: Map[String, Any] = Map.empty)(implicit ec: ExecutionContext) = render(uri, "scaml", attributes)

  def mustache(uri: String, attributes: Map[String, Any] = Map.empty)(implicit ec: ExecutionContext) = render(uri, "mustache", attributes)

  protected def render(uri: String, ext: String, attributes: Map[String, Any] = Map.empty, mediaType: MediaType = `text/html`)(implicit ec: ExecutionContext): Route =
    detach() {
      respondWithMediaType(mediaType) {
        complete {
          val fullUri = (if (uri.startsWith("/")) templatesPath + uri else templatesPath + "/" + uri) + "." + ext
          templateEngine.layout(fullUri, attributes)
        }
      }
    }

  def renderError(errorCode: StatusCode, attributes: Map[String, Any] = Map.empty, mediaType: MediaType = `text/html`)(implicit ec: ExecutionContext): Route =
    errorCode match {
      case NotFound => render(errorTemplatesPath + "/" + NotFound.value, templatesPath, attributes, mediaType)
      case _ => render(errorTemplatesPath + "/default", templatesPath, attributes, mediaType)
    }

}