package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{Future, ExecutionContext}
import repositories.CandlesRepository
import models.Candlestick
import play.api.libs.json._

@Singleton
class CandlesticksController @Inject() (
    cc: ControllerComponents,
    candlesRepository: CandlesRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val candlestickFormat: OFormat[Candlestick] =
    Json.format[Candlestick]

  // Load candlesticks from the database before handling requests
  private def loadCandlesticks(): Future[Unit] =
    candlesRepository.listCandlesticks().map(_ => ())

  /** List all candlesticks. GET /candlesticks
    */
  def listCandlesticks: Action[AnyContent] = Action.async { implicit request =>
    loadCandlesticks().flatMap { _ =>
      candlesRepository.listCandlesticks().map { candlesticks =>
        Ok(Json.toJson(candlesticks))
      }
    }
  }

  /** Create multiple candlesticks. POST /candlesticks Expects JSON array of
    * candlesticks.
    */
  def createCandlesticks: Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body
        .validate[Seq[Candlestick]]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj("status" -> "error", "message" -> "Invalid JSON")
              )
            ),
          candlesticksList => {
            candlesRepository.createCandlesticks(candlesticksList).map {
              createdCandlesticks =>
                Created(Json.toJson(createdCandlesticks))
            }
          }
        )
  }

  /** Get a batch of candlesticks by pair name, timeframe, and up to a specific
    * time. GET
    * /candlesticks/batch?pairName=...&timeframe=...&toTime=...&batchSize=...
    */
  def getCandlesticksBatch: Action[AnyContent] = Action.async {
    implicit request =>
      val queryParams = request.queryString

      val pairNameOpt = queryParams.get("pairName").flatMap(_.headOption)
      val timeframeOpt = queryParams.get("timeframe").flatMap(_.headOption)
      val toTimeOpt = queryParams.get("toTime").flatMap(_.headOption)
      val batchSizeOpt = queryParams.get("batchSize").flatMap(_.headOption)

      (pairNameOpt, timeframeOpt, toTimeOpt, batchSizeOpt) match {
        case (
              Some(pairName),
              Some(timeframe),
              Some(toTimeStr),
              Some(batchSizeStr)
            ) =>
          try {
            val toTime = toTimeStr.toLong
            val batchSize = batchSizeStr.toInt

            candlesRepository
              .getCandlesticksBatchByPairName(
                pairName,
                timeframe,
                toTime,
                batchSize
              )
              .map { candlesticks =>
                Ok(Json.toJson(candlesticks))
              }
          } catch {
            case _: Exception =>
              Future.successful(
                BadRequest(
                  Json.obj(
                    "status" -> "error",
                    "message" -> "Invalid parameters"
                  )
                )
              )
          }

        case _ =>
          Future.successful(
            BadRequest(
              Json.obj(
                "status" -> "error",
                "message" -> "Missing required query parameters"
              )
            )
          )
      }
  }

  /** Delete candlesticks by pair ID. DELETE /candlesticks/pair/:pairId
    */
  def deleteCandlesticksByPairId(pair_id: Long): Action[AnyContent] =
    Action.async { implicit request =>
      candlesRepository.deleteCandlesticksByPairId(pair_id).map { deletedRows =>
        if (deletedRows > 0)
          Ok(Json.obj("status" -> "success", "deletedRows" -> deletedRows))
        else
          NotFound(
            Json.obj(
              "status" -> "error",
              "message" -> "No candlesticks found for the provided pairId"
            )
          )
      }
    }

}
