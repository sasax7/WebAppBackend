package state

import models.{Trade, User, Strategy, Candlestick}
import repositories.CandlesRepository
import scala.concurrent.{ExecutionContext, Future}

class BacktestContext(var state: BacktestState) {

  def updateTimestamp(newTimestamp: Long)(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository
  ): Future[Unit] = {
    state.updateTimestamp(newTimestamp).map { newState =>
      state = newState
    }
  }

  def addTrade(trade: Trade): Unit = {
    state = state.addTrade(trade)
  }

  def getCurrentState: BacktestState = state
}
