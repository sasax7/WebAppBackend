package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import models.{User, UserTable}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class UsersRepository @Inject() (
    @NamedDatabase("default") dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  private val users = TableQuery[UserTable]

  def listUsers(): Future[Seq[User]] = {
    db.run(users.result)
  }

  def createUser(user: User): Future[User] = {
    db.run(
      (users returning users.map(_.id) into ((user, id) =>
        user.copy(id = Some(id))
      )) += user.copy(id = None)
    )
  }

  def findUserById(id: Long): Future[Option[User]] = {
    db.run(users.filter(_.id === id).result.headOption)
  }

  def deleteUser(id: Long): Future[Int] = {
    db.run(users.filter(_.id === id).delete)
  }
  def updateUser(firebaseUid: String, user: User): Future[Int] = {
    db.run(
      users
        .filter(_.firebaseUid === firebaseUid)
        .update(user.copy(firebaseUid = firebaseUid))
    )
  }

  def findUserByFirebaseUid(firebaseUid: String): Future[Option[User]] = {
    db.run(users.filter(_.firebaseUid === firebaseUid).result.headOption)
  }
}
