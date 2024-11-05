package repositories

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Future, ExecutionContext}
import models.{User, UsersTable}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.DatabaseConfigProvider

@Singleton
class UserRepository @Inject() (
    @NamedDatabase("default") dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  private val users = UsersTable.users

  def list(): Future[Seq[User]] = {
    dbConfig.db.run(users.result)
  }

  def create(name: String, email: String): Future[User] = {
    val user = User(0, name, email)
    dbConfig.db.run(
      (users returning users.map(_.id) into ((user, id) =>
        user.copy(id = id)
      )) += user
    )
  }

  def findById(id: Long): Future[Option[User]] = {
    dbConfig.db.run(users.filter(_.id === id).result.headOption)
  }

  def update(id: Long, updatedUser: User): Future[Int] = {
    dbConfig.db.run(users.filter(_.id === id).update(updatedUser))
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(users.filter(_.id === id).delete)
  }
}
