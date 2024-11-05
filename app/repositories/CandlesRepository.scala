package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import models.{Candlestick, CandlestickTable, Pair, PairTable}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import java.sql.Timestamp

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
    val insertQuery =
      (candlesticks returning candlesticks.map(_.id) into ((candlestick, id) =>
        candlestick.copy(id = Some(id))
      )) ++= candlesticksList.map(_.copy(id = None))

    db.run(insertQuery)
  }

  def listPairs(): Future[Seq[Pair]] = {
    db.run(pairs.result)
  }

  def createPair(pair: Pair): Future[Pair] = {
    db.run(
      (pairs returning pairs.map(_.id) into ((pair, id) =>
        pair.copy(id = Some(id))
      )) += pair.copy(id = None)
    )
  }

  def findPairById(id: Long): Future[Option[Pair]] = {
    db.run(pairs.filter(_.id === id).result.headOption)
  }

  def deletePair(id: Long): Future[Int] = {
    db.run(pairs.filter(_.id === id).delete)
  }

  def findCandlesticksByCriteria(
      pairName: Option[String],
      timeframe: Option[String],
      from: Option[Timestamp],
      to: Option[Timestamp]
  ): Future[Seq[Candlestick]] = {
    val query = candlesticks.filter { candlestick =>
      List(
        pairName.map(name =>
          candlestick.pairId in pairs.filter(_.pairName === name).map(_.id)
        ),
        timeframe.map(tf => candlestick.timeframe === tf),
        from.map(f => candlestick.time >= f),
        to.map(t => candlestick.time <= t)
      ).collect({ case Some(criteria) => criteria })
        .reduceLeftOption(_ && _)
        .getOrElse(true: Rep[Boolean])
    }

    db.run(query.result)
  }
}
