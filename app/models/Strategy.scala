package models

import play.api.libs.json.{Json, OFormat}
import slick.jdbc.PostgresProfile.api._

case class StrategyDetails(
    strategyId: Long,
    strategyName: String,
    indicatorNames: Seq[String],
    stopLossNames: Seq[String],
    takeProfitNames: Seq[String],
    pricePointNames: Seq[String]
)

object StrategyDetails {
  implicit val format: OFormat[StrategyDetails] = Json.format[StrategyDetails]
}

case class Strategy(id: Option[Long], name: String)

class StrategyTable(tag: Tag) extends Table[Strategy](tag, "strategies") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (Strategy.tupled, Strategy.unapply)
}
case class UserStrategy(userId: Long, strategyId: Long)

class UserStrategiesTable(tag: Tag)
    extends Table[UserStrategy](tag, "userstrategies") {
  def userId = column[Long]("user_id")
  def strategyId = column[Long]("strategy_id")

  def pk = primaryKey("pk_userstrategies", (userId, strategyId))

  def * = (userId, strategyId) <> (UserStrategy.tupled, UserStrategy.unapply)

  def user =
    foreignKey("fk_userstrategies_user", userId, TableQuery[UserTable])(_.id)
  def strategy = foreignKey(
    "fk_userstrategies_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
}
case class StrategyIndicator(strategyId: Long, indicatorId: Long)

class StrategyIndicatorTable(tag: Tag)
    extends Table[StrategyIndicator](tag, "strategyindicator") {
  def strategyId = column[Long]("strategy_id")
  def indicatorId = column[Long]("indicator_id")

  def pk = primaryKey("pk_strategyindicator", (strategyId, indicatorId))

  def * = (
    strategyId,
    indicatorId
  ) <> (StrategyIndicator.tupled, StrategyIndicator.unapply)

  def strategy = foreignKey(
    "fk_strategyindicator_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
  def indicator = foreignKey(
    "fk_strategyindicator_indicator",
    indicatorId,
    TableQuery[IndicatorNameTable]
  )(_.id)
}
case class IndicatorName(id: Option[Long], name: String)

class IndicatorNameTable(tag: Tag)
    extends Table[IndicatorName](tag, "indicatorname") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (IndicatorName.tupled, IndicatorName.unapply)
}
case class StrategyStopLoss(strategyId: Long, stopLossId: Long)

class StrategyStopLossesTable(tag: Tag)
    extends Table[StrategyStopLoss](tag, "strategystoplosses") {
  def strategyId = column[Long]("strategy_id")
  def stopLossId = column[Long]("stoploss_id")

  def pk = primaryKey("pk_strategystoplosses", (strategyId, stopLossId))

  def * = (
    strategyId,
    stopLossId
  ) <> (StrategyStopLoss.tupled, StrategyStopLoss.unapply)

  def strategy = foreignKey(
    "fk_strategystoplosses_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
  def stopLoss = foreignKey(
    "fk_strategystoplosses_stoploss",
    stopLossId,
    TableQuery[StopLossNameTable]
  )(_.id)
}
case class StopLossName(id: Option[Long], name: String)

class StopLossNameTable(tag: Tag)
    extends Table[StopLossName](tag, "stoplossesname") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (StopLossName.tupled, StopLossName.unapply)
}
case class StrategyTakeProfit(strategyId: Long, takeProfitId: Long)

class StrategyTakeProfitTable(tag: Tag)
    extends Table[StrategyTakeProfit](tag, "strategytakeprofit") {
  def strategyId = column[Long]("strategy_id")
  def takeProfitId = column[Long]("takeprofit_id")

  def pk = primaryKey("pk_strategytakeprofit", (strategyId, takeProfitId))

  def * = (
    strategyId,
    takeProfitId
  ) <> (StrategyTakeProfit.tupled, StrategyTakeProfit.unapply)

  def strategy = foreignKey(
    "fk_strategytakeprofit_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
  def takeProfit = foreignKey(
    "fk_strategytakeprofit_takeprofit",
    takeProfitId,
    TableQuery[TakeProfitNameTable]
  )(_.id)
}
case class TakeProfitName(id: Option[Long], name: String)

class TakeProfitNameTable(tag: Tag)
    extends Table[TakeProfitName](tag, "takeprofitname") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (TakeProfitName.tupled, TakeProfitName.unapply)
}
case class StrategyPricePoint(strategyId: Long, pricePointId: Long)

class StrategyPricePointTable(tag: Tag)
    extends Table[StrategyPricePoint](tag, "strategypricepoint") {
  def strategyId = column[Long]("strategy_id")
  def pricePointId = column[Long]("pricepoint_id")

  def pk = primaryKey("pk_strategypricepoint", (strategyId, pricePointId))

  def * = (
    strategyId,
    pricePointId
  ) <> (StrategyPricePoint.tupled, StrategyPricePoint.unapply)

  def strategy = foreignKey(
    "fk_strategypricepoint_strategy",
    strategyId,
    TableQuery[StrategyTable]
  )(_.id)
  def pricePoint = foreignKey(
    "fk_strategypricepoint_pricepoint",
    pricePointId,
    TableQuery[PricePointNameTable]
  )(_.id)
}
case class PricePointName(id: Option[Long], name: String)

class PricePointNameTable(tag: Tag)
    extends Table[PricePointName](tag, "pricepointname") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")

  def * = (id.?, name) <> (PricePointName.tupled, PricePointName.unapply)
}
