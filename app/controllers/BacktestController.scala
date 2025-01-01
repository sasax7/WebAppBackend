package controllers

import javax.inject._
import play.api.mvc._
import repositories.{
  CandlesRepository,
  UsersRepository,
  StrategyRepository,
  AdvancedTradeRepository,
  PairsRepository
}
import models.TimestampFormat._
import state.{BacktestContext, OperationalState}
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models.{
  Trade,
  Strategy,
  StrategyDetails,
  User,
  Candlestick,
  AddTradeRequest
}
import java.sql.Timestamp

@Singleton
class BacktestController @Inject() (
    cc: ControllerComponents,
    candlesRepository: CandlesRepository,
    usersRepository: UsersRepository,
    strategyRepository: StrategyRepository,
    advancedTradeRepository: AdvancedTradeRepository,
    pairsRepository: PairsRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  private var context: BacktestContext = _
  def addStopLoss: Action[JsValue] = Action.async(parse.json) { request =>
    val json = request.body
    val price = (json \ "price").as[Double]
    val tradeId = (json \ "tradeId").asOpt[Long]
    val stopLossNameId = (json \ "stopLossNameId").as[Long]

    if (context == null) {
      Future.successful(
        BadRequest(
          Json.obj("status" -> "error", "message" -> "Context not initialized")
        )
      )
    } else {
      implicit val repo: AdvancedTradeRepository = advancedTradeRepository
      context.getCurrentState
        .asInstanceOf[OperationalState]
        .addStopLoss(price, tradeId, stopLossNameId)
        .map { updatedState =>
          context = new BacktestContext(updatedState)
          Ok(
            Json
              .obj("status" -> "success", "state" -> Json.toJson(updatedState))
          )
        }
        .recover { case e: Exception =>
          BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
        }
    }
  }

  def addTakeProfit: Action[JsValue] = Action.async(parse.json) { request =>
    val json = request.body
    val price = (json \ "price").as[Double]
    val tradeId = (json \ "tradeId").asOpt[Long]
    val takeProfitNameId = (json \ "takeProfitNameId").as[Long]

    if (context == null) {
      Future.successful(
        BadRequest(
          Json.obj("status" -> "error", "message" -> "Context not initialized")
        )
      )
    } else {
      implicit val repo: AdvancedTradeRepository = advancedTradeRepository
      context.getCurrentState
        .asInstanceOf[OperationalState]
        .addTakeProfit(price, tradeId, takeProfitNameId)
        .map { updatedState =>
          context = new BacktestContext(updatedState)
          Ok(
            Json
              .obj("status" -> "success", "state" -> Json.toJson(updatedState))
          )
        }
        .recover { case e: Exception =>
          BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
        }
    }
  }

  def addIndicator: Action[JsValue] = Action.async(parse.json) { request =>
    val json = request.body
    val value = (json \ "value").as[Double]
    val time = (json \ "time").asOpt[Timestamp]
    val indicatorNameId = (json \ "indicatorNameId").as[Long]
    val tradeId = (json \ "tradeId").asOpt[Long]

    if (context == null) {
      Future.successful(
        BadRequest(
          Json.obj("status" -> "error", "message" -> "Context not initialized")
        )
      )
    } else {
      implicit val repo: AdvancedTradeRepository = advancedTradeRepository
      context.getCurrentState
        .asInstanceOf[OperationalState]
        .addIndicator(value, time, indicatorNameId, tradeId)
        .map { updatedState =>
          context = new BacktestContext(updatedState)
          Ok(
            Json
              .obj("status" -> "success", "state" -> Json.toJson(updatedState))
          )
        }
        .recover { case e: Exception =>
          BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
        }
    }
  }

  def addPricePoint: Action[JsValue] = Action.async(parse.json) { request =>
    val json = request.body
    val value = (json \ "value").as[Double]
    val time = (json \ "time").as[Timestamp]
    val pricePointNameId = (json \ "pricePointNameId").as[Long]
    val tradeId = (json \ "tradeId").asOpt[Long]

    if (context == null) {
      Future.successful(
        BadRequest(
          Json.obj("status" -> "error", "message" -> "Context not initialized")
        )
      )
    } else {
      implicit val repo: AdvancedTradeRepository = advancedTradeRepository
      context.getCurrentState
        .asInstanceOf[OperationalState]
        .addPricePoint(value, time, pricePointNameId, tradeId)
        .map { updatedState =>
          context = new BacktestContext(updatedState)
          Ok(
            Json
              .obj("status" -> "success", "state" -> Json.toJson(updatedState))
          )
        }
        .recover { case e: Exception =>
          BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
        }
    }
  }
  def addTrade: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[AddTradeRequest]
      .fold(
        errors => {
          Future.successful(
            BadRequest(
              Json.obj("status" -> "error", "message" -> JsError.toJson(errors))
            )
          )
        },
        tradeData => {
          if (context == null) {
            Future.successful(
              BadRequest(
                Json.obj(
                  "status" -> "error",
                  "message" -> "Context not initialized"
                )
              )
            )
          } else {
            implicit val repo: AdvancedTradeRepository = advancedTradeRepository
            context.getCurrentState
              .asInstanceOf[OperationalState]
              .addTradeAsync(tradeData)
              .map { updatedState =>
                context = new BacktestContext(updatedState)
                Ok(
                  Json.obj(
                    "status" -> "success",
                    "state" -> Json.toJson(updatedState)
                  )
                )
              }
              .recover { case e: Exception =>
                BadRequest(
                  Json.obj(
                    "status" -> "error",
                    "message" -> e.getMessage
                  )
                )
              }
          }
        }
      )
  }
  def initializeState: Action[JsValue] = Action(parse.json).async {
    implicit request =>
      val stateResult = for {
        newestTimestamp <- (request.body \ "newestTimestamp").asOpt[Long]
        strategyId <- (request.body \ "strategyId").asOpt[Long]
        userFirebaseId <- (request.body \ "userFirebaseId").asOpt[String]
        currentPairName <- (request.body \ "currentPair").asOpt[String]
        balance = (request.body \ "balance").asOpt[BigDecimal]
        timeframe = (request.body \ "timeframe").asOpt[String]
        spread = (request.body \ "spread").asOpt[BigDecimal]
        fees = (request.body \ "fees").asOpt[BigDecimal]
        percentageRiskPerTrade = (request.body \ "percentageRiskPerTrade")
          .asOpt[BigDecimal]
      } yield {
        OperationalState
          .initialize(
            newestTimestamp = newestTimestamp,
            strategyId = strategyId,
            userFirebaseId = userFirebaseId,
            currentPairName = currentPairName,
            balance = balance,
            timeframe = timeframe,
            spread = spread,
            fees = fees,
            percentageRiskPerTrade = percentageRiskPerTrade
          )(
            ec,
            candlesRepository,
            usersRepository,
            strategyRepository,
            advancedTradeRepository,
            pairsRepository
          )
          .map { initialState =>
            context = new BacktestContext(initialState)
            Ok(
              Json.obj(
                "status" -> "success",
                "state" -> Json.toJson(initialState)
              )
            )
          }
          .recover { case e: Exception =>
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> e.getMessage
              )
            )
          }
      }

      stateResult.getOrElse(
        Future.successful(
          BadRequest(
            Json.obj("status" -> "error", "message" -> "Invalid input")
          )
        )
      )
  }

  def updateTimestamp(newTimestamp: Long): Action[AnyContent] = Action.async {
    implicit request =>
      if (context == null) {
        Future.successful(
          BadRequest(
            Json.obj(
              "status" -> "error",
              "message" -> "Context not initialized"
            )
          )
        )
      } else {
        implicit val repo: CandlesRepository = candlesRepository
        context
          .updateTimestamp(newTimestamp)
          .map { _ =>
            val currentState =
              context.getCurrentState.asInstanceOf[OperationalState]
            Ok(
              Json.obj(
                "status" -> "success",
                "state" -> Json.toJson(currentState)
              )
            )
          }
          .recover { case e: Exception =>
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> e.getMessage
              )
            )
          }
      }
  }
}
