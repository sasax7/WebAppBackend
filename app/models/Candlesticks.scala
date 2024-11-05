package models

import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

case class Pair(
    id: Option[Long],
    pairName: String,
    baseCurrency: Option[String],
    quoteCurrency: Option[String],
    description: Option[String],
    spread: Option[Double]
)

case class Candlestick(
    id: Option[Long],
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    time: Timestamp,
    timeframe: String,
    volume: Option[Double],
    pairId: Long
)

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
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def open = column[Double]("open")
  def high = column[Double]("high")
  def low = column[Double]("low")
  def close = column[Double]("close")
  def time = column[Timestamp]("time")
  def timeframe = column[String]("timeframe")
  def volume = column[Option[Double]]("volume")
  def pairId = column[Long]("pair_id")

  def pair = foreignKey("pair_fk", pairId, TableQuery[PairTable])(_.id)

  def * = (
    id.?,
    open,
    high,
    low,
    close,
    time,
    timeframe,
    volume,
    pairId
  ) <> ((Candlestick.apply _).tupled, Candlestick.unapply)
}
