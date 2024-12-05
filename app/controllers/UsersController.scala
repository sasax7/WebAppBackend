package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import repositories.UsersRepository
import models.User
import play.api.libs.json._

@Singleton
class UsersController @Inject() (
    cc: ControllerComponents,
    usersRepository: UsersRepository
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  // Import the implicit formats from the User object
  import models.User._

  def listUsers: Action[AnyContent] = Action.async {
    usersRepository.listUsers().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def createUser: Action[JsValue] = Action.async(parse.json) { request =>
    request.body
      .validate[User]
      .fold(
        errors =>
          Future.successful(
            BadRequest(
              Json.obj("status" -> "error", "message" -> "Invalid JSON")
            )
          ),
        user => {
          usersRepository.createUser(user).map { createdUser =>
            Created(Json.toJson(createdUser))
          }
        }
      )
  }

  def findUserByFirebaseUid(id: String): Action[AnyContent] = Action.async {
    usersRepository.findUserByFirebaseUid(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None =>
        NotFound(Json.obj("status" -> "error", "message" -> "User not found"))
    }
  }
  def updateUser(firebaseUid: String): Action[JsValue] =
    Action.async(parse.json) { request =>
      request.body
        .validate[JsObject]
        .fold(
          errors =>
            Future.successful(
              BadRequest(
                Json.obj("status" -> "error", "message" -> "Invalid JSON")
              )
            ),
          json => {
            usersRepository.findUserByFirebaseUid(firebaseUid).flatMap {
              case Some(existingUser) =>
                val updatedUserJson =
                  Json.toJson(existingUser).as[JsObject] ++ json
                updatedUserJson
                  .validate[User]
                  .fold(
                    errors =>
                      Future.successful(
                        BadRequest(
                          Json.obj(
                            "status" -> "error",
                            "message" -> "Invalid JSON"
                          )
                        )
                      ),
                    updatedUser => {
                      usersRepository.updateUser(firebaseUid, updatedUser).map {
                        case 0 =>
                          NotFound(
                            Json.obj(
                              "status" -> "error",
                              "message" -> "User not found"
                            )
                          )
                        case _ =>
                          Ok(
                            Json.obj(
                              "status" -> "success",
                              "message" -> "User updated"
                            )
                          )
                      }
                    }
                  )
              case None =>
                Future.successful(
                  NotFound(
                    Json.obj("status" -> "error", "message" -> "User not found")
                  )
                )
            }
          }
        )
    }
  def deleteUser(id: Long): Action[AnyContent] = Action.async {
    usersRepository.deleteUser(id).map { deletedRows =>
      if (deletedRows > 0) Ok(Json.obj("status" -> "success"))
      else
        NotFound(Json.obj("status" -> "error", "message" -> "User not found"))
    }
  }
}
