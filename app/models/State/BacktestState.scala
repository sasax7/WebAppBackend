package state

import models._
import repositories.{
  CandlesRepository,
  UsersRepository,
  StrategyRepository,
  AdvancedTradeRepository,
  PairsRepository
}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.math.abs
import java.sql.Timestamp

trait BacktestState {
  def updateTimestamp(newTimestamp: Long)(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository
  ): Future[BacktestState]
  def addTrade(trade: Trade): BacktestState
}

case class OperationalState(
    newestTimestamp: Long,
    trades: Seq[Trade],
    currentPrice: BigDecimal,
    strategy: Option[Strategy],
    strategyDetails: Option[StrategyDetails],
    user: Option[User],
    currentPair: Pair,
    balance: Option[BigDecimal],
    timeframe: String,
    spread: Option[BigDecimal],
    fees: Option[BigDecimal],
    percentageRiskPerTrade: Option[BigDecimal],
    currentCandlestick: Option[Candlestick],
    nextCandlesticks: Seq[Candlestick] = Seq.empty,
    latestTradeId: Option[Long] = None,
    chartDrawings: JsValue
) extends BacktestState {
  def saveChartDrawings(drawings: JsValue): OperationalState = {
    copy(chartDrawings = drawings)
  }
  def addTakeProfit(
      price: Double,
      tradeId: Option[Long] = None,
      takeProfitNameId: Long
  )(implicit
      ec: ExecutionContext,
      advancedTradeRepository: AdvancedTradeRepository
  ): Future[OperationalState] = {

    val pairId = currentPair.id.getOrElse(1L)
    val idToUse = tradeId.orElse(latestTradeId)
    idToUse match {
      case Some(id) =>
        val dateTradeTriggered = trades
          .find(_.addAdvancedTradeObject.exists(_.trade.id.contains(id)))
          .flatMap(_.addAdvancedTradeObject.flatMap(_.trade.triggeredDate))
          .getOrElse(
            new Timestamp(newestTimestamp)
          ) // Default to newestTimestamp if not found

        advancedTradeRepository
          .addTakeProfit(
            price,
            id,
            takeProfitNameId,
            pairId,
            dateTradeTriggered
          )
          .map { case (takeProfit, rrs) =>
            val updatedTrades = trades.map { trade =>
              if (
                trade.addAdvancedTradeObject.exists(_.trade.id.contains(id))
              ) {
                val updatedTradeObject = trade.addAdvancedTradeObject.map {
                  ato =>
                    ato.copy(
                      takeProfits = ato.takeProfits :+ takeProfit,
                      tradeRRs = ato.tradeRRs ++ rrs
                    )
                }
                trade.copy(addAdvancedTradeObject = updatedTradeObject)
              } else {
                trade
              }
            }
            copy(trades = updatedTrades)
          }
      case None =>
        Future.failed(
          new IllegalArgumentException(
            "No tradeId provided and no latestTradeId available"
          )
        )
    }
  }

  def addStopLoss(
      price: Double,
      tradeId: Option[Long] = None,
      stopLossNameId: Long
  )(implicit
      ec: ExecutionContext,
      advancedTradeRepository: AdvancedTradeRepository
  ): Future[OperationalState] = {

    val pairId = currentPair.id.getOrElse(1L)
    val idToUse = tradeId.orElse(latestTradeId)
    idToUse match {
      case Some(id) =>
        val dateTradeTriggered = trades
          .find(_.addAdvancedTradeObject.exists(_.trade.id.contains(id)))
          .flatMap(_.addAdvancedTradeObject.flatMap(_.trade.triggeredDate))
          .getOrElse(
            new Timestamp(newestTimestamp)
          ) // Default to newestTimestamp if not found

        advancedTradeRepository
          .addStopLoss(price, id, stopLossNameId, pairId, dateTradeTriggered)
          .map { case (stopLoss, rrs) =>
            val updatedTrades = trades.map { trade =>
              if (
                trade.addAdvancedTradeObject.exists(_.trade.id.contains(id))
              ) {
                val updatedTradeObject = trade.addAdvancedTradeObject.map {
                  ato =>
                    ato.copy(
                      stopLosses = ato.stopLosses :+ stopLoss,
                      tradeRRs = ato.tradeRRs ++ rrs
                    )
                }
                trade.copy(addAdvancedTradeObject = updatedTradeObject)
              } else {
                trade
              }
            }
            copy(trades = updatedTrades)
          }
      case None =>
        Future.failed(
          new IllegalArgumentException(
            "No tradeId provided and no latestTradeId available"
          )
        )
    }
  }
  def addPricePoint(
      value: Double,
      time: Timestamp,
      pricePointNameId: Long,
      tradeId: Option[Long] = None
  )(implicit
      ec: ExecutionContext,
      advancedTradeRepository: AdvancedTradeRepository
  ): Future[OperationalState] = {
    val idToUse = tradeId.orElse(latestTradeId)
    idToUse match {
      case Some(id) =>
        advancedTradeRepository
          .addPricePoint(value, time, pricePointNameId, id)
          .map { pricePoint =>
            val updatedTrades = trades.map { trade =>
              if (
                trade.addAdvancedTradeObject.exists(_.trade.id.contains(id))
              ) {
                val updatedTradeObject = trade.addAdvancedTradeObject.map {
                  ato =>
                    ato.copy(pricePoints = ato.pricePoints :+ pricePoint)
                }
                trade.copy(addAdvancedTradeObject = updatedTradeObject)
              } else {
                trade
              }
            }
            copy(trades = updatedTrades)
          }
      case None =>
        Future.failed(
          new IllegalArgumentException(
            "No tradeId provided and no latestTradeId available"
          )
        )
    }
  }

  def addIndicator(
      value: Double,
      time: Option[Timestamp],
      indicatorNameId: Long,
      tradeId: Option[Long] = None
  )(implicit
      ec: ExecutionContext,
      advancedTradeRepository: AdvancedTradeRepository
  ): Future[OperationalState] = {
    val idToUse = tradeId.orElse(latestTradeId)
    val timeToUse = time.getOrElse(new Timestamp(newestTimestamp))
    idToUse match {
      case Some(id) =>
        advancedTradeRepository
          .addIndicator(value, Some(timeToUse), indicatorNameId, id)
          .map { indicator =>
            val updatedTrades = trades.map { trade =>
              if (
                trade.addAdvancedTradeObject.exists(_.trade.id.contains(id))
              ) {
                val updatedTradeObject = trade.addAdvancedTradeObject.map {
                  ato =>
                    ato.copy(indicators = ato.indicators :+ indicator)
                }
                trade.copy(addAdvancedTradeObject = updatedTradeObject)
              } else {
                trade
              }
            }
            copy(trades = updatedTrades)
          }
      case None =>
        Future.failed(
          new IllegalArgumentException(
            "No tradeId provided and no latestTradeId available"
          )
        )
    }
  }
  def addTradeAsync(
      tradeData: AddTradeRequest
  )(implicit
      ec: ExecutionContext,
      advancedTradeRepository: AdvancedTradeRepository
  ): Future[OperationalState] = {
    val state = this
    (for {
      sId <- state.strategy.flatMap(_.id)
      pId <- state.currentPair.id
    } yield {
      advancedTradeRepository
        .addAdvancedTrade(
          start_date = new Timestamp(state.newestTimestamp),
          entry_price = tradeData.entryPrice,
          stop_loss = tradeData.stopLoss,
          strategy_id = sId,
          pair_id = pId
        )
        .flatMap {
          case Some(newTradeId) =>
            // Use the new method to fetch the full AdvancedTradeObject
            advancedTradeRepository
              .getTradeObjectById(newTradeId)
              .map {
                case Some(dbTradeObject) =>
                  val newTrade = OperationalState.convertToTrade(dbTradeObject)(
                    state.newestTimestamp,
                    state.balance,
                    state.percentageRiskPerTrade,
                    state.currentPrice
                  )
                  state.copy(
                    trades = state.trades :+ newTrade,
                    latestTradeId = Some(newTradeId)
                  )
                case None =>
                  state
              }

          case None =>
            Future.successful(state)
        }
    }).getOrElse(Future.successful(state))
  }
  override def updateTimestamp(newTimestamp: Long)(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository
  ): Future[BacktestState] = {
    val oldTimestamp = newestTimestamp
    for {
      candlesticks <- candlesRepository.getCandlesticksBatchByPairName(
        currentPair.pairName,
        "1m",
        newTimestamp,
        1
      )
      nextCandlesticks <- candlesRepository.getCandlesticksByTimeRange(
        currentPair.pairName,
        "1m",
        oldTimestamp,
        newTimestamp
      )
    } yield {
      candlesticks.headOption match {
        case Some(latestCandle) =>
          val newPrice = BigDecimal(latestCandle.close)
          val updatedTrades = trades.map { trade =>
            val profit =
              if (trade.addAdvancedTradeObject.exists(_.trade.isBuy)) {
                (newPrice - trade.addAdvancedTradeObject.get.trade.entryPrice) * trade.size
                  .getOrElse(BigDecimal(1))
              } else {
                (trade.addAdvancedTradeObject.get.trade.entryPrice - newPrice) * trade.size
                  .getOrElse(BigDecimal(1))
              }

            val newState = (
              trade.addAdvancedTradeObject.flatMap(_.trade.triggeredDate),
              trade.addAdvancedTradeObject.flatMap(
                _.stopLosses.headOption.flatMap(_.timeHit)
              ),
              trade.addAdvancedTradeObject.flatMap(
                _.takeProfits.headOption.flatMap(_.timeHit)
              )
            ) match {
              case (Some(triggeredDate), Some(slTimeHit), Some(tpTimeHit))
                  if newTimestamp >= triggeredDate.getTime =>
                if (
                  newTimestamp >= slTimeHit.getTime || newTimestamp >= tpTimeHit.getTime
                ) {
                  TradeState.Closed
                } else {
                  TradeState.Triggered
                }
              case (Some(triggeredDate), Some(slTimeHit), None)
                  if newTimestamp >= triggeredDate.getTime =>
                if (newTimestamp >= slTimeHit.getTime) {
                  TradeState.Closed
                } else {
                  TradeState.Triggered
                }
              case (Some(triggeredDate), None, Some(tpTimeHit))
                  if newTimestamp >= triggeredDate.getTime =>
                if (newTimestamp >= tpTimeHit.getTime) {
                  TradeState.Closed
                } else {
                  TradeState.Triggered
                }
              case (Some(triggeredDate), None, None)
                  if newTimestamp >= triggeredDate.getTime =>
                TradeState.Triggered
              case _ =>
                TradeState.NotTriggered
            }

            val currentProfit = newState match {
              case TradeState.NotTriggered => BigDecimal(0)
              case TradeState.Triggered =>
                if (trade.addAdvancedTradeObject.exists(_.trade.isBuy)) {
                  (newPrice - trade.addAdvancedTradeObject.get.trade.entryPrice) * trade.size
                    .getOrElse(BigDecimal(1))
                } else {
                  (trade.addAdvancedTradeObject.get.trade.entryPrice - newPrice) * trade.size
                    .getOrElse(BigDecimal(1))
                }
              case TradeState.Closed => BigDecimal(0)
            }

            trade.copy(currentProfit = Some(currentProfit), state = newState)
          }
          copy(
            newestTimestamp = newTimestamp,
            currentPrice = newPrice,
            trades = updatedTrades,
            currentCandlestick =
              Some(latestCandle), // Update the current candlestick
            nextCandlesticks = nextCandlesticks // Update the next candlesticks
          )
        case None =>
          this // No update if no candlestick data is available
      }
    }
  }
  override def addTrade(trade: Trade): BacktestState =
    copy(trades = trades :+ trade)
}

object OperationalState {
  import models.Strategy.strategyFormat // Import the implicit format for Strategy
  import models.StrategyDetails.format // Import the implicit format for StrategyDetails
  import models.User.userFormat // Import the implicit format for User
  import models.Trade.format // Import the implicit format for Trade
  import models.Candlestick.format // Import the implicit format for Candlestick

  implicit val operationalStateWrites: OWrites[OperationalState] =
    Json.writes[OperationalState]
  implicit val operationalStateReads: Reads[OperationalState] =
    Json.reads[OperationalState]
  implicit val operationalStateFormat: OFormat[OperationalState] =
    OFormat(operationalStateReads, operationalStateWrites)

  def initialize(
      newestTimestamp: Long,
      strategyId: Long,
      userFirebaseId: String,
      currentPairName: String,
      balance: Option[BigDecimal],
      timeframe: Option[String],
      spread: Option[BigDecimal],
      fees: Option[BigDecimal],
      percentageRiskPerTrade: Option[BigDecimal]
  )(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository,
      usersRepository: UsersRepository,
      strategyRepository: StrategyRepository,
      advancedTradeRepository: AdvancedTradeRepository,
      pairsRepository: PairsRepository
  ): Future[OperationalState] = {
    for {
      userOpt <- usersRepository.findUserByFirebaseUid(userFirebaseId)
      strategyOpt <- strategyRepository.findStrategyById(strategyId)
      strategyDetailsOpt <- strategyRepository.findStrategyDetailsById(
        strategyId
      )
      pairOpt <- pairsRepository.getPairIdByName(currentPairName).flatMap {
        case Some(pairId) => pairsRepository.findPairById(pairId)
        case None         => Future.successful(None)
      }
      candlestickOpt <- candlesRepository
        .getCandlesticksBatchByPairName(
          currentPairName,
          "1m",
          newestTimestamp,
          1
        )
        .map(_.headOption)
      advancedTrades <- advancedTradeRepository.getTradesByStrategyIdObject(
        strategyId
      )
    } yield {
      val currentPrice = candlestickOpt.map(_.close).getOrElse(0.0)
      val trades = advancedTrades.map { trade =>
        convertToTrade(trade)(
          newestTimestamp,
          balance,
          percentageRiskPerTrade,
          BigDecimal(currentPrice)
        )
      }

      val finalBalance =
        trades.foldLeft(balance.getOrElse(BigDecimal(100000))) {
          (balance, trade) =>
            trade.state match {
              case TradeState.Closed =>
                val riskAmount =
                  balance * percentageRiskPerTrade.getOrElse(BigDecimal(0.02))
                val stopLossHitTime = trade.addAdvancedTradeObject.flatMap(
                  _.stopLosses.headOption.flatMap(_.timeHit)
                )
                val takeProfitHitTime = trade.addAdvancedTradeObject.flatMap(
                  _.takeProfits.headOption.flatMap(_.timeHit)
                )

                (stopLossHitTime, takeProfitHitTime) match {
                  case (Some(slTime), Some(tpTime)) =>
                    if (tpTime.before(slTime)) {
                      balance + riskAmount
                    } else {
                      balance - riskAmount
                    }
                  case (Some(_), None) =>
                    trade.addAdvancedTradeObject.flatMap(
                      _.stopLosses.headOption
                    ) match {
                      case Some(stopLoss) if stopLoss.hit1RR =>
                        balance + riskAmount
                      case _ =>
                        balance - riskAmount
                    }
                  case (None, Some(_)) =>
                    balance + riskAmount
                  case (None, None) =>
                    balance
                }
              case _ => balance
            }
        }
      (
        userOpt,
        strategyOpt,
        strategyDetailsOpt,
        pairOpt,
        candlestickOpt
      ) match {
        case (
              Some(user),
              Some(strategy),
              Some(strategyDetails),
              Some(pair),
              Some(candlestick)
            ) =>
          OperationalState(
            newestTimestamp = newestTimestamp,
            trades = trades,
            currentPrice = BigDecimal(candlestick.close),
            strategy = Some(strategy),
            strategyDetails = Some(strategyDetails),
            user = Some(user),
            currentPair = pair,
            balance = Some(finalBalance),
            timeframe = timeframe.getOrElse("1m"),
            spread = spread.orElse(Some(BigDecimal(0))),
            fees = fees.orElse(Some(BigDecimal(0))),
            percentageRiskPerTrade =
              percentageRiskPerTrade.orElse(Some(BigDecimal(0.02))),
            currentCandlestick = Some(candlestick),
            chartDrawings = Json.obj()
          )
        case _ =>
          throw new Exception(
            "Invalid user, strategy, pair, or candlestick data"
          )
      }
    }
  }
  def convertToTrade(
      advancedTradeObject: AdvancedTradeObject
  )(implicit
      newestTimestamp: Long,
      balance: Option[BigDecimal],
      percentageRiskPerTrade: Option[BigDecimal],
      currentPrice: BigDecimal
  ): Trade = {
    val advancedTrade = advancedTradeObject.trade
    val stopLoss = advancedTradeObject.stopLosses.headOption
    val takeProfit = advancedTradeObject.takeProfits.headOption

    val state = (advancedTrade.triggeredDate, stopLoss, takeProfit) match {
      case (Some(triggeredDate), Some(sl), Some(tp))
          if newestTimestamp >= triggeredDate.getTime =>
        if (
          newestTimestamp >= sl.timeHit
            .map(_.getTime)
            .getOrElse(Long.MaxValue) || newestTimestamp >= tp.timeHit
            .map(_.getTime)
            .getOrElse(Long.MaxValue)
        ) {
          TradeState.Closed
        } else {
          TradeState.Triggered
        }
      case (Some(triggeredDate), Some(sl), None)
          if newestTimestamp >= triggeredDate.getTime =>
        if (
          newestTimestamp >= sl.timeHit.map(_.getTime).getOrElse(Long.MaxValue)
        ) {
          TradeState.Closed
        } else {
          TradeState.Triggered
        }
      case (Some(triggeredDate), None, Some(tp))
          if newestTimestamp >= triggeredDate.getTime =>
        if (
          newestTimestamp >= tp.timeHit.map(_.getTime).getOrElse(Long.MaxValue)
        ) {
          TradeState.Closed
        } else {
          TradeState.Triggered
        }
      case (Some(triggeredDate), None, None)
          if newestTimestamp >= triggeredDate.getTime =>
        TradeState.Triggered
      case _ =>
        TradeState.NotTriggered
    }

    // Convert any numeric values to BigDecimal explicitly:
    val riskPercentage = percentageRiskPerTrade.getOrElse(BigDecimal(0.02))
    val balanceValue = balance.getOrElse(BigDecimal(100000))
    val entryPrice = advancedTrade.entryPrice
    val exitPrice =
      stopLoss.map(sl => BigDecimal(sl.price)).getOrElse(BigDecimal(0))

    val difference: BigDecimal = (entryPrice - exitPrice).abs
    val size: BigDecimal =
      if (difference > 0) (balanceValue * riskPercentage) / difference
      else BigDecimal(0)

    val currentProfit = state match {
      case TradeState.NotTriggered => BigDecimal(0)
      case TradeState.Triggered =>
        if (advancedTrade.isBuy) {
          (currentPrice - entryPrice) * size
        } else {
          (entryPrice - currentPrice) * size
        }
      case TradeState.Closed => BigDecimal(0)
    }
    Trade(
      addAdvancedTradeObject = Some(advancedTradeObject),
      currentProfit = Some(
        currentProfit
      ),
      size = Some(size), // Calculated size
      state = state // Determined state
    )
  }

}
