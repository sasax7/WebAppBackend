package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models._
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import java.sql.Timestamp
import models.Candlestick._
import scala.math.abs
import play.api.libs.json._
import slick.lifted.ProvenShape
@Singleton
class AdvancedTradeRepository @Inject() (
    dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[slick.jdbc.JdbcProfile]
  private val db = dbConfig.db

  private val advancedTrades = TableQuery[AdvancedTradeTable]
  private val indicators = TableQuery[IndicatorTable]
  private val pricePoints = TableQuery[PricePointTable]
  private val stopLosses = TableQuery[StopLossTable]
  private val takeProfits = TableQuery[TakeProfitTable]
  private val tradeRRs = TableQuery[TradeRRTable]
  private val candlesticks = TableQuery[CandlestickTable]
  private val pairs = TableQuery[PairTable]
  private val indicatorNames = TableQuery[IndicatorNameTable]
  private val pricePointNames = TableQuery[PricePointNameTable]
  private val stopLossNames = TableQuery[StopLossNameTable]
  private val takeProfitNames = TableQuery[TakeProfitNameTable]

  def addAdvancedTrade(
      start_date: Timestamp,
      entry_price: BigDecimal,
      stop_loss: BigDecimal,
      strategy_id: Long,
      pair_id: Long
  ): Future[Option[Long]] = {
    val is_buy = entry_price > stop_loss

    val triggeredDateQuery = candlesticks
      .filter(c =>
        c.pair_id === pair_id && c.timeframe === "1m" && c.time >= start_date.getTime
      )
      .filter { c =>
        if (is_buy) c.low <= entry_price.toDouble
        else c.high >= entry_price.toDouble
      }
      .sortBy(_.time.asc)
      .map(_.time)
      .result
      .headOption

    val timeHitQuery = candlesticks
      .filter(c =>
        c.pair_id === pair_id && c.timeframe === "1m" && c.time >= start_date.getTime
      )
      .filter { c =>
        if (is_buy) c.low <= stop_loss.toDouble
        else c.high >= stop_loss.toDouble
      }
      .sortBy(_.time.asc)
      .map(_.time)
      .result
      .headOption

    def highestOrLowestPriceQuery(
        startTime: Long,
        endTime: Long,
        isBuy: Boolean
    ): DBIO[Double] = {
      val query = candlesticks
        .filter(c =>
          c.pair_id === pair_id &&
            c.timeframe === "1m" &&
            c.time >= startTime &&
            c.time <= endTime
        )

      val priceQuery =
        if (isBuy) query.map(_.high).max.result
        else query.map(_.low).min.result

      priceQuery.map {
        case Some(price) => price
        case None        => entry_price.toDouble
      }
    }

    def getNextCandleOpen(startTime: Long): DBIO[Option[Double]] = {
      candlesticks
        .filter(c =>
          c.pair_id === pair_id &&
            c.timeframe === "1m" &&
            c.time > startTime
        )
        .sortBy(_.time.asc)
        .map(_.open)
        .result
        .headOption
    }

    val action = for {
      triggeredDateOpt <- triggeredDateQuery
      timeHitOpt <- timeHitQuery

      effectiveEntryPrice <- triggeredDateOpt match {
        case Some(triggeredTime) if triggeredTime == start_date.getTime =>
          getNextCandleOpen(triggeredTime).map(
            _.getOrElse(entry_price.toDouble)
          )
        case _ =>
          DBIO.successful(entry_price.toDouble)
      }

      highestPrice <- triggeredDateOpt match {
        case Some(triggeredTime) =>
          val endTime = timeHitOpt.getOrElse(System.currentTimeMillis())
          highestOrLowestPriceQuery(triggeredTime, endTime, is_buy)
        case None =>
          DBIO.successful(entry_price.toDouble)
      }

      highestRR = {
        val profitDistance = abs(highestPrice - effectiveEntryPrice)
        val riskDistance = abs(effectiveEntryPrice - stop_loss.toDouble)
        if (riskDistance > 0) profitDistance / riskDistance else 0.0
      }

      newTradeId <- (advancedTrades returning advancedTrades.map(
        _.id
      )) += AdvancedTrade(
        id = None,
        startDate = start_date,
        triggeredDate = triggeredDateOpt.map(time => new Timestamp(time)),
        isBuy = is_buy,
        entryPrice = BigDecimal(effectiveEntryPrice),
        strategyId = strategy_id,
        pairId = pair_id
      )

      _ <- stopLosses += StopLoss(
        id = None,
        price = stop_loss.toDouble,
        timeHit = timeHitOpt.map(new java.sql.Timestamp(_)),
        highestPrice = Some(highestPrice),
        highestRR = Some(highestRR),
        hit1RR = highestRR >= 1.0,
        tradeId = newTradeId,
        stopLossNameId = 1
      )
    } yield Some(newTradeId)

    db.run(action.transactionally)
  }
  def getTradesByStrategyId(strategyId: Long): Future[Seq[JsObject]] = {
    val query = for {
      trade <- advancedTrades if trade.strategyId === strategyId
      pair <- pairs if trade.pairId === pair.id
    } yield (trade, pair.pairName)

    val stopLossQuery = for {
      stopLoss <- stopLosses
      stopLossName <- stopLossNames
      if stopLoss.stopLossNameId === stopLossName.id
      trade <- advancedTrades
      if stopLoss.tradeId === trade.id && trade.strategyId === strategyId
    } yield (stopLoss, stopLossName.name, trade)

    val takeProfitQuery = for {
      takeProfit <- takeProfits
      takeProfitName <- takeProfitNames
      if takeProfit.takeProfitNameId === takeProfitName.id
      trade <- advancedTrades
      if takeProfit.tradeId === trade.id && trade.strategyId === strategyId
    } yield (takeProfit, takeProfitName.name, trade)

    val indicatorQuery = for {
      indicator <- indicators
      indicatorName <- indicatorNames
      if indicator.indicatorNameId === indicatorName.id
      trade <- advancedTrades
      if indicator.tradeId === trade.id && trade.strategyId === strategyId
    } yield (indicator, indicatorName.name, trade)

    val pricePointQuery = for {
      pricePoint <- pricePoints
      pricePointName <- pricePointNames
      if pricePoint.pricePointNameId === pricePointName.id
      trade <- advancedTrades
      if pricePoint.tradeId === trade.id && trade.strategyId === strategyId
    } yield (pricePoint, pricePointName.name, trade)

    for {
      trades <- db.run(query.result)
      stopLosses <- db.run(stopLossQuery.result)
      takeProfits <- db.run(takeProfitQuery.result)
      indicators <- db.run(indicatorQuery.result)
      pricePoints <- db.run(pricePointQuery.result)
    } yield {
      trades.map { case (trade, pairName) =>
        val tradeStopLosses = stopLosses.collect {
          case (stopLoss, stopLossName, t) if t.id == trade.id =>
            Json.obj(
              "stopLoss" -> Json.toJson(stopLoss),
              "name" -> stopLossName
            )
        }
        val tradeTakeProfits = takeProfits.collect {
          case (takeProfit, takeProfitName, t) if t.id == trade.id =>
            Json.obj(
              "takeProfit" -> Json.toJson(takeProfit),
              "name" -> takeProfitName
            )
        }
        val tradeIndicators = indicators.collect {
          case (indicator, indicatorName, t) if t.id == trade.id =>
            Json.obj(
              "indicator" -> Json.toJson(indicator),
              "name" -> indicatorName
            )
        }
        val tradePricePoints = pricePoints.collect {
          case (pricePoint, pricePointName, t) if t.id == trade.id =>
            Json.obj(
              "pricePoint" -> Json.toJson(pricePoint),
              "name" -> pricePointName
            )
        }
        Json.obj(
          "trade" -> Json.obj(
            "trade" -> Json.toJson(
              trade
            ), // This will use AdvancedTrade.format and apply the timestampFormat
            "stopLosses" -> tradeStopLosses,
            "takeProfits" -> tradeTakeProfits,
            "indicators" -> tradeIndicators,
            "pricePoints" -> tradePricePoints
          )
        )
      }
    }
  }

}
