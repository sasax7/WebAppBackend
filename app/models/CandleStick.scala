package models

import slick.jdbc.PostgresProfile.api._

import play.api.libs.json.{Json, OFormat, Format, JsResult, JsValue, JsNumber}

case class Pair(
    id: Option[Long],
    pairName: String,
    baseCurrency: Option[String],
    quoteCurrency: Option[String],
    description: Option[String],
    spread: Option[Double]
)

case class Candlestick(
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    time: Long,
    timeframe: String,
    volume: Option[Double],
    pair_id: Long
)
object Candlestick {
  // JSON format for Candlestick
  implicit val format: OFormat[Candlestick] = Json.format[Candlestick]

}
object Pair {
  implicit val format: OFormat[Pair] = Json.format[Pair]
}
class PairTable(tag: Tag) extends Table[Pair](tag, "pairs") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def pairName = column[String]("pair_name")
  def baseCurrency = column[Option[String]]("base_currency")
  def quoteCurrency = column[Option[String]]("quote_currency")
  def description = column[Option[String]]("description")
  def spread = column[Option[Double]]("spread")

  def * = (
    id.?,
    pairName,
    baseCurrency,
    quoteCurrency,
    description,
    spread
  ) <> ((Pair.apply _).tupled, Pair.unapply)
}

class CandlestickTable(tag: Tag)
    extends Table[Candlestick](tag, "candlesticks") {
  def open = column[Double]("open")
  def high = column[Double]("high")
  def low = column[Double]("low")
  def close = column[Double]("close")
  def time = column[Long]("time")
  def timeframe = column[String]("timeframe")
  def volume = column[Option[Double]]("volume")
  def pair_id = column[Long]("pair_id")

  def pair = foreignKey("pair_fk", pair_id, TableQuery[PairTable])(_.id)

  // Define Composite Primary Key
  def pk = primaryKey("pk_candlestick", (pair_id, timeframe, time))

  def * = (
    open,
    high,
    low,
    close,
    time,
    timeframe,
    volume,
    pair_id
  ) <> ((Candlestick.apply _).tupled, Candlestick.unapply)
}
