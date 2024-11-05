package controllers

import javax.inject._
import play.api.mvc._
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

@Singleton
class UserController @Inject() (
    cc: ControllerComponents,
    userRepository: UserRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  implicit val userFormat: OFormat[models.User] = Json.format[models.User]

  def listUsers = Action.async {
    userRepository.list().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def createUser = Action.async(parse.json) { request =>
    request.body
      .validate[models.User]
      .fold(
        errors =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid input"))),
        user => {
          userRepository.create(user.name, user.email).map { newUser =>
            Created(Json.toJson(newUser))
          }
        }
      )
  }

  def getUser(id: Long) = Action.async {
    userRepository.findById(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound(Json.obj("message" -> "User not found"))
    }
  }

  def updateUser(id: Long) = Action.async(parse.json) { request =>
    request.body
      .validate[models.User]
      .fold(
        errors =>
          Future.successful(BadRequest(Json.obj("message" -> "Invalid input"))),
        user => {
          userRepository.update(id, user).map { updatedRows =>
            if (updatedRows > 0) Ok(Json.obj("message" -> "User updated"))
            else NotFound(Json.obj("message" -> "User not found"))
          }
        }
      )
  }

  def deleteUser(id: Long) = Action.async {
    userRepository.delete(id).map { deletedRows =>
      if (deletedRows > 0) Ok(Json.obj("message" -> "User deleted"))
      else NotFound(Json.obj("message" -> "User not found"))
    }
  }
}
