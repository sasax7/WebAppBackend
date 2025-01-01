file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala
### java.lang.IndexOutOfBoundsException: 0

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 2.13.12
Classpath:
<WORKSPACE>\conf [exists ], <WORKSPACE>\.bloop\root\bloop-bsp-clients-classes\classes-Metals-v0X0-SSbSXmMPza17mlJ0g== [exists ], <HOME>\AppData\Local\bloop\cache\semanticdb\com.sourcegraph.semanticdb-javac.0.10.3\semanticdb-javac-0.10.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\twirl-api_2.13\1.5.1\twirl-api_2.13-1.5.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-server_2.13\2.8.20\play-server_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-logback_2.13\2.8.20\play-logback_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-akka-http-server_2.13\2.8.20\play-akka-http-server_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\filters-helpers_2.13\2.8.20\filters-helpers_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-guice_2.13\2.8.20\play-guice_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick_2.13\5.0.0\play-slick_2.13-5.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick-evolutions_2.13\5.0.0\play-slick-evolutions_2.13-5.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\postgresql\postgresql\42.2.23\postgresql-42.2.23.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\fusesource\jansi\jansi\2.4.0\jansi-2.4.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-xml_2.13\1.3.1\scala-xml_2.13-1.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play_2.13\2.8.20\play_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-streams_2.13\2.8.20\play-streams_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-http-core_2.13\10.1.15\akka-http-core_2.13-10.1.15.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\guice\5.1.0\guice-5.1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\extensions\guice-assistedinject\4.2.3\guice-assistedinject-4.2.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick_2.13\3.3.2\slick_2.13-3.3.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick-hikaricp_2.13\3.3.2\slick-hikaricp_2.13-3.3.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-api_2.13\2.8.0\play-jdbc-api_2.13-2.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-evolutions_2.13\2.8.0\play-jdbc-evolutions_2.13-2.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\checkerframework\checker-qual\3.8.0\checker-qual-3.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\build-link\2.8.20\build-link-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jul-to-slf4j\1.7.36\jul-to-slf4j-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jcl-over-slf4j\1.7.36\jcl-over-slf4j-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.21\akka-actor_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor-typed_2.13\2.6.21\akka-actor-typed_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-slf4j_2.13\2.6.21\akka-slf4j_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-serialization-jackson_2.13\2.6.21\akka-serialization-jackson_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-core\2.11.4\jackson-core-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-annotations\2.11.4\jackson-annotations-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.11.4\jackson-datatype-jdk8-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.11.4\jackson-datatype-jsr310-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-databind\2.11.4\jackson-databind-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-json_2.13\2.8.2\play-json_2.13-2.8.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\guava\30.1.1-jre\guava-30.1.1-jre.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt\0.9.1\jjwt-0.9.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\transaction\jakarta.transaction-api\1.3.3\jakarta.transaction-api-1.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\inject\javax.inject\1\javax.inject-1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.2\scala-java8-compat_2.13-1.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\ssl-config-core_2.13\0.4.3\ssl-config-core_2.13-0.4.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-parser-combinators_2.13\1.1.2\scala-parser-combinators_2.13-1.1.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-stream_2.13\2.6.21\akka-stream_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-parsing_2.13\10.1.15\akka-parsing_2.13-10.1.15.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\aopalliance\aopalliance\1.0\aopalliance-1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.2\config-1.4.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-collection-compat_2.13\2.0.0\scala-collection-compat_2.13-2.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\zaxxer\HikariCP\3.2.0\HikariCP-3.2.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-exceptions\2.8.20\play-exceptions-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-parameter-names\2.11.4\jackson-module-parameter-names-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\dataformat\jackson-dataformat-cbor\2.11.4\jackson-dataformat-cbor-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-scala_2.13\2.11.4\jackson-module-scala_2.13-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-functional_2.13\2.8.2\play-functional_2.13-2.8.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-reflect\2.13.12\scala-reflect-2.13.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\joda-time\joda-time\2.10.5\joda-time-2.10.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\errorprone\error_prone_annotations\2.5.1\error_prone_annotations-2.5.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-protobuf-v3_2.13\2.6.21\akka-protobuf-v3_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-paranamer\2.11.4\jackson-module-paranamer-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar [exists ]
Options:
-deprecation -unchecked -encoding utf8 -Yrangepos -Xplugin-require:semanticdb -release 11


action parameters:
offset: 5818
uri: file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala
text:
```scala
package state

import models.{Trade, User, Strategy, StrategyDetails, Candlestick}
import repositories.{CandlesRepository, UsersRepository, StrategyRepository}
import scala.concurrent.{ExecutionContext, Future}
import models.TradeState
import play.api.libs.json._
import play.api.libs.functional.syntax._

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
    currentPair: String,
    balance: Option[BigDecimal],
    timeframe: String,
    spread: Option[BigDecimal],
    fees: Option[BigDecimal],
    percentageRiskPerTrade: Option[BigDecimal],
    currentCandlestick: Option[Candlestick]
) extends BacktestState {

  override def updateTimestamp(newTimestamp: Long)(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository
  ): Future[BacktestState] = {
    candlesRepository
      .getCandlesticksBatchByPairName(currentPair, "1m", newTimestamp, 1)
      .map { candlesticks =>
        candlesticks.headOption match {
          case Some(latestCandle) =>
            val newPrice = BigDecimal(latestCandle.close)
            val updatedTrades = trades.map { trade =>
              val profit = if (trade.isBuy) {
                (newPrice - trade.entryPrice) * trade.size.getOrElse(
                  BigDecimal(1)
                )
              } else {
                (trade.entryPrice - newPrice) * trade.size.getOrElse(
                  BigDecimal(1)
                )
              }

              val newState = trade.dateTriggered match {
                case Some(triggeredDate) if newTimestamp < triggeredDate =>
                  TradeState.NotTriggered
                case Some(triggeredDate) if newTimestamp >= triggeredDate =>
                  if (
                    newPrice <= trade.stopLossPrice || trade.takeProfitPrice
                      .exists(newPrice >= _)
                  ) {
                    TradeState.Closed
                  } else {
                    TradeState.Triggered
                  }
                case None =>
                  TradeState.NotTriggered
                case Some(
                      _
                    ) => // This case handles any other unexpected Some(_) input
                  TradeState.NotTriggered
              }

              trade.copy(currentProfit = Some(profit), state = newState)
            }
            copy(
              newestTimestamp = newTimestamp,
              currentPrice = newPrice,
              trades = updatedTrades,
              currentCandlestick =
                Some(latestCandle) // Update the current candlestick
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
      currentPair: String,
      balance: Option[BigDecimal],
      timeframe: Option[String],
      spread: Option[BigDecimal],
      fees: Option[BigDecimal],
      percentageRiskPerTrade: Option[BigDecimal]
  )(implicit
      ec: ExecutionContext,
      candlesRepository: CandlesRepository,
      usersRepository: UsersRepository,
      strategyRepository: StrategyRepository
  ): Future[OperationalState] = {
    for {
      userOpt <- usersRepository.findUserByFirebaseUid(userFirebaseId)
      strategyOpt <- strategyRepository.findStrategyById(strategyId)
      strategyDetailsOpt <- strategyRepository.findStrategyDetailsById(
        strategyId
      )
      candlestickOpt <- candlesRepository
        .getCandlesticksBatchByPairName(currentPair, "1m", newestTimestamp, 1)
        .map(_.headOption)
    } yield {
      (userOpt, strategyOpt, strategyDetailsOpt, candlestickOpt) match {
        case (
              Some(user),
              Some(strategy),
              Some(strategyDetails),
              Some(candlestick)
            ) =>
          OperationalState(
            newestTimestamp = newestTimestamp,
            trades = Seq.empty,
            currentPrice = BigDecimal(candlestick.close),
            strategy = Some(strategy),
            strategyDetails = Some(strategyDetails),
            user = Some(user),
            currentPair = currentPair,
            balance = balance,
            timeframe = timeframe.getOrElse("1m"),
            spread = spread.orElse(Some(BigDecimal(0))),
            fees = fees.orElse(Some(BigDecimal(0))),
            percentageRiskPerTrade = percentageRiskPerTrade.orElse(Some(BigDecimal(0))),@@
            currentCandlestick = Some(candlestick)
          )
        case _ =>
          throw new Exception("Invalid user, strategy, or candlestick data")
      }
    }
  }
}

```



#### Error stacktrace:

```
scala.collection.LinearSeqOps.apply(LinearSeq.scala:131)
	scala.collection.LinearSeqOps.apply$(LinearSeq.scala:128)
	scala.collection.immutable.List.apply(List.scala:79)
	scala.meta.internal.pc.SignatureHelpProvider.toSignatureHelp(SignatureHelpProvider.scala:481)
	scala.meta.internal.pc.SignatureHelpProvider.$anonfun$signatureHelp$1(SignatureHelpProvider.scala:28)
	scala.Option.map(Option.scala:242)
	scala.meta.internal.pc.SignatureHelpProvider.signatureHelp(SignatureHelpProvider.scala:28)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$signatureHelp$1(ScalaPresentationCompiler.scala:339)
```
#### Short summary: 

java.lang.IndexOutOfBoundsException: 0