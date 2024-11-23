package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import repositories.PairsRepository
import models.Pair
import play.api.libs.json._

@Singleton
class PairsController @Inject() (
    cc: ControllerComponents,
    pairsRepository: PairsRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val pairFormat: OFormat[Pair] = Json.format[Pair]

  // Load pairs from the database before handling requests
  private def loadPairs(): Future[Unit] =
    pairsRepository.listPairs().map(_ => ())

  def listPairs: Action[AnyContent] = Action.async { implicit request =>
    loadPairs().flatMap { _ =>
      pairsRepository.listPairs().map { pairs =>
        Ok(Json.toJson(pairs))
      }
    }
  }

  def createPair: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[Pair]
      .fold(
        errors =>
          Future.successful(
            BadRequest(
              Json.obj("status" -> "error", "message" -> "Invalid JSON")
            )
          ),
        pair => {
          pairsRepository.createPair(pair).map { createdPair =>
            Created(Json.toJson(createdPair))
          }
        }
      )
  }

  def findPairById(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      loadPairs().flatMap { _ =>
        pairsRepository.findPairById(id).map {
          case Some(pair) => Ok(Json.toJson(pair))
          case None =>
            NotFound(
              Json.obj("status" -> "error", "message" -> "Pair not found")
            )
        }
      }
  }

  def deletePair(id: Long): Action[AnyContent] = Action.async {
    implicit request =>
      loadPairs().flatMap { _ =>
        pairsRepository.deletePair(id).map { deletedRows =>
          if (deletedRows > 0) Ok(Json.obj("status" -> "success"))
          else
            NotFound(
              Json.obj("status" -> "error", "message" -> "Pair not found")
            )
        }
      }
  }

}
