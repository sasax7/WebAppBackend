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
class PairsRepository @Inject() (
    @NamedDatabase("default") dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  private val candlesticks = TableQuery[CandlestickTable]
  private val pairs = TableQuery[PairTable]

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

  def getPairIdByName(pairName: String): Future[Option[Long]] = {
    db.run(pairs.filter(_.pairName === pairName).map(_.id).result.headOption)
  }

}
