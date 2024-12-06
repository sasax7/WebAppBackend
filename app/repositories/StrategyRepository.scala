package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.StrategyDetails
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import models._
import models.Strategy._

@Singleton
class StrategyRepository @Inject() (
    dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  private val strategies = TableQuery[StrategyTable]
  private val userStrategies = TableQuery[UserStrategiesTable]
  private val users = TableQuery[UserTable]
  private val strategyIndicators = TableQuery[StrategyIndicatorTable]
  private val indicatorNames = TableQuery[IndicatorNameTable]
  private val strategyStopLosses = TableQuery[StrategyStopLossesTable]
  private val stopLossNames = TableQuery[StopLossNameTable]
  private val strategyTakeProfits = TableQuery[StrategyTakeProfitTable]
  private val takeProfitNames = TableQuery[TakeProfitNameTable]
  private val strategyPricePoints = TableQuery[StrategyPricePointTable]
  private val pricePointNames = TableQuery[PricePointNameTable]
  def addStrategy(firebaseUid: String, name: String): Future[Option[Long]] = {
    val action = for {
      // Find user by firebaseUid
      userOpt <- users.filter(_.firebaseUid === firebaseUid).result.headOption
      strategyIdOpt <- userOpt match {
        case Some(user) =>
          // Insert the new strategy and get the generated ID
          (strategies returning strategies.map(_.id))
            .+=(Strategy(None, name))
            .flatMap { strategyId =>
              // Link the user to the new strategy and return the strategyId
              (userStrategies += UserStrategy(user.id.get, strategyId))
                .map(_ =>
                  strategyId
                ) // Ignore the DBIO[Int] and return the strategyId
            }
            .map(Some(_)) // Wrap the strategyId in Some
        case None =>
          // User not found
          DBIO.successful(None)
      }
    } yield strategyIdOpt

    db.run(action.transactionally)
  }
  def addIndicator(strategyId: Long, name: String): Future[Option[Long]] = {
    val action = for {
      strategyExists <- strategies.filter(_.id === strategyId).exists.result
      indicatorId <-
        if (strategyExists) {
          // Insert the name and get its ID
          (indicatorNames returning indicatorNames.map(_.id))
            .+=(IndicatorName(None, name))
            .flatMap { id =>
              // Link to strategy
              (strategyIndicators += StrategyIndicator(strategyId, id)).map(_ =>
                id
              )
            }
            .map(Some(_)) // Wrap ID in Some
        } else DBIO.successful(None)
    } yield indicatorId

    db.run(action.transactionally)
  }

  def addStopLoss(strategyId: Long, name: String): Future[Option[Long]] = {
    val action = for {
      strategyExists <- strategies.filter(_.id === strategyId).exists.result
      stopLossId <-
        if (strategyExists) {
          (stopLossNames returning stopLossNames.map(_.id))
            .+=(StopLossName(None, name))
            .flatMap { id =>
              (strategyStopLosses += StrategyStopLoss(strategyId, id)).map(_ =>
                id
              )
            }
            .map(Some(_))
        } else DBIO.successful(None)
    } yield stopLossId

    db.run(action.transactionally)
  }

  def addTakeProfit(strategyId: Long, name: String): Future[Option[Long]] = {
    val action = for {
      strategyExists <- strategies.filter(_.id === strategyId).exists.result
      takeProfitId <-
        if (strategyExists) {
          (takeProfitNames returning takeProfitNames.map(_.id))
            .+=(TakeProfitName(None, name))
            .flatMap { id =>
              (strategyTakeProfits += StrategyTakeProfit(strategyId, id)).map(
                _ => id
              )
            }
            .map(Some(_))
        } else DBIO.successful(None)
    } yield takeProfitId

    db.run(action.transactionally)
  }

  def addPricePoint(strategyId: Long, name: String): Future[Option[Long]] = {
    val action = for {
      strategyExists <- strategies.filter(_.id === strategyId).exists.result
      pricePointId <-
        if (strategyExists) {
          (pricePointNames returning pricePointNames.map(_.id))
            .+=(PricePointName(None, name))
            .flatMap { id =>
              (strategyPricePoints += StrategyPricePoint(strategyId, id)).map(
                _ => id
              )
            }
            .map(Some(_))
        } else DBIO.successful(None)
    } yield pricePointId

    db.run(action.transactionally)
  }

  def getStrategiesWithDetailsByFirebaseUid(
      firebaseUid: String
  ): Future[Seq[StrategyDetails]] = {
    val query = for {
      (s, us) <- strategies join userStrategies on (_.id === _.strategyId)
      u <- users if u.id === us.userId && u.firebaseUid === firebaseUid
    } yield s

    val strategiesWithDetailsAction = for {
      strategies <- query.result
      strategyIds = strategies.map(_.id.get)

      indicators <- strategyIndicators
        .filter(_.strategyId inSet strategyIds)
        .join(indicatorNames)
        .on(_.indicatorId === _.id)
        .map { case (si, i) => (si.strategyId, i.name) }
        .result

      stopLosses <- strategyStopLosses
        .filter(_.strategyId inSet strategyIds)
        .join(stopLossNames)
        .on(_.stopLossId === _.id)
        .map { case (ssl, sl) => (ssl.strategyId, sl.name) }
        .result

      takeProfits <- strategyTakeProfits
        .filter(_.strategyId inSet strategyIds)
        .join(takeProfitNames)
        .on(_.takeProfitId === _.id)
        .map { case (stp, tp) => (stp.strategyId, tp.name) }
        .result

      pricePoints <- strategyPricePoints
        .filter(_.strategyId inSet strategyIds)
        .join(pricePointNames)
        .on(_.pricePointId === _.id)
        .map { case (spp, pp) => (spp.strategyId, pp.name) }
        .result
    } yield (strategies, indicators, stopLosses, takeProfits, pricePoints)

    db.run(strategiesWithDetailsAction).map {
      case (strategies, indicators, stopLosses, takeProfits, pricePoints) =>
        strategies.map { strategy =>
          StrategyDetails(
            strategyId = strategy.id.get,
            strategyName = strategy.name,
            indicatorNames = indicators.collect {
              case (strategyId, name) if strategyId == strategy.id.get => name
            },
            stopLossNames = stopLosses.collect {
              case (strategyId, name) if strategyId == strategy.id.get => name
            },
            takeProfitNames = takeProfits.collect {
              case (strategyId, name) if strategyId == strategy.id.get => name
            },
            pricePointNames = pricePoints.collect {
              case (strategyId, name) if strategyId == strategy.id.get => name
            }
          )
        }
    }
  }
}
