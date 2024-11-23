file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
### scala.reflect.internal.FatalError: 
  unexpected tree: class scala.reflect.internal.Trees$CaseDef
case Some((pairId @ _)) => Future(getMutex(pairName, timeframe).synchronized({
  val cacheKey = scala.Tuple2(pairName, timeframe);
  val cacheQueue = preloadedCandlesCache.getOrElseUpdate(cacheKey, Queue());
  if (dateOpt.isDefined)
    candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, referenceTimestamp, totalBatchSize).map(((candles) => {
      val sortedCandles = candles.sortBy(((x$5) => x$5.time.getTime));
      if (sortedCandles.nonEmpty)
        {
          println(StringContext("SortedCandles first time: ", "").s(sortedCandles.head.time.getTime));
          println(StringContext("SortedCandles last time: ", "").s(sortedCandles.last.time.getTime))
        }
      else
        ();
      val initialCandles = sortedCandles.take(primaryBatchSize);
      val preloadedCandles = sortedCandles.drop(primaryBatchSize);
      cacheQueue.clear();
      if (preloadedCandles.nonEmpty)
        cacheQueue.enqueueAll(preloadedCandles)
      else
        ();
      val newEarliestTimestamp = preloadedCandles.headOption.map(((x$6) => x$6.time.getTime)).getOrElse(initialCandles.headOption.map(((x$7) => x$7.time.getTime)).getOrElse(referenceTimestamp.getTime));
      val response = Json.obj("candles".$minus$greater(initialCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
      Ok(response)
    }))
  else
    if (earliestTimestampOpt.isDefined)
      if (cacheQueue.size.$greater$eq(primaryBatchSize))
        {
          val batchCandles = cacheQueue.dequeueAll(((x$8) => true)).take(primaryBatchSize);
          if (batchCandles.nonEmpty)
            {
              println(StringContext("BatchCandles first time: ", "").s(batchCandles.head.time.getTime));
              println(StringContext("BatchCandles last time: ", "").s(batchCandles.last.time.getTime))
            }
          else
            ();
          val newEarliestTimestamp = batchCandles.head.time.getTime;
          val preloadFuture = candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, new Timestamp(newEarliestTimestamp), preloadBatchSize).map(((newPreloadedCandles) => {
            val sortedNewPreloaded = newPreloadedCandles.sortBy(((x$9) => x$9.time.getTime));
            if (sortedNewPreloaded.nonEmpty)
              {
                println(StringContext("Preloading newCandles first time: ", "").s(sortedNewPreloaded.head.time.getTime));
                println(StringContext("Preloading newCandles last time: ", "").s(sortedNewPreloaded.last.time.getTime));
                cacheQueue.enqueueAll(sortedNewPreloaded)
              }
            else
              ()
          }));
          preloadFuture.map(((x$10) => {
            val response = Json.obj("candles".$minus$greater(batchCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
            Ok(response)
          }))
        }
      else
        {
          val remainingBatchSize = primaryBatchSize.$minus(cacheQueue.size);
          val initialCandles = cacheQueue.dequeueAll(((x$11) => true));
          candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, referenceTimestamp, remainingBatchSize.$plus(preloadBatchSize)).map(((candles) => {
            val sortedCandles = candles.sortBy(((x$12) => x$12.time.getTime));
            val additionalCandles = sortedCandles.take(remainingBatchSize);
            val preloadedCandles = sortedCandles.drop(remainingBatchSize);
            if (additionalCandles.nonEmpty)
              {
                println(StringContext("AdditionalCandles first time: ", "").s(additionalCandles.head.time.getTime));
                println(StringContext("AdditionalCandles last time: ", "").s(additionalCandles.last.time.getTime))
              }
            else
              ();
            if (preloadedCandles.nonEmpty)
              {
                println(StringContext("PreloadedCandles first time: ", "").s(preloadedCandles.head.time.getTime));
                println(StringContext("PreloadedCandles last time: ", "").s(preloadedCandles.last.time.getTime))
              }
            else
              ();
            if (preloadedCandles.nonEmpty)
              cacheQueue.enqueueAll(preloadedCandles)
            else
              ();
            val combinedCandles = initialCandles.$plus$plus(additionalCandles);
            val newEarliestTimestamp = combinedCandles.headOption.map(((x$13) => x$13.time.getTime)).getOrElse(referenceTimestamp.getTime);
            val response = Json.obj("candles".$minus$greater(combinedCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
            Ok(response)
          }))
        }
    else
      Future.successful(BadRequest(Json.obj("error".$minus$greater("Missing required query parameter: either date or earliestTimestamp"))))
})).flatten
     while compiling: file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
        during phase: globalPhase=<no phase>, enteringPhase=parser
     library version: version 2.13.12
    compiler version: version 2.13.12
  reconstructed args: -deprecation -encoding utf8 -release:11 -unchecked -Wconf:cat=unchecked:w -Wconf:cat=deprecation:w -Wconf:cat=deprecation:ws -Wconf:cat=feature:ws -Wconf:cat=optimizer:ws -classpath <WORKSPACE>\conf;<WORKSPACE>\.bloop\root\bloop-bsp-clients-classes\classes-Metals-sbt9g92EQSS4hfsLxpnxUQ==;<HOME>\AppData\Local\bloop\cache\semanticdb\com.sourcegraph.semanticdb-javac.0.10.3\semanticdb-javac-0.10.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\twirl-api_2.13\1.5.1\twirl-api_2.13-1.5.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-server_2.13\2.8.20\play-server_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-logback_2.13\2.8.20\play-logback_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-akka-http-server_2.13\2.8.20\play-akka-http-server_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\filters-helpers_2.13\2.8.20\filters-helpers_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-guice_2.13\2.8.20\play-guice_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick_2.13\5.0.0\play-slick_2.13-5.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick-evolutions_2.13\5.0.0\play-slick-evolutions_2.13-5.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\postgresql\postgresql\42.2.23\postgresql-42.2.23.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-xml_2.13\1.3.1\scala-xml_2.13-1.3.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play_2.13\2.8.20\play_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-streams_2.13\2.8.20\play-streams_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-http-core_2.13\10.1.15\akka-http-core_2.13-10.1.15.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\guice\5.1.0\guice-5.1.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\extensions\guice-assistedinject\4.2.3\guice-assistedinject-4.2.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick_2.13\3.3.2\slick_2.13-3.3.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick-hikaricp_2.13\3.3.2\slick-hikaricp_2.13-3.3.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-api_2.13\2.8.0\play-jdbc-api_2.13-2.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-evolutions_2.13\2.8.0\play-jdbc-evolutions_2.13-2.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\checkerframework\checker-qual\3.8.0\checker-qual-3.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\build-link\2.8.20\build-link-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jul-to-slf4j\1.7.36\jul-to-slf4j-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jcl-over-slf4j\1.7.36\jcl-over-slf4j-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.21\akka-actor_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor-typed_2.13\2.6.21\akka-actor-typed_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-slf4j_2.13\2.6.21\akka-slf4j_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-serialization-jackson_2.13\2.6.21\akka-serialization-jackson_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-core\2.11.4\jackson-core-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-annotations\2.11.4\jackson-annotations-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.11.4\jackson-datatype-jdk8-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.11.4\jackson-datatype-jsr310-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-databind\2.11.4\jackson-databind-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-json_2.13\2.8.2\play-json_2.13-2.8.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\guava\30.1.1-jre\guava-30.1.1-jre.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt\0.9.1\jjwt-0.9.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\transaction\jakarta.transaction-api\1.3.3\jakarta.transaction-api-1.3.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\inject\javax.inject\1\javax.inject-1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.2\scala-java8-compat_2.13-1.0.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\ssl-config-core_2.13\0.4.3\ssl-config-core_2.13-0.4.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-parser-combinators_2.13\1.1.2\scala-parser-combinators_2.13-1.1.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-stream_2.13\2.6.21\akka-stream_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-parsing_2.13\10.1.15\akka-parsing_2.13-10.1.15.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\aopalliance\aopalliance\1.0\aopalliance-1.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.2\config-1.4.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-collection-compat_2.13\2.0.0\scala-collection-compat_2.13-2.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\zaxxer\HikariCP\3.2.0\HikariCP-3.2.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-exceptions\2.8.20\play-exceptions-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-parameter-names\2.11.4\jackson-module-parameter-names-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\dataformat\jackson-dataformat-cbor\2.11.4\jackson-dataformat-cbor-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-scala_2.13\2.11.4\jackson-module-scala_2.13-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-functional_2.13\2.8.2\play-functional_2.13-2.8.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-reflect\2.13.12\scala-reflect-2.13.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\joda-time\joda-time\2.10.5\joda-time-2.10.5.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\errorprone\error_prone_annotations\2.5.1\error_prone_annotations-2.5.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-protobuf-v3_2.13\2.6.21\akka-protobuf-v3_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-paranamer\2.11.4\jackson-module-paranamer-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar -Xplugin-require:semanticdb -Yrangepos -Ymacro-expand:discard -Ycache-plugin-class-loader:last-modified -Ypresentation-any-thread

  last tree to typer: CaseDef
       tree position: line 80 of file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
              symbol: null
           call site: <none> in <none>

== Source file context for tree position ==

    77           val totalBatchSize = primaryBatchSize + preloadBatchSize
    78 
    79           candlesRepository.getPairIdByName(pairName).flatMap {
    80             case Some(pairId) =>
    81               // Synchronize operations per (pairName, timeframe) to ensure thread safety
    82               Future {
    83                 getMutex(pairName, timeframe).synchronized {

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 2.13.12
Classpath:
<WORKSPACE>\conf [exists ], <WORKSPACE>\.bloop\root\bloop-bsp-clients-classes\classes-Metals-sbt9g92EQSS4hfsLxpnxUQ== [exists ], <HOME>\AppData\Local\bloop\cache\semanticdb\com.sourcegraph.semanticdb-javac.0.10.3\semanticdb-javac-0.10.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\twirl-api_2.13\1.5.1\twirl-api_2.13-1.5.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-server_2.13\2.8.20\play-server_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-logback_2.13\2.8.20\play-logback_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-akka-http-server_2.13\2.8.20\play-akka-http-server_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\filters-helpers_2.13\2.8.20\filters-helpers_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-guice_2.13\2.8.20\play-guice_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick_2.13\5.0.0\play-slick_2.13-5.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick-evolutions_2.13\5.0.0\play-slick-evolutions_2.13-5.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\postgresql\postgresql\42.2.23\postgresql-42.2.23.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-xml_2.13\1.3.1\scala-xml_2.13-1.3.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play_2.13\2.8.20\play_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-streams_2.13\2.8.20\play-streams_2.13-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-http-core_2.13\10.1.15\akka-http-core_2.13-10.1.15.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\guice\5.1.0\guice-5.1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\extensions\guice-assistedinject\4.2.3\guice-assistedinject-4.2.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick_2.13\3.3.2\slick_2.13-3.3.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick-hikaricp_2.13\3.3.2\slick-hikaricp_2.13-3.3.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-api_2.13\2.8.0\play-jdbc-api_2.13-2.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-evolutions_2.13\2.8.0\play-jdbc-evolutions_2.13-2.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\checkerframework\checker-qual\3.8.0\checker-qual-3.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\build-link\2.8.20\build-link-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jul-to-slf4j\1.7.36\jul-to-slf4j-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jcl-over-slf4j\1.7.36\jcl-over-slf4j-1.7.36.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.21\akka-actor_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor-typed_2.13\2.6.21\akka-actor-typed_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-slf4j_2.13\2.6.21\akka-slf4j_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-serialization-jackson_2.13\2.6.21\akka-serialization-jackson_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-core\2.11.4\jackson-core-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-annotations\2.11.4\jackson-annotations-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.11.4\jackson-datatype-jdk8-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.11.4\jackson-datatype-jsr310-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-databind\2.11.4\jackson-databind-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-json_2.13\2.8.2\play-json_2.13-2.8.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\guava\30.1.1-jre\guava-30.1.1-jre.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt\0.9.1\jjwt-0.9.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\transaction\jakarta.transaction-api\1.3.3\jakarta.transaction-api-1.3.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\inject\javax.inject\1\javax.inject-1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.2\scala-java8-compat_2.13-1.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\ssl-config-core_2.13\0.4.3\ssl-config-core_2.13-0.4.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-parser-combinators_2.13\1.1.2\scala-parser-combinators_2.13-1.1.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-stream_2.13\2.6.21\akka-stream_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-parsing_2.13\10.1.15\akka-parsing_2.13-10.1.15.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\aopalliance\aopalliance\1.0\aopalliance-1.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.2\config-1.4.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-collection-compat_2.13\2.0.0\scala-collection-compat_2.13-2.0.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\zaxxer\HikariCP\3.2.0\HikariCP-3.2.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-exceptions\2.8.20\play-exceptions-2.8.20.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-parameter-names\2.11.4\jackson-module-parameter-names-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\dataformat\jackson-dataformat-cbor\2.11.4\jackson-dataformat-cbor-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-scala_2.13\2.11.4\jackson-module-scala_2.13-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-functional_2.13\2.8.2\play-functional_2.13-2.8.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-reflect\2.13.12\scala-reflect-2.13.12.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\joda-time\joda-time\2.10.5\joda-time-2.10.5.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\errorprone\error_prone_annotations\2.5.1\error_prone_annotations-2.5.1.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-protobuf-v3_2.13\2.6.21\akka-protobuf-v3_2.13-2.6.21.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-paranamer\2.11.4\jackson-module-paranamer-2.11.4.jar [exists ], <HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar [exists ]
Options:
-deprecation -unchecked -encoding utf8 -Yrangepos -Xplugin-require:semanticdb -release 11


action parameters:
offset: 2984
uri: file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
text:
```scala
package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import repositories.CandlesRepository
import models.Candlestick
import play.api.libs.json.Json
import java.sql.Timestamp
import scala.collection.concurrent.TrieMap
import scala.collection.mutable.Queue

@Singleton
class ChartController @Inject() (
    cc: ControllerComponents,
    candlesRepository: CandlesRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // Cache: Map[(pairName, timeframe), Queue[Candlestick]]
  private val preloadedCandlesCache =
    TrieMap[(String, String), Queue[Candlestick]]()

  // Mutex map to handle synchronization per (pairName, timeframe)
  private val mutexMap = TrieMap[(String, String), Object]()

  private def getMutex(pairName: String, timeframe: String): Object = {
    mutexMap.getOrElseUpdate((pairName, timeframe), new Object())
  }

  /** Unified Controller Method: getCandleData
    *
    * Handles both initial and subsequent data fetches.
    *
    * Query Parameters:
    *   - pairName (String): Required. The trading pair (e.g., BTCUSD).
    *   - timeframe (String): Required. The timeframe of the candles (e.g., 1d).
    *   - date (Long): Optional. The reference timestamp (in milliseconds) for
    *     the initial call.
    *   - earliestTimestamp (Long): Optional. The earliest timestamp from the
    *     previous response for subsequent calls.
    *
    * Response:
    *   - candles (Array): The primary 1,000 candles.
    *   - earliestTimestamp (Long): The timestamp of the oldest candle in the
    *     returned candles.
    */
  def getCandleData: Action[AnyContent] = Action.async { implicit request =>
    val pairNameOpt = request.getQueryString("pairName")
    val timeframeOpt = request.getQueryString("timeframe")
    val dateOpt = request.getQueryString("date").map(_.toLong)
    val earliestTimestampOpt =
      request.getQueryString("earliestTimestamp").map(_.toLong)

    // Validate required parameters
    if (pairNameOpt.isEmpty || timeframeOpt.isEmpty) {
      Future.successful(
        BadRequest(
          Json.obj(
            "error" -> "Missing required query parameters: pairName, timeframe"
          )
        )
      )
    } else {
      val pairName = pairNameOpt.get
      val timeframe = timeframeOpt.get

      // Determine the reference timestamp
      val referenceTimestampOpt = dateOpt
        .map(new Timestamp(_))
        .orElse(earliestTimestampOpt.map(new Timestamp(_)))

      referenceTimestampOpt match {
        case Some(referenceTimestamp) =>
          val primaryBatchSize = 1000
          val preloadBatchSize = 10000
          val totalBatchSize = primaryBatchSize + preloadBatchSize

          candlesRepository.getPairIdByName(pairName).flatMap {
            case Some(pairId) =>
              // Synchronize operations per (pairName, t@@imeframe) to ensure thread safety
              Future {
                getMutex(pairName, timeframe).synchronized {
                  val cacheKey = (pairName, timeframe)
                  val cacheQueue =
                    preloadedCandlesCache.getOrElseUpdate(cacheKey, Queue())

                  if (dateOpt.isDefined) {
                    // Initial Call: Fetch from DB and populate cache
                    candlesRepository
                      .getCandlesticksBatchByPairId(
                        pairId,
                        timeframe,
                        referenceTimestamp,
                        totalBatchSize
                      )
                      .map { candles =>
                        // Sort candles in ascending order by time
                        val sortedCandles = candles.sortBy(_.time.getTime)

                        // Log the sorted candles for debugging
                        if (sortedCandles.nonEmpty) {
                          println(
                            s"SortedCandles first time: ${sortedCandles.head.time.getTime}"
                          )
                          println(
                            s"SortedCandles last time: ${sortedCandles.last.time.getTime}"
                          )
                        }

                        val initialCandles =
                          sortedCandles.take(primaryBatchSize)
                        val preloadedCandles =
                          sortedCandles.drop(primaryBatchSize)

                        // Update the cache with preloaded candles
                        cacheQueue.clear()
                        if (preloadedCandles.nonEmpty) {
                          cacheQueue.enqueueAll(preloadedCandles)
                        }

                        // Determine the new earliest timestamp
                        val newEarliestTimestamp = preloadedCandles.headOption
                          .map(_.time.getTime)
                          .getOrElse(
                            initialCandles.headOption
                              .map(_.time.getTime)
                              .getOrElse(referenceTimestamp.getTime)
                          )

                        // Prepare JSON response
                        val response = Json.obj(
                          "candles" -> initialCandles,
                          "earliestTimestamp" -> newEarliestTimestamp
                        )
                        Ok(response)
                      }
                  } else if (earliestTimestampOpt.isDefined) {
                    // Subsequent Call: Serve from cache if available
                    if (cacheQueue.size >= primaryBatchSize) {
                      // Extract the next 1,000 candles from the cache
                      val batchCandles =
                        cacheQueue.dequeueAll(_ => true).take(primaryBatchSize)

                      // Log the batchCandles for debugging
                      if (batchCandles.nonEmpty) {
                        println(
                          s"BatchCandles first time: ${batchCandles.head.time.getTime}"
                        )
                        println(
                          s"BatchCandles last time: ${batchCandles.last.time.getTime}"
                        )
                      }

                      // Determine the new earliest timestamp
                      val newEarliestTimestamp = batchCandles.head.time.getTime

                      // Asynchronously preload the next 10,000 candles from DB
                      val preloadFuture = candlesRepository
                        .getCandlesticksBatchByPairId(
                          pairId,
                          timeframe,
                          new Timestamp(newEarliestTimestamp),
                          preloadBatchSize
                        )
                        .map { newPreloadedCandles =>
                          // Sort and enqueue the new preloaded candles
                          val sortedNewPreloaded =
                            newPreloadedCandles.sortBy(_.time.getTime)
                          if (sortedNewPreloaded.nonEmpty) {
                            println(
                              s"Preloading newCandles first time: ${sortedNewPreloaded.head.time.getTime}"
                            )
                            println(
                              s"Preloading newCandles last time: ${sortedNewPreloaded.last.time.getTime}"
                            )
                            cacheQueue.enqueueAll(sortedNewPreloaded)
                          }
                        }

                      preloadFuture.map { _ =>
                        // Prepare JSON response
                        val response = Json.obj(
                          "candles" -> batchCandles,
                          "earliestTimestamp" -> newEarliestTimestamp
                        )
                        Ok(response)
                      }
                    } else {
                      // Not enough data in cache, fetch from DB
                      val remainingBatchSize =
                        primaryBatchSize - cacheQueue.size
                      val initialCandles = cacheQueue.dequeueAll(_ => true)

                      candlesRepository
                        .getCandlesticksBatchByPairId(
                          pairId,
                          timeframe,
                          referenceTimestamp,
                          remainingBatchSize + preloadBatchSize
                        )
                        .map { candles =>
                          // Sort candles in ascending order by time
                          val sortedCandles = candles.sortBy(_.time.getTime)

                          val additionalCandles =
                            sortedCandles.take(remainingBatchSize)
                          val preloadedCandles =
                            sortedCandles.drop(remainingBatchSize)

                          // Log the additionalCandles and preloadedCandles for debugging
                          if (additionalCandles.nonEmpty) {
                            println(
                              s"AdditionalCandles first time: ${additionalCandles.head.time.getTime}"
                            )
                            println(
                              s"AdditionalCandles last time: ${additionalCandles.last.time.getTime}"
                            )
                          }
                          if (preloadedCandles.nonEmpty) {
                            println(
                              s"PreloadedCandles first time: ${preloadedCandles.head.time.getTime}"
                            )
                            println(
                              s"PreloadedCandles last time: ${preloadedCandles.last.time.getTime}"
                            )
                          }

                          // Update the cache with new preloaded candles
                          if (preloadedCandles.nonEmpty) {
                            cacheQueue.enqueueAll(preloadedCandles)
                          }

                          // Combine with existing initial candles
                          val combinedCandles =
                            initialCandles ++ additionalCandles

                          // Determine the new earliest timestamp
                          val newEarliestTimestamp = combinedCandles.headOption
                            .map(_.time.getTime)
                            .getOrElse(referenceTimestamp.getTime)

                          // Prepare JSON response
                          val response = Json.obj(
                            "candles" -> combinedCandles,
                            "earliestTimestamp" -> newEarliestTimestamp
                          )
                          Ok(response)
                        }
                    }
                  } else {
                    // Neither date nor earliestTimestamp provided
                    Future.successful(
                      BadRequest(
                        Json.obj(
                          "error" -> "Missing required query parameter: either date or earliestTimestamp"
                        )
                      )
                    )
                  }
                }
              }.flatten // Flatten the nested Future
            case None =>
              Future.successful(
                NotFound(Json.obj("error" -> s"Pair '$pairName' not found"))
              )
          }
      }
    }

    /** Optional: Remove or deprecate existing methods to ensure only
      * getCandleData is used.
      */
    // def initialize: Action[AnyContent] = Action.async { ... }
    // def getNextBatch: Action[AnyContent] = Action.async { ... }
    // def getCandlestickBatch: Action[AnyContent] = Action.async { ... }

  }
}

```



#### Error stacktrace:

```
scala.reflect.internal.Reporting.abort(Reporting.scala:70)
	scala.reflect.internal.Reporting.abort$(Reporting.scala:66)
	scala.reflect.internal.SymbolTable.abort(SymbolTable.scala:28)
	scala.tools.nsc.typechecker.Typers$Typer.typedOutsidePatternMode$1(Typers.scala:6090)
	scala.tools.nsc.typechecker.Typers$Typer.typed1(Typers.scala:6107)
	scala.tools.nsc.typechecker.Typers$Typer.typed(Typers.scala:6153)
	scala.tools.nsc.typechecker.Typers$Typer.typedQualifier(Typers.scala:6251)
	scala.meta.internal.pc.PcDefinitionProvider.definitionTypedTreeAt(PcDefinitionProvider.scala:164)
	scala.meta.internal.pc.PcDefinitionProvider.definition(PcDefinitionProvider.scala:68)
	scala.meta.internal.pc.PcDefinitionProvider.definition(PcDefinitionProvider.scala:16)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$definition$1(ScalaPresentationCompiler.scala:393)
```
#### Short summary: 

scala.reflect.internal.FatalError: 
  unexpected tree: class scala.reflect.internal.Trees$CaseDef
case Some((pairId @ _)) => Future(getMutex(pairName, timeframe).synchronized({
  val cacheKey = scala.Tuple2(pairName, timeframe);
  val cacheQueue = preloadedCandlesCache.getOrElseUpdate(cacheKey, Queue());
  if (dateOpt.isDefined)
    candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, referenceTimestamp, totalBatchSize).map(((candles) => {
      val sortedCandles = candles.sortBy(((x$5) => x$5.time.getTime));
      if (sortedCandles.nonEmpty)
        {
          println(StringContext("SortedCandles first time: ", "").s(sortedCandles.head.time.getTime));
          println(StringContext("SortedCandles last time: ", "").s(sortedCandles.last.time.getTime))
        }
      else
        ();
      val initialCandles = sortedCandles.take(primaryBatchSize);
      val preloadedCandles = sortedCandles.drop(primaryBatchSize);
      cacheQueue.clear();
      if (preloadedCandles.nonEmpty)
        cacheQueue.enqueueAll(preloadedCandles)
      else
        ();
      val newEarliestTimestamp = preloadedCandles.headOption.map(((x$6) => x$6.time.getTime)).getOrElse(initialCandles.headOption.map(((x$7) => x$7.time.getTime)).getOrElse(referenceTimestamp.getTime));
      val response = Json.obj("candles".$minus$greater(initialCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
      Ok(response)
    }))
  else
    if (earliestTimestampOpt.isDefined)
      if (cacheQueue.size.$greater$eq(primaryBatchSize))
        {
          val batchCandles = cacheQueue.dequeueAll(((x$8) => true)).take(primaryBatchSize);
          if (batchCandles.nonEmpty)
            {
              println(StringContext("BatchCandles first time: ", "").s(batchCandles.head.time.getTime));
              println(StringContext("BatchCandles last time: ", "").s(batchCandles.last.time.getTime))
            }
          else
            ();
          val newEarliestTimestamp = batchCandles.head.time.getTime;
          val preloadFuture = candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, new Timestamp(newEarliestTimestamp), preloadBatchSize).map(((newPreloadedCandles) => {
            val sortedNewPreloaded = newPreloadedCandles.sortBy(((x$9) => x$9.time.getTime));
            if (sortedNewPreloaded.nonEmpty)
              {
                println(StringContext("Preloading newCandles first time: ", "").s(sortedNewPreloaded.head.time.getTime));
                println(StringContext("Preloading newCandles last time: ", "").s(sortedNewPreloaded.last.time.getTime));
                cacheQueue.enqueueAll(sortedNewPreloaded)
              }
            else
              ()
          }));
          preloadFuture.map(((x$10) => {
            val response = Json.obj("candles".$minus$greater(batchCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
            Ok(response)
          }))
        }
      else
        {
          val remainingBatchSize = primaryBatchSize.$minus(cacheQueue.size);
          val initialCandles = cacheQueue.dequeueAll(((x$11) => true));
          candlesRepository.getCandlesticksBatchByPairId(pairId, timeframe, referenceTimestamp, remainingBatchSize.$plus(preloadBatchSize)).map(((candles) => {
            val sortedCandles = candles.sortBy(((x$12) => x$12.time.getTime));
            val additionalCandles = sortedCandles.take(remainingBatchSize);
            val preloadedCandles = sortedCandles.drop(remainingBatchSize);
            if (additionalCandles.nonEmpty)
              {
                println(StringContext("AdditionalCandles first time: ", "").s(additionalCandles.head.time.getTime));
                println(StringContext("AdditionalCandles last time: ", "").s(additionalCandles.last.time.getTime))
              }
            else
              ();
            if (preloadedCandles.nonEmpty)
              {
                println(StringContext("PreloadedCandles first time: ", "").s(preloadedCandles.head.time.getTime));
                println(StringContext("PreloadedCandles last time: ", "").s(preloadedCandles.last.time.getTime))
              }
            else
              ();
            if (preloadedCandles.nonEmpty)
              cacheQueue.enqueueAll(preloadedCandles)
            else
              ();
            val combinedCandles = initialCandles.$plus$plus(additionalCandles);
            val newEarliestTimestamp = combinedCandles.headOption.map(((x$13) => x$13.time.getTime)).getOrElse(referenceTimestamp.getTime);
            val response = Json.obj("candles".$minus$greater(combinedCandles), "earliestTimestamp".$minus$greater(newEarliestTimestamp));
            Ok(response)
          }))
        }
    else
      Future.successful(BadRequest(Json.obj("error".$minus$greater("Missing required query parameter: either date or earliestTimestamp"))))
})).flatten
     while compiling: file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
        during phase: globalPhase=<no phase>, enteringPhase=parser
     library version: version 2.13.12
    compiler version: version 2.13.12
  reconstructed args: -deprecation -encoding utf8 -release:11 -unchecked -Wconf:cat=unchecked:w -Wconf:cat=deprecation:w -Wconf:cat=deprecation:ws -Wconf:cat=feature:ws -Wconf:cat=optimizer:ws -classpath <WORKSPACE>\conf;<WORKSPACE>\.bloop\root\bloop-bsp-clients-classes\classes-Metals-sbt9g92EQSS4hfsLxpnxUQ==;<HOME>\AppData\Local\bloop\cache\semanticdb\com.sourcegraph.semanticdb-javac.0.10.3\semanticdb-javac-0.10.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-library\2.13.12\scala-library-2.13.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\twirl-api_2.13\1.5.1\twirl-api_2.13-1.5.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-server_2.13\2.8.20\play-server_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-logback_2.13\2.8.20\play-logback_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-akka-http-server_2.13\2.8.20\play-akka-http-server_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\filters-helpers_2.13\2.8.20\filters-helpers_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-guice_2.13\2.8.20\play-guice_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick_2.13\5.0.0\play-slick_2.13-5.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-slick-evolutions_2.13\5.0.0\play-slick-evolutions_2.13-5.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\postgresql\postgresql\42.2.23\postgresql-42.2.23.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-xml_2.13\1.3.1\scala-xml_2.13-1.3.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play_2.13\2.8.20\play_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-classic\1.2.12\logback-classic-1.2.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-streams_2.13\2.8.20\play-streams_2.13-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-http-core_2.13\10.1.15\akka-http-core_2.13-10.1.15.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\guice\5.1.0\guice-5.1.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\inject\extensions\guice-assistedinject\4.2.3\guice-assistedinject-4.2.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick_2.13\3.3.2\slick_2.13-3.3.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\slick\slick-hikaricp_2.13\3.3.2\slick-hikaricp_2.13-3.3.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-api_2.13\2.8.0\play-jdbc-api_2.13-2.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-jdbc-evolutions_2.13\2.8.0\play-jdbc-evolutions_2.13-2.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\checkerframework\checker-qual\3.8.0\checker-qual-3.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\build-link\2.8.20\build-link-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jul-to-slf4j\1.7.36\jul-to-slf4j-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\slf4j\jcl-over-slf4j\1.7.36\jcl-over-slf4j-1.7.36.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor_2.13\2.6.21\akka-actor_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-actor-typed_2.13\2.6.21\akka-actor-typed_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-slf4j_2.13\2.6.21\akka-slf4j_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-serialization-jackson_2.13\2.6.21\akka-serialization-jackson_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-core\2.11.4\jackson-core-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-annotations\2.11.4\jackson-annotations-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jdk8\2.11.4\jackson-datatype-jdk8-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\datatype\jackson-datatype-jsr310\2.11.4\jackson-datatype-jsr310-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\core\jackson-databind\2.11.4\jackson-databind-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-json_2.13\2.8.2\play-json_2.13-2.8.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\guava\30.1.1-jre\guava-30.1.1-jre.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\io\jsonwebtoken\jjwt\0.9.1\jjwt-0.9.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\xml\bind\jakarta.xml.bind-api\2.3.3\jakarta.xml.bind-api-2.3.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\transaction\jakarta.transaction-api\1.3.3\jakarta.transaction-api-1.3.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\javax\inject\javax.inject\1\javax.inject-1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-java8-compat_2.13\1.0.2\scala-java8-compat_2.13-1.0.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\ssl-config-core_2.13\0.4.3\ssl-config-core_2.13-0.4.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-parser-combinators_2.13\1.1.2\scala-parser-combinators_2.13-1.1.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\ch\qos\logback\logback-core\1.2.12\logback-core-1.2.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\reactivestreams\reactive-streams\1.0.4\reactive-streams-1.0.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-stream_2.13\2.6.21\akka-stream_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-parsing_2.13\10.1.15\akka-parsing_2.13-10.1.15.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\aopalliance\aopalliance\1.0\aopalliance-1.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\config\1.4.2\config-1.4.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\modules\scala-collection-compat_2.13\2.0.0\scala-collection-compat_2.13-2.0.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\zaxxer\HikariCP\3.2.0\HikariCP-3.2.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-exceptions\2.8.20\play-exceptions-2.8.20.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-parameter-names\2.11.4\jackson-module-parameter-names-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\dataformat\jackson-dataformat-cbor\2.11.4\jackson-dataformat-cbor-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-scala_2.13\2.11.4\jackson-module-scala_2.13-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\lz4\lz4-java\1.8.0\lz4-java-1.8.0.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\play\play-functional_2.13\2.8.2\play-functional_2.13-2.8.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\org\scala-lang\scala-reflect\2.13.12\scala-reflect-2.13.12.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\joda-time\joda-time\2.10.5\joda-time-2.10.5.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\failureaccess\1.0.1\failureaccess-1.0.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\guava\listenablefuture\9999.0-empty-to-avoid-conflict-with-guava\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\code\findbugs\jsr305\3.0.2\jsr305-3.0.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\errorprone\error_prone_annotations\2.5.1\error_prone_annotations-2.5.1.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\google\j2objc\j2objc-annotations\1.3\j2objc-annotations-1.3.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\typesafe\akka\akka-protobuf-v3_2.13\2.6.21\akka-protobuf-v3_2.13-2.6.21.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\fasterxml\jackson\module\jackson-module-paranamer\2.11.4\jackson-module-paranamer-2.11.4.jar;<HOME>\AppData\Local\Coursier\cache\v1\https\repo1.maven.org\maven2\com\thoughtworks\paranamer\paranamer\2.8\paranamer-2.8.jar -Xplugin-require:semanticdb -Yrangepos -Ymacro-expand:discard -Ycache-plugin-class-loader:last-modified -Ypresentation-any-thread

  last tree to typer: CaseDef
       tree position: line 80 of file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/controllers/ChartController.scala
              symbol: null
           call site: <none> in <none>

== Source file context for tree position ==

    77           val totalBatchSize = primaryBatchSize + preloadBatchSize
    78 
    79           candlesRepository.getPairIdByName(pairName).flatMap {
    80             case Some(pairId) =>
    81               // Synchronize operations per (pairName, timeframe) to ensure thread safety
    82               Future {
    83                 getMutex(pairName, timeframe).synchronized {