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
        c.time < toTime // toTime is now Long
    } yield c

    val sortedQuery = query.sortBy(_.time.asc).take(batchSize)

    db.run(sortedQuery.result)
  }

  def deleteCandlesticksByPairId(pair_id: Long): Future[Int] = {
    db.run(candlesticks.filter(_.pair_id === pair_id).delete)
  }
}
