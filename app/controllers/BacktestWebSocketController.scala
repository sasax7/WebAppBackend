package controllers

import akka.actor._
import akka.stream.Materializer
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import play.api.libs.json._
import play.api.mvc.WebSocket.MessageFlowTransformer
import javax.inject._
import services.{BacktestHub, BacktestContextService}
import state.OperationalState

@Singleton
class BacktestWebSocketController @Inject() (
    cc: ControllerComponents,
    backtestContextService: BacktestContextService
)(implicit system: ActorSystem, mat: Materializer)
    extends AbstractController(cc) {

  def ws(key: String): WebSocket =
    WebSocket.accept[JsValue, JsValue] { _ =>
      ActorFlow.actorRef(out => WebSocketActor.props(key, out))
    }

  implicit val messageFlowTransformer
      : MessageFlowTransformer[JsValue, JsValue] =
    MessageFlowTransformer.jsonMessageFlowTransformer[JsValue, JsValue]
}

object WebSocketActor {
  def props(key: String, out: ActorRef): Props = Props(
    new WebSocketActor(key, out)
  )
}

class WebSocketActor(key: String, out: ActorRef) extends Actor {
  override def preStart(): Unit = BacktestHub.subscribe(key, self)
  override def postStop(): Unit = BacktestHub.unsubscribe(key, self)

  def receive: Receive = { case state: OperationalState =>
    out ! Json.toJson(state)
  }
}
