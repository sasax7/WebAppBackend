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
  def addTakeProfit(
      price: Double,
      tradeId: Long,
      takeProfitNameId: Long,
      pairId: Long,
      startDate: Timestamp
  ): Future[(TakeProfit, Seq[TradeRR])] = {
    val isBuyQuery =
      advancedTrades.filter(_.id === tradeId).map(_.isBuy).result.head
    val entryPriceQuery =
      advancedTrades.filter(_.id === tradeId).map(_.entryPrice).result.head

    val timeHitQuery = for {
      isBuy <- isBuyQuery
      timeHit <- candlesticks
        .filter(c =>
          c.pair_id === pairId && c.timeframe === "1m" && c.time >= startDate.getTime
        )
        .filter { c =>
          if (isBuy) c.high >= price
          else c.low <= price
        }
        .sortBy(_.time.asc)
        .map(_.time)
        .result
        .headOption
    } yield timeHit

    def lowestPriceQuery(
        startTime: Long,
        endTime: Long,
        isBuy: Boolean
    ): DBIO[Double] = {
      val query = candlesticks
        .filter(c =>
          c.pair_id === pairId &&
            c.timeframe === "1m" &&
            c.time >= startTime &&
            c.time <= endTime
        )

      val priceQuery =
        if (isBuy) query.map(_.low).min.result
        else query.map(_.high).max.result

      priceQuery.map {
        case Some(price) => price
        case None        => price
      }
    }

    def calculateRRs(
        tradeId: Long,
        takeProfitId: Long,
        price: Double,
        isBuy: Boolean,
        entryPrice: BigDecimal
    ): DBIO[Seq[TradeRR]] = {
      val stopLossesQuery = stopLosses.filter(_.tradeId === tradeId).result

      stopLossesQuery.flatMap { stopLosses =>
        val rrs = stopLosses.map { stopLoss =>
          val rr = if (isBuy) {
            if (stopLoss.highestPrice.exists(_ >= price)) {
              (price - stopLoss.price) / (stopLoss.price - entryPrice.toDouble)
            } else {
              -1.0
            }
          } else {
            if (stopLoss.highestPrice.exists(_ <= price)) {
              (stopLoss.price - price) / (entryPrice.toDouble - stopLoss.price)
            } else {
              -1.0
            }
          }

          TradeRR(
            id = None,
            takeProfitId = takeProfitId,
            stopLossId = stopLoss.id.get,
            rr = rr
          )
        }

        (tradeRRs ++= rrs).map(_ => rrs)
      }
    }

    val action = for {
      isBuy <- isBuyQuery
      entryPrice <- entryPriceQuery
      timeHitOpt <- timeHitQuery
      lowestPrice <- timeHitOpt match {
        case Some(timeHit) =>
          lowestPriceQuery(startDate.getTime, timeHit, isBuy)
        case None =>
          DBIO.successful(price)
      }

      newTakeProfit <- (takeProfits returning takeProfits.map(_.id) into (
        (takeProfit, id) => takeProfit.copy(id = Some(id))
      )) += TakeProfit(
        id = None,
        price = price,
        timeHit = timeHitOpt.map(new Timestamp(_)),
        lowestPrice = Some(lowestPrice),
        tradeId = tradeId,
        takeProfitNameId = takeProfitNameId
      )

      rrs <- calculateRRs(
        tradeId,
        newTakeProfit.id.get,
        price,
        isBuy,
        entryPrice
      )
    } yield (newTakeProfit, rrs)

    db.run(action.transactionally)
  }
  def addStopLoss(
      price: Double,
      tradeId: Long,
      stopLossNameId: Long,
      pairId: Long,
      startDate: Timestamp
  ): Future[(StopLoss, Seq[TradeRR])] = {
    val isBuyQuery =
      advancedTrades.filter(_.id === tradeId).map(_.isBuy).result.head
    val entryPriceQuery =
      advancedTrades.filter(_.id === tradeId).map(_.entryPrice).result.head

    val timeHitQuery = for {
      isBuy <- isBuyQuery
      timeHit <- candlesticks
        .filter(c =>
          c.pair_id === pairId && c.timeframe === "1m" && c.time >= startDate.getTime
        )
        .filter { c =>
          if (isBuy) c.low <= price
          else c.high >= price
        }
        .sortBy(_.time.asc)
        .map(_.time)
        .result
        .headOption
    } yield timeHit

    def highestPriceQuery(
        startTime: Long,
        endTime: Long,
        isBuy: Boolean
    ): DBIO[Double] = {
      val query = candlesticks
        .filter(c =>
          c.pair_id === pairId &&
            c.timeframe === "1m" &&
            c.time >= startTime &&
            c.time <= endTime
        )

      val priceQuery =
        if (isBuy) query.map(_.high).max.result
        else query.map(_.low).min.result

      priceQuery.map {
        case Some(price) => price
        case None        => price
      }
    }

    def calculateRRs(
        tradeId: Long,
        stopLossId: Long,
        price: Double,
        isBuy: Boolean,
        entryPrice: BigDecimal
    ): DBIO[Seq[TradeRR]] = {
      val takeProfitsQuery = takeProfits.filter(_.tradeId === tradeId).result

      takeProfitsQuery.flatMap { takeProfits =>
        val rrs = takeProfits.map { takeProfit =>
          val rr = if (isBuy) {
            if (takeProfit.lowestPrice.exists(_ <= price)) {
              (takeProfit.price - price) / (entryPrice.toDouble - price)
            } else {
              -1.0
            }
          } else {
            if (takeProfit.lowestPrice.exists(_ >= price)) {
              (price - takeProfit.price) / (price - entryPrice.toDouble)
            } else {
              -1.0
            }
          }

          TradeRR(
            id = None,
            takeProfitId = takeProfit.id.get,
            stopLossId = stopLossId,
            rr = rr
          )
        }

        (tradeRRs ++= rrs).map(_ => rrs)
      }
    }

    val action = for {
      isBuy <- isBuyQuery
      entryPrice <- entryPriceQuery
      timeHitOpt <- timeHitQuery
      highestPrice <- timeHitOpt match {
        case Some(timeHit) =>
          highestPriceQuery(startDate.getTime, timeHit, isBuy)
        case None =>
          DBIO.successful(price)
      }

      highestRR = {
        val profitDistance = abs(highestPrice - entryPrice.toDouble)
        val riskDistance = abs(entryPrice.toDouble - price)
        if (riskDistance > 0) profitDistance / riskDistance else 0.0
      }

      newStopLoss <- (stopLosses returning stopLosses.map(_.id) into (
        (stopLoss, id) => stopLoss.copy(id = Some(id))
      )) += StopLoss(
        id = None,
        price = price,
        timeHit = timeHitOpt.map(new Timestamp(_)),
        highestPrice = Some(highestPrice),
        highestRR = Some(highestRR),
        hit1RR = highestRR >= 1.0,
        tradeId = tradeId,
        stopLossNameId = stopLossNameId
      )

      rrs <- calculateRRs(tradeId, newStopLoss.id.get, price, isBuy, entryPrice)
    } yield (newStopLoss, rrs)

    db.run(action.transactionally)
  }
  def addIndicator(
      value: Double,
      time: Option[Timestamp],
      indicatorNameId: Long,
      tradeId: Long
  ): Future[Indicator] = {
    val newIndicator = Indicator(
      id = None,
      value = value,
      time = time,
      indicatorNameId = indicatorNameId,
      tradeId = tradeId
    )

    val action =
      (indicators returning indicators.map(_.id) into ((indicator, id) =>
        indicator.copy(id = Some(id))
      )) += newIndicator

    db.run(action)
  }
  def addPricePoint(
      value: Double,
      time: Timestamp,
      pricePointNameId: Long,
      tradeId: Long
  ): Future[PricePoint] = {
    val newPricePoint = PricePoint(
      id = None,
      value = value,
      time = time, // Directly assign the Timestamp
      pricePointNameId = pricePointNameId,
      tradeId = tradeId
    )

    val action =
      (pricePoints returning pricePoints.map(_.id) into ((pricePoint, id) =>
        pricePoint.copy(id = Some(id))
      )) += newPricePoint

    db.run(action)
  }
  def getTradeObjectById(tradeId: Long): Future[Option[AdvancedTradeObject]] = {
    val baseQuery = for {
      t <- advancedTrades if t.id === tradeId
      p <- pairs if t.pairId === p.id
    } yield (t, p.pairName)

    val stopLossQuery = for {
      stopLoss <- stopLosses
      stopLossName <- stopLossNames
      if stopLoss.stopLossNameId === stopLossName.id
      t <- advancedTrades if stopLoss.tradeId === t.id && t.id === tradeId
    } yield (stopLoss, stopLossName.name)

    val takeProfitQuery = for {
      takeProfit <- takeProfits
      takeProfitName <- takeProfitNames
      if takeProfit.takeProfitNameId === takeProfitName.id
      t <- advancedTrades if takeProfit.tradeId === t.id && t.id === tradeId
    } yield (takeProfit, takeProfitName.name)

    val indicatorQuery = for {
      indicator <- indicators
      indicatorName <- indicatorNames
      if indicator.indicatorNameId === indicatorName.id
      t <- advancedTrades if indicator.tradeId === t.id && t.id === tradeId
    } yield (indicator, indicatorName.name)

    val pricePointQuery = for {
      pricePoint <- pricePoints
      pricePointName <- pricePointNames
      if pricePoint.pricePointNameId === pricePointName.id
      t <- advancedTrades if pricePoint.tradeId === t.id && t.id === tradeId
    } yield (pricePoint, pricePointName.name)

    val tradeRRQuery = for {
      tradeRR <- tradeRRs
      stopLoss <- stopLosses if tradeRR.stopLossId === stopLoss.id
      takeProfit <- takeProfits if tradeRR.takeProfitId === takeProfit.id
      t <- advancedTrades
      if t.id === tradeId && stopLoss.tradeId === t.id && takeProfit.tradeId === t.id
    } yield tradeRR

    for {
      trades <- db.run(baseQuery.result)
      stopLossesResult <- db.run(stopLossQuery.result)
      takeProfitsResult <- db.run(takeProfitQuery.result)
      indicatorsResult <- db.run(indicatorQuery.result)
      pricePointsResult <- db.run(pricePointQuery.result)
      tradeRRsResult <- db.run(tradeRRQuery.result)
    } yield {
      trades.headOption.map { case (trade, pairName) =>
        val tradeStopLosses = stopLossesResult.map(_._1)
        val tradeTakeProfits = takeProfitsResult.map(_._1)
        val tradeIndicators = indicatorsResult.map(_._1)
        val tradePricePoints = pricePointsResult.map(_._1)
        AdvancedTradeObject(
          trade = trade,
          stopLosses = tradeStopLosses,
          takeProfits = tradeTakeProfits,
          indicators = tradeIndicators,
          pricePoints = tradePricePoints,
          tradeRRs = tradeRRsResult
        )
      }
    }
  }
  def getTradesByStrategyIdObject(
      strategyId: Long
  ): Future[Seq[AdvancedTradeObject]] = {
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

    val tradeRRQuery = for {
      tradeRR <- tradeRRs
      stopLoss <- stopLosses if tradeRR.stopLossId === stopLoss.id
      takeProfit <- takeProfits if tradeRR.takeProfitId === takeProfit.id
      trade <- advancedTrades
      if stopLoss.tradeId === trade.id && takeProfit.tradeId === trade.id && trade.strategyId === strategyId
    } yield (tradeRR, trade)

    for {
      trades <- db.run(query.result)
      stopLosses <- db.run(stopLossQuery.result)
      takeProfits <- db.run(takeProfitQuery.result)
      indicators <- db.run(indicatorQuery.result)
      pricePoints <- db.run(pricePointQuery.result)
      tradeRRs <- db.run(tradeRRQuery.result)
    } yield {
      trades.map { case (trade, pairName) =>
        val tradeStopLosses = stopLosses.collect {
          case (stopLoss, stopLossName, t) if t.id == trade.id => stopLoss
        }
        val tradeTakeProfits = takeProfits.collect {
          case (takeProfit, takeProfitName, t) if t.id == trade.id => takeProfit
        }
        val tradeIndicators = indicators.collect {
          case (indicator, indicatorName, t) if t.id == trade.id => indicator
        }
        val tradePricePoints = pricePoints.collect {
          case (pricePoint, pricePointName, t) if t.id == trade.id => pricePoint
        }
        val tradeRRsForTrade = tradeRRs.collect {
          case (tradeRR, t) if t.id == trade.id => tradeRR
        }
        AdvancedTradeObject(
          trade = trade,
          stopLosses = tradeStopLosses,
          takeProfits = tradeTakeProfits,
          indicators = tradeIndicators,
          pricePoints = tradePricePoints,
          tradeRRs = tradeRRsForTrade
        )
      }
    }
  }

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
