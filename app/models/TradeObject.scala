package models

import slick.jdbc.PostgresProfile.api._
import play.api.libs.json._
import models.AdvancedTradeObject

import models.TimestampFormat._ // Import the TimestampFormat object

object TradeState extends Enumeration {
  type TradeState = Value
  val NotTriggered, Triggered, Closed = Value
  implicit val format: Format[TradeState] = Json.formatEnum(this)
}

import TradeState._

case class Trade(
    addAdvancedTradeObject: Option[AdvancedTradeObject],
    currentProfit: Option[BigDecimal] = None,
    size: Option[BigDecimal] = None,
    state: TradeState = NotTriggered 
)

object Trade {
  implicit val format: OFormat[Trade] = Json.format[Trade]
}
