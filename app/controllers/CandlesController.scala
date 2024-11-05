package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import models.{Candlestick, Pair}
import repositories.CandlesRepository
import play.api.libs.json._
import java.sql.Timestamp

@Singleton
class CandlesController @Inject() (
    cc: ControllerComponents,
    candlesRepository: CandlesRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def reads(json: JsValue): JsResult[Timestamp] =
      json.validate[Long].map(new Timestamp(_))
    def writes(ts: Timestamp): JsValue = JsNumber(ts.getTime)
  }

  implicit val candlestickFormat: OFormat[Candlestick] =
    Json.format[Candlestick]
  implicit val pairFormat: OFormat[Pair] = Json.format[Pair]

  def listCandlesticks = Action.async {
    candlesRepository.listCandlesticks().map { candlesticks =>
      Ok(Json.toJson(candlesticks))
    }
  }

  def createCandlesticks = Action.async(parse.json) { request =>
    request.body
      .validate[Seq[Candlestick]]
      .map { candlesticks =>
        candlesRepository.createCandlesticks(candlesticks).map {
          createdCandlesticks =>
            Created(Json.toJson(createdCandlesticks))
        }
      }
      .getOrElse(Future.successful(BadRequest("Invalid JSON")))
  }

  def listPairs = Action.async {
    candlesRepository.listPairs().map { pairs =>
      Ok(Json.toJson(pairs))
    }
  }

  def createPair = Action.async(parse.json) { request =>
    request.body
      .validate[Pair]
      .map { pair =>
        candlesRepository.createPair(pair).map { createdPair =>
          Created(Json.toJson(createdPair))
        }
      }
      .getOrElse(Future.successful(BadRequest("Invalid JSON")))
  }

  def findPairById(id: Long) = Action.async {
    candlesRepository.findPairById(id).map {
      case Some(pair) => Ok(Json.toJson(pair))
      case None       => NotFound
    }
  }

  def deletePair(id: Long) = Action.async {
    candlesRepository.deletePair(id).map {
      case 0 => NotFound
      case _ => NoContent
    }
  }

  def findCandlesticksByCriteria(
      pairName: Option[String],
      timeframe: Option[String],
      from: Option[Long],
      to: Option[Long]
  ) = Action.async {
    val fromTimestamp = from.map(new Timestamp(_))
    val toTimestamp = to.map(new Timestamp(_))
    candlesRepository
      .findCandlesticksByCriteria(
        pairName,
        timeframe,
        fromTimestamp,
        toTimestamp
      )
      .map { candlesticks =>
        Ok(Json.toJson(candlesticks))
      }
  }
}
