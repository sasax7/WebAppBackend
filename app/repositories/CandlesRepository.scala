package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import models.{Candlestick, CandlestickTable, PairTable}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
@Singleton
class CandlesRepository @Inject() (
    @NamedDatabase("default") dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  private val candlesticks = TableQuery[CandlestickTable]
  private val pairs = TableQuery[PairTable]

  def listCandlesticks(): Future[Seq[Candlestick]] = {
    db.run(candlesticks.result)
  }

  def createCandlesticks(
      candlesticksList: Seq[Candlestick]
  ): Future[Seq[Candlestick]] = {
    val insertOrUpdateAction = DBIO.sequence(
      candlesticksList.map(c => candlesticks.insertOrUpdate(c))
    )
    db.run(insertOrUpdateAction).map(_ => candlesticksList)
  }
  def getCandlesticksBatchByPairName(
      pairName: String,
      timeframe: String,
      toTime: Long,
      batchSize: Int
  ): Future[Seq[Candlestick]] = {

    val query = for {
      (c, p) <- candlesticks join pairs on (_.pair_id === _.id)
      if p.pairName === pairName &&
        c.timeframe === timeframe &&
        c.time < toTime
    } yield c

    val sortedQuery = query.sortBy(_.time.desc).take(batchSize)

    db.run(sortedQuery.result).map(_.reverse)
  }

  def deleteCandlesticksByPairId(pair_id: Long): Future[Int] = {
    db.run(candlesticks.filter(_.pair_id === pair_id).delete)
  }
  def aggregateAndUpdateCandlesticks(
      oneMinCandlesticks: Seq[Candlestick]
  ): Future[Unit] = {
    val timeframes = Seq("5m", "15m", "30m", "1h", "4h", "1d")
    val timeframeDurations = Map(
      "5m" -> 5 * 60 * 1000,
      "15m" -> 15 * 60 * 1000,
      "30m" -> 30 * 60 * 1000,
      "1h" -> 60 * 60 * 1000,
      "4h" -> 4 * 60 * 60 * 1000,
      "1d" -> 24 * 60 * 60 * 1000
    )

    val baseTimeReference = 0L // Unix epoch as the base reference

    val actions = timeframes.flatMap { timeframe =>
      val duration = timeframeDurations(timeframe)

      oneMinCandlesticks
        .groupBy(c =>
          (
            c.pair_id,
            ((c.time - baseTimeReference) / duration) * duration + baseTimeReference
          )
        )
        .map { case ((pairId, aggregatedTime), candles) =>
          // Debug: Log grouping details for the timeframe
          println(
            s"Aggregating for timeframe $timeframe: Pair $pairId, Time $aggregatedTime, Candles Count: ${candles.size}"
          )

          val aggregatedCandle = Candlestick(
            open = candles.minBy(_.time).open,
            high = candles.map(_.high).max,
            low = candles.map(_.low).min,
            close = candles.maxBy(_.time).close,
            time = aggregatedTime,
            timeframe = timeframe,
            volume = candles.flatMap(_.volume).reduceOption(_ + _),
            pair_id = pairId
          )

          val existingCandleQuery = candlesticks
            .filter(c =>
              c.pair_id === pairId &&
                c.timeframe === timeframe &&
                c.time === aggregatedTime
            )

          val upsertAction = existingCandleQuery.result.headOption.flatMap {
            case Some(existingCandle) =>
              // Merge logic: combine existing data with new partial aggregate
              val mergedHigh =
                math.max(existingCandle.high, aggregatedCandle.high)
              val mergedLow = math.min(existingCandle.low, aggregatedCandle.low)

              // For volume, sum them if both are defined, or take whichever is defined
              val mergedVolume =
                (existingCandle.volume, aggregatedCandle.volume) match {
                  case (Some(ev), Some(av)) => Some(ev + av)
                  case (Some(ev), None)     => Some(ev)
                  case (None, Some(av))     => Some(av)
                  case (None, None)         => None
                }

              // The close should be from the latest available candle in this batch
              // and open from the earliest available candle in this batch,
              // but if we're missing data, we can choose to only update fields we're sure about.
              val mergedCandle = existingCandle.copy(
                open =
                  existingCandle.open, // or aggregatedCandle.open if you want to refresh the open from this batch
                high = mergedHigh,
                low = mergedLow,
                close =
                  aggregatedCandle.close, // close from the latest candle we aggregated this time
                volume = mergedVolume
              )

              existingCandleQuery.update(mergedCandle)

            case None =>
              // No existing candle, insert the new one
              candlesticks += aggregatedCandle
          }

          upsertAction
        }
    }

    db.run(DBIO.sequence(actions).transactionally).map(_ => ())
  }

}
