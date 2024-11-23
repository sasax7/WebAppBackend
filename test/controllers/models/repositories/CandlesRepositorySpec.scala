import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import play.api.db.slick.DatabaseConfigProvider
import models.{Candlestick, Pair}
import repositories.CandlesRepository
import java.sql.Timestamp
import java.time.Instant

class CandlesRepositorySpec
    extends AsyncWordSpec
    with Matchers
    with ScalaFutures {

  // Use GuiceApplicationBuilder to load configurations from application.conf
  implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "play.evolutions.enabled" -> "true", // Enable evolutions for testing
      "play.evolutions.autoApply" -> "true" // Automatically apply evolutions
    )
    .build()

  "CandlesRepository" should {

    "fetch candlesticks in a sorted batch based on pairId, timeframe, and timestamp" in {
      // Set up the repository using the app injector
      val dbConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
      val candlesRepository = new CandlesRepository(dbConfigProvider)

      // Define test data
      val testPair = Pair(
        id = None,
        pairName = "BTC/USDtest",
        baseCurrency = Some("BTCtest"),
        quoteCurrency = Some("USDtest"),
        description = Some("Bitcoin to US Dollar"),
        spread = Some(0.1)
      )

      val now = Timestamp.from(Instant.now)
      val candlesticks = Seq(
        Candlestick(
          None,
          100.0,
          110.0,
          90.0,
          105.0,
          Timestamp.from(Instant.now.minusSeconds(300)),
          "1m",
          Some(500.0),
          0L
        ),
        Candlestick(
          None,
          105.0,
          115.0,
          95.0,
          110.0,
          Timestamp.from(Instant.now.minusSeconds(200)),
          "1m",
          Some(600.0),
          0L
        ),
        Candlestick(
          None,
          110.0,
          120.0,
          100.0,
          115.0,
          Timestamp.from(Instant.now.minusSeconds(100)),
          "1m",
          Some(700.0),
          0L
        )
      )

      // Insert test data into the database
      for {
        savedPair <- candlesRepository.createPair(testPair)
        candlesticksWithPairId = candlesticks.map(
          _.copy(pair_id = savedPair.id.get)
        )
        _ <- candlesRepository.createCandlesticks(candlesticksWithPairId)
        // Query candlesticks for the test pair with specific conditions
        result <- candlesRepository.getCandlesticksBatchByPairId(
          pair_id = savedPair.id.get,
          timeframe = "1m",
          toTime = now,
          batchSize = 2
        )
        // Cleanup the created test data
        _ <- candlesRepository.deleteCandlesticksByPairId(
          savedPair.id.get
        ) // Explicitly delete candlesticks
        _ <- candlesRepository.deletePair(
          savedPair.id.get
        ) // Then delete the pair
      } yield {
        // Assertions
        result.size shouldBe 2
        result.map(_.time).sorted shouldBe result.map(_.time) // Check sorting
        result.head.time.before(result.last.time) shouldBe true
      }
    }
  }
}
