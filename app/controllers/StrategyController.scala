package controllers

import javax.inject._
import play.api.mvc._
import repositories.StrategyRepository
import models.StrategyDetails
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json
import play.api.libs.json.JsValue

@Singleton
class StrategyController @Inject() (
    cc: ControllerComponents,
    strategyRepository: StrategyRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def addIndicator(strategyId: Long): Action[JsValue] =
    Action.async(parse.json) { request =>
      (request.body \ "name")
        .validate[String]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Invalid JSON: Missing 'name' field"
                )
              )
            ),
          name => {
            strategyRepository.addIndicator(strategyId, name).map {
              case Some(indicatorId) =>
                Created(
                  Json.obj("status" -> "success", "indicatorId" -> indicatorId)
                )
              case None =>
                NotFound(
                  Json.obj(
                    "status" -> "error",
                    "message" -> s"Strategy with ID $strategyId not found"
                  )
                )
            }
          }
        )
    }

  def addStopLoss(strategyId: Long): Action[JsValue] =
    Action.async(parse.json) { request =>
      (request.body \ "name")
        .validate[String]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Invalid JSON: Missing 'name' field"
                )
              )
            ),
          name => {
            strategyRepository.addStopLoss(strategyId, name).map {
              case Some(stopLossId) =>
                Created(
                  Json.obj("status" -> "success", "stopLossId" -> stopLossId)
                )
              case None =>
                NotFound(
                  Json.obj(
                    "status" -> "error",
                    "message" -> s"Strategy with ID $strategyId not found"
                  )
                )
            }
          }
        )
    }

  def addTakeProfit(strategyId: Long): Action[JsValue] =
    Action.async(parse.json) { request =>
      (request.body \ "name")
        .validate[String]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Invalid JSON: Missing 'name' field"
                )
              )
            ),
          name => {
            strategyRepository.addTakeProfit(strategyId, name).map {
              case Some(takeProfitId) =>
                Created(
                  Json
                    .obj("status" -> "success", "takeProfitId" -> takeProfitId)
                )
              case None =>
                NotFound(
                  Json.obj(
                    "status" -> "error",
                    "message" -> s"Strategy with ID $strategyId not found"
                  )
                )
            }
          }
        )
    }

  def addPricePoint(strategyId: Long): Action[JsValue] =
    Action.async(parse.json) { request =>
      (request.body \ "name")
        .validate[String]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Invalid JSON: Missing 'name' field"
                )
              )
            ),
          name => {
            strategyRepository.addPricePoint(strategyId, name).map {
              case Some(pricePointId) =>
                Created(
                  Json
                    .obj("status" -> "success", "pricePointId" -> pricePointId)
                )
              case None =>
                NotFound(
                  Json.obj(
                    "status" -> "error",
                    "message" -> s"Strategy with ID $strategyId not found"
                  )
                )
            }
          }
        )
    }

  def getStrategiesByFirebaseUid(firebaseUid: String): Action[AnyContent] =
    Action.async {
      strategyRepository
        .getStrategiesWithDetailsByFirebaseUid(firebaseUid)
        .map { strategies =>
          Ok(Json.toJson(strategies))
        }
    }
  def addStrategy(firebaseUid: String): Action[JsValue] =
    Action.async(parse.json) { request =>
      (request.body \ "name")
        .validate[String]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Invalid JSON: Missing 'name' field"
                )
              )
            ),
          name => {
            strategyRepository.addStrategy(firebaseUid, name).map {
              case Some(strategyId) =>
                Created(
                  Json.obj("status" -> "success", "strategyId" -> strategyId)
                )
              case None =>
                NotFound(
                  Json.obj(
                    "status" -> "error",
                    "message" -> s"User with firebaseUid $firebaseUid not found"
                  )
                )
            }
          }
        )
    }
}
