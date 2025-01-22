package controllers

import javax.inject._
import play.api.mvc._
import repositories._
import models.TimestampFormat._
import state.{BacktestContext, OperationalState}
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models._
import services.BacktestContextService
import java.sql.Timestamp

@Singleton
class BacktestController @Inject() (
    cc: ControllerComponents,
    candlesRepository: CandlesRepository,
    usersRepository: UsersRepository,
    strategyRepository: StrategyRepository,
    advancedTradeRepository: AdvancedTradeRepository,
    pairsRepository: PairsRepository,
    backtestContextService: BacktestContextService
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def saveChartDrawings(key: String): Action[JsValue] =
    Action.async(parse.json) { request =>
      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          val updatedState = ctx.getCurrentState
            .asInstanceOf[OperationalState]
            .saveChartDrawings((request.body \ "drawings").as[JsValue])
          backtestContextService.setContext(
            key,
            new BacktestContext(updatedState)
          )
          Future.successful(
            Ok(
              Json.obj(
                "status" -> "success",
                "state" -> Json.toJson(updatedState)
              )
            )
          )
      }
    }

  def getChartDrawings(key: String): Action[AnyContent] = Action.async {
    backtestContextService.getContext(key) match {
      case None =>
        Future.successful(
          BadRequest(
            Json.obj(
              "status" -> "error",
              "message" -> "Context not initialized"
            )
          )
        )
      case Some(ctx) =>
        val drawings =
          ctx.getCurrentState.asInstanceOf[OperationalState].chartDrawings
        Future.successful(
          Ok(Json.obj("status" -> "success", "drawings" -> drawings))
        )
    }
  }

  def addStopLoss(key: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      val json = request.body
      val price = (json \ "price").as[Double]
      val tradeId = (json \ "tradeId").asOpt[Long]
      val stopLossNameId = (json \ "stopLossNameId").as[Long]

      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          implicit val repo: AdvancedTradeRepository = advancedTradeRepository
          ctx.getCurrentState
            .asInstanceOf[OperationalState]
            .addStopLoss(price, tradeId, stopLossNameId)
            .map { updatedState =>
              backtestContextService.setContext(
                key,
                new BacktestContext(updatedState)
              )
              Ok(
                Json.obj(
                  "status" -> "success",
                  "state" -> Json.toJson(updatedState)
                )
              )
            }
            .recover { case e: Exception =>
              BadRequest(
                Json.obj("status" -> "error", "message" -> e.getMessage)
              )
            }
      }
  }

  def addTakeProfit(key: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      val json = request.body
      val price = (json \ "price").as[Double]
      val tradeId = (json \ "tradeId").asOpt[Long]
      val takeProfitNameId = (json \ "takeProfitNameId").as[Long]

      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          implicit val repo: AdvancedTradeRepository = advancedTradeRepository
          ctx.getCurrentState
            .asInstanceOf[OperationalState]
            .addTakeProfit(price, tradeId, takeProfitNameId)
            .map { updatedState =>
              backtestContextService.setContext(
                key,
                new BacktestContext(updatedState)
              )
              Ok(
                Json.obj(
                  "status" -> "success",
                  "state" -> Json.toJson(updatedState)
                )
              )
            }
            .recover { case e: Exception =>
              BadRequest(
                Json.obj("status" -> "error", "message" -> e.getMessage)
              )
            }
      }
  }

  def addIndicator(key: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      val json = request.body
      val value = (json \ "value").as[Double]
      val time = (json \ "time").asOpt[Timestamp]
      val indicatorNameId = (json \ "indicatorNameId").as[Long]
      val tradeId = (json \ "tradeId").asOpt[Long]

      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          implicit val repo: AdvancedTradeRepository = advancedTradeRepository
          ctx.getCurrentState
            .asInstanceOf[OperationalState]
            .addIndicator(value, time, indicatorNameId, tradeId)
            .map { updatedState =>
              backtestContextService.setContext(
                key,
                new BacktestContext(updatedState)
              )
              Ok(
                Json.obj(
                  "status" -> "success",
                  "state" -> Json.toJson(updatedState)
                )
              )
            }
            .recover { case e: Exception =>
              BadRequest(
                Json.obj("status" -> "error", "message" -> e.getMessage)
              )
            }
      }
  }

  def addPricePoint(key: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      val json = request.body
      val value = (json \ "value").as[Double]
      val time = (json \ "time").as[Long]
      val pricePointNameId = (json \ "pricePointNameId").as[Long]
      val tradeId = (json \ "tradeId").asOpt[Long]

      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          implicit val repo: AdvancedTradeRepository = advancedTradeRepository
          val timestamp = new Timestamp(time * 1000)
          ctx.getCurrentState
            .asInstanceOf[OperationalState]
            .addPricePoint(value, timestamp, pricePointNameId, tradeId)
            .map { updatedState =>
              backtestContextService.setContext(
                key,
                new BacktestContext(updatedState)
              )
              Ok(
                Json.obj(
                  "status" -> "success",
                  "state" -> Json.toJson(updatedState)
                )
              )
            }
            .recover { case e: Exception =>
              BadRequest(
                Json.obj("status" -> "error", "message" -> e.getMessage)
              )
            }
      }
  }

  def addTrade(key: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body
        .validate[AddTradeRequest]
        .fold(
          errors => {
            Future.successful(
              BadRequest(
                Json
                  .obj("status" -> "error", "message" -> JsError.toJson(errors))
              )
            )
          },
          tradeData => {
            backtestContextService.getContext(key) match {
              case None =>
                Future.successful(
                  BadRequest(
                    Json.obj(
                      "status" -> "error",
                      "message" -> "Context not initialized"
                    )
                  )
                )
              case Some(ctx) =>
                implicit val repo: AdvancedTradeRepository =
                  advancedTradeRepository
                ctx.getCurrentState
                  .asInstanceOf[OperationalState]
                  .addTradeAsync(tradeData)
                  .map { updatedState =>
                    backtestContextService
                      .setContext(key, new BacktestContext(updatedState))
                    Ok(
                      Json.obj(
                        "status" -> "success",
                        "state" -> Json.toJson(updatedState)
                      )
                    )
                  }
                  .recover { case e: Exception =>
                    BadRequest(
                      Json.obj("status" -> "error", "message" -> e.getMessage)
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
            val key = backtestContextService.generateKey()
            backtestContextService.setContext(
              key,
              new BacktestContext(initialState)
            )
            Ok(
              Json.obj(
                "status" -> "success",
                "state" -> Json.toJson(initialState),
                "key" -> key
              )
            )
          }
          .recover { case e: Exception =>
            BadRequest(Json.obj("status" -> "error", "message" -> e.getMessage))
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

  def updateTimestamp(key: String, newTimestamp: Long): Action[AnyContent] =
    Action.async {
      backtestContextService.getContext(key) match {
        case None =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Context not initialized"
              )
            )
          )
        case Some(ctx) =>
          implicit val repo: CandlesRepository = candlesRepository
          ctx
            .updateTimestamp(newTimestamp)
            .map { _ =>
              val currentState =
                ctx.getCurrentState.asInstanceOf[OperationalState]
              backtestContextService.setContext(key, ctx)
              Ok(
                Json.obj(
                  "status" -> "success",
                  "state" -> Json.toJson(currentState)
                )
              )
            }
            .recover { case e: Exception =>
              BadRequest(
                Json.obj("status" -> "error", "message" -> e.getMessage)
              )
            }
      }
    }

  def getState(key: String): Action[AnyContent] = Action.async {
    backtestContextService.getContext(key) match {
      case None =>
        Future.successful(
          BadRequest(
            Json.obj(
              "status" -> "error",
              "message" -> "Context not initialized"
            )
          )
        )
      case Some(ctx) =>
        val state = ctx.getCurrentState.asInstanceOf[OperationalState]
        Future.successful(
          Ok(Json.obj("status" -> "success", "state" -> Json.toJson(state)))
        )
    }
  }
}
