error id: file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala:[115..116) in Input.VirtualFile("file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala", "package state

import models.{Trade, User, Strategy}
import java.sql.Timestamp

trait BacktestState {
 def 
}

case class OperationalState(
    newestTimestamp: Long,
    trades: Seq[Trade],
    currentPrice: BigDecimal,
    strategy: Option[Strategy],
    user: Option[User],
    currentPair: String,
    balance: BigDecimal,
    timeframe: String,
    spread: Option[BigDecimal],
    fees: Option[BigDecimal]
) extends BacktestState {


}
")
file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala
file:///C:/Users/sti/OneDrive%20-%20Leicom%20AG/Desktop/HTWG/Web_App/backend/app/models/State/BacktestState.scala:8: error: expected identifier; obtained rbrace
}
^
#### Short summary: 

expected identifier; obtained rbrace