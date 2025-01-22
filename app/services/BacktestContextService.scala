package services

import akka.actor.ActorSystem
import javax.inject._
import state.{BacktestContext, OperationalState}
import scala.collection.mutable

@Singleton
class BacktestContextService @Inject() (implicit system: ActorSystem) {
  private val contexts = mutable.Map[String, BacktestContext]()

  def getContext(key: String): Option[BacktestContext] = contexts.get(key)

  def setContext(key: String, ctx: BacktestContext): Unit = {
    contexts(key) = ctx
    BacktestHub.broadcast(
      key,
      ctx.getCurrentState.asInstanceOf[OperationalState]
    )
  }

  def generateKey(): String = java.util.UUID.randomUUID().toString
}
