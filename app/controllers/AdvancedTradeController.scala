package controllers

import javax.inject._
import play.api.mvc._
import repositories.AdvancedTradeRepository
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import java.sql.Timestamp
import java.time.Instant

@Singleton
class AdvancedTradeController @Inject() (
    cc: ControllerComponents,
    advancedTradeRepository: AdvancedTradeRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // JSON format for the addAdvancedTrade endpoint request body
  case class AddAdvancedTradeRequest(
      startDate: String, // ISO date format (e.g., "2023-12-01T00:00:00Z")
      entryPrice: BigDecimal,
      stopLoss: BigDecimal,
      strategyId: Long,
      pairId: Long
  )

  implicit val addAdvancedTradeRequestFormat: OFormat[AddAdvancedTradeRequest] =
    Json.format[AddAdvancedTradeRequest]
  def getTradesByStrategy(strategyId: Long): Action[AnyContent] = Action.async {
    advancedTradeRepository.getTradesByStrategyId(strategyId).map { trades =>
      Ok(Json.obj("status" -> "success", "data" -> trades))
    } recover { case ex: Exception =>
      InternalServerError(
        Json.obj("status" -> "error", "message" -> ex.getMessage)
      )
    }
  }

  // Route for adding an advanced trade
  def addAdvancedTrade: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[AddAdvancedTradeRequest]
      .fold(
        errors => {
          Future.successful(
            BadRequest(
              Json.obj("status" -> "error", "message" -> JsError.toJson(errors))
            )
          )
        },
        tradeData => {
          val startDate = Timestamp.from(Instant.parse(tradeData.startDate))
          advancedTradeRepository
            .addAdvancedTrade(
              start_date = startDate,
              entry_price = tradeData.entryPrice,
              stop_loss = tradeData.stopLoss,
              strategy_id = tradeData.strategyId,
              pair_id = tradeData.pairId
            )
            .map {
              case Some(tradeId) =>
                Ok(Json.obj("status" -> "success", "tradeId" -> tradeId))
              case None =>
                BadRequest(
                  Json.obj(
                    "status" -> "error",
                    "message" -> "Failed to add trade"
                  )
                )
            }
        }
      )
  }
}
