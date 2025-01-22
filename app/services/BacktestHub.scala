package services

import akka.actor._
import state.OperationalState
import scala.collection.mutable

object BacktestHub {
  private val subscribers = mutable.Map[String, mutable.Set[ActorRef]]().withDefaultValue(mutable.Set())

  def subscribe(key: String, ref: ActorRef): Unit = subscribers(key) += ref
  def unsubscribe(key: String, ref: ActorRef): Unit = subscribers(key) -= ref

  def broadcast(key: String, state: OperationalState): Unit = {
    subscribers(key).foreach(_ ! state)
  }
}