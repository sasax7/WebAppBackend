package models

import slick.jdbc.PostgresProfile.api._
import play.api.libs.json._
import java.sql.Timestamp
import play.api.libs.json.{Json, OFormat, OWrites, Reads}
// Define implicit formats for Timestamp and Option[Timestamp]
object TimestampFormat {
  private val dateFormats = Seq(
    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"), // Z-style
    new java.text.SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    ) // With milliseconds
  )

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def reads(json: JsValue): JsResult[Timestamp] =
      json.validate[String].flatMap { str =>
        dateFormats.view
          .map { format =>
            try {
              JsSuccess(new Timestamp(format.parse(str).getTime))
            } catch {
              case _: java.text.ParseException => JsError()
            }
          }
          .find(_.isSuccess)
          .getOrElse(JsError(s"Invalid date format: $str"))
      }

    def writes(ts: Timestamp): JsValue =
      Json.toJson(dateFormats.head.format(ts)) // Primary format for output
  }

  implicit val optionTimestampFormat: Format[Option[Timestamp]] =
    new Format[Option[Timestamp]] {
      def reads(json: JsValue): JsResult[Option[Timestamp]] =
        json.validateOpt[Timestamp]

      def writes(optTs: Option[Timestamp]): JsValue = optTs match {
        case Some(ts) => Json.toJson(ts)
        case None     => JsNull
      }
    }
}

case class AdvancedTrade(
    id: Option[Long],
    startDate: java.sql.Timestamp,
    triggeredDate: Option[java.sql.Timestamp],
    isBuy: Boolean,
    entryPrice: BigDecimal,
    strategyId: Long,
    pairId: Long
)

object AdvancedTrade {
  import TimestampFormat._

  implicit val advancedTradeWrites: OWrites[AdvancedTrade] =
    new OWrites[AdvancedTrade] {
      def writes(trade: AdvancedTrade): JsObject = Json.obj(
        "id" -> trade.id,
        "startDate" -> Json.toJson(trade.startDate)(timestampFormat),
        "triggeredDate" -> Json.toJson(trade.triggeredDate)(
          optionTimestampFormat
        ),
        "isBuy" -> trade.isBuy,
        "entryPrice" -> trade.entryPrice,
        "strategyId" -> trade.strategyId,
        "pairId" -> trade.pairId
      )
    }

  implicit val advancedTradeReads: Reads[AdvancedTrade] =
    Json.reads[AdvancedTrade]

  implicit val format: OFormat[AdvancedTrade] =
    OFormat(advancedTradeReads, advancedTradeWrites)
  val tupled = (AdvancedTrade.apply _).tupled
}

class AdvancedTradeTable(tag: Tag)
    extends Table[AdvancedTrade](tag, "advancedtrade") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def startDate = column[java.sql.Timestamp]("start_date")
  def triggeredDate = column[Option[java.sql.Timestamp]]("triggered_date")
  def isBuy = column[Boolean]("is_buy")
  def entryPrice = column[BigDecimal]("entry_price")
  def strategyId = column[Long]("strategy_id")
  def pairId = column[Long]("pair_id")

  def strategy = foreignKey(
    "fk_advancedtrade_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
  def pair =
    foreignKey("fk_advancedtrade_pair", pairId, TableQuery[PairTable])(_.id)

  // Ensure the tuple matches the case class
  def * = (
    id.?,
    startDate,
    triggeredDate,
    isBuy,
    entryPrice,
    strategyId,
    pairId
  ) <> (AdvancedTrade.tupled, AdvancedTrade.unapply)
}
case class Indicator(
    id: Option[Long],
    value: Double,
    time: Option[Timestamp],
    indicatorNameId: Long,
    tradeId: Long
)
object Indicator {
  import TimestampFormat._

  implicit val writes: Writes[Indicator] = Json.writes[Indicator]
  implicit val format: OFormat[Indicator] = Json.format[Indicator]
  val tupled = (Indicator.apply _).tupled
}

class IndicatorTable(tag: Tag) extends Table[Indicator](tag, "indicators") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def value = column[Double]("value")
  def time = column[Option[Timestamp]]("time")
  def indicatorNameId = column[Long]("indicator_name_id")
  def tradeId = column[Long]("trade_id")

  def indicatorName = foreignKey(
    "fk_indicator_name",
    indicatorNameId,
    TableQuery[IndicatorNameTable]
  )(_.id)
  def trade =
    foreignKey("fk_indicator_trade", tradeId, TableQuery[AdvancedTradeTable])(
      _.id
    )

  def * = (
    id.?,
    value,
    time,
    indicatorNameId,
    tradeId
  ) <> (Indicator.tupled, Indicator.unapply)
}

case class PricePoint(
    id: Option[Long],
    value: Double,
    time: Timestamp,
    tradeId: Long,
    pricePointNameId: Long
)
object PricePoint {
  import TimestampFormat._

  implicit val writes: Writes[PricePoint] = Json.writes[PricePoint]
  implicit val format: OFormat[PricePoint] = Json.format[PricePoint]
  val tupled = (PricePoint.apply _).tupled
}

class PricePointTable(tag: Tag) extends Table[PricePoint](tag, "pricepoint") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def value = column[Double]("value")
  def time = column[Timestamp]("time")
  def tradeId = column[Long]("trade_id")
  def pricePointNameId = column[Long]("pricepoint_name_id")

  def trade =
    foreignKey("fk_pricepoint_trade", tradeId, TableQuery[AdvancedTradeTable])(
      _.id
    )
  def pricePointName = foreignKey(
    "fk_pricepoint_name",
    pricePointNameId,
    TableQuery[PricePointNameTable]
  )(_.id)

  def * = (
    id.?,
    value,
    time,
    tradeId,
    pricePointNameId
  ) <> (PricePoint.tupled, PricePoint.unapply)
}

case class StopLoss(
    id: Option[Long],
    price: Double,
    timeHit: Option[java.sql.Timestamp],
    highestPrice: Option[Double],
    highestRR: Option[Double],
    hit1RR: Boolean,
    tradeId: Long,
    stopLossNameId: Long
)

object StopLoss {
  import TimestampFormat._

  implicit val writes: Writes[StopLoss] = Json.writes[StopLoss]
  implicit val format: OFormat[StopLoss] = Json.format[StopLoss]
  val tupled = (StopLoss.apply _).tupled
}

class StopLossTable(tag: Tag) extends Table[StopLoss](tag, "stoploss") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def price = column[Double]("price")
  def timeHit = column[Option[java.sql.Timestamp]]("timehit")
  def highestPrice = column[Option[Double]]("highest_price")
  def highestRR = column[Option[Double]]("highest_rr")
  def hit1RR = column[Boolean]("hit_1_rr")
  def tradeId = column[Long]("trade_id")
  def stopLossNameId = column[Long]("stoploss_name_id")

  def trade =
    foreignKey("fk_stoploss_trade", tradeId, TableQuery[AdvancedTradeTable])(
      _.id
    )
  def stopLossName =
    foreignKey(
      "fk_stoploss_name",
      stopLossNameId,
      TableQuery[StopLossNameTable]
    )(_.id)

  def * = (
    id.?,
    price,
    timeHit,
    highestPrice,
    highestRR,
    hit1RR,
    tradeId,
    stopLossNameId
  ) <> (StopLoss.tupled, StopLoss.unapply)
}

case class TakeProfit(
    id: Option[Long],
    price: Double,
    timeHit: Option[java.sql.Timestamp],
    lowestPrice: Option[Double],
    tradeId: Long,
    takeProfitNameId: Long
)

object TakeProfit {

  import TimestampFormat._

  implicit val writes: Writes[TakeProfit] = Json.writes[TakeProfit]
  implicit val format: OFormat[TakeProfit] = Json.format[TakeProfit]
  val tupled = (TakeProfit.apply _).tupled
}

class TakeProfitTable(tag: Tag) extends Table[TakeProfit](tag, "takeprofit") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def price = column[Double]("price")
  def timeHit = column[Option[java.sql.Timestamp]]("time_hit")
  def lowestPrice = column[Option[Double]]("lowest_price")
  def tradeId = column[Long]("trade_id")
  def takeProfitNameId = column[Long]("takeprofit_name_id")

  def trade =
    foreignKey("fk_takeprofit_trade", tradeId, TableQuery[AdvancedTradeTable])(
      _.id
    )
  def takeProfitName =
    foreignKey(
      "fk_takeprofit_name",
      takeProfitNameId,
      TableQuery[TakeProfitNameTable]
    )(_.id)

  def * = (
    id.?,
    price,
    timeHit,
    lowestPrice,
    tradeId,
    takeProfitNameId
  ) <> (TakeProfit.tupled, TakeProfit.unapply)
}

case class TradeRR(
    id: Option[Long],
    takeProfitId: Long,
    stopLossId: Long,
    rr: Double
)

object TradeRR {
  implicit val format: OFormat[TradeRR] = Json.format[TradeRR]
  val tupled = (TradeRR.apply _).tupled
}

class TradeRRTable(tag: Tag) extends Table[TradeRR](tag, "traderr") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def takeProfitId = column[Long]("takeprofit_id")
  def stopLossId = column[Long]("stoploss_id")
  def rr = column[Double]("rr")

  def takeProfit =
    foreignKey(
      "fk_traderr_takeprofit",
      takeProfitId,
      TableQuery[TakeProfitTable]
    )(_.id)
  def stopLoss =
    foreignKey("fk_traderr_stoploss", stopLossId, TableQuery[StopLossTable])(
      _.id
    )

  def * =
    (id.?, takeProfitId, stopLossId, rr) <> (TradeRR.tupled, TradeRR.unapply)
}
