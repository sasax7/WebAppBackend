package models

import slick.jdbc.PostgresProfile.api._
import play.api.libs.json.{
  Json,
  OFormat,
  Format,
  JsResult,
  JsValue,
  JsSuccess,
  JsError
}
import java.sql.Timestamp
import java.text.SimpleDateFormat

case class User(
    id: Option[Long],
    firebaseUid: String,
    username: Option[String],
    email: Option[String],
    role: String = "user",
    createdAt: Option[Timestamp] = Some(
      new Timestamp(System.currentTimeMillis())
    ),
    lastLogin: Option[Timestamp]
)

object User {
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    private val dateFormats = Seq(
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"), // Handles Z-style
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX") // Handles milliseconds
    )

    def reads(json: JsValue): JsResult[Timestamp] =
      json.validate[String].flatMap { str =>
        dateFormats.view
          .map { format =>
            try {
              JsSuccess(new Timestamp(format.parse(str).getTime))
            } catch {
              case _: java.text.ParseException => JsError()
            }
          }
          .find(_.isSuccess)
          .getOrElse(JsError(s"Invalid date format: $str"))
      }

    def writes(ts: Timestamp): JsValue =
      Json.toJson(
        dateFormats.head.format(ts)
      ) // Uses the primary format for output
  }

  implicit val optionTimestampFormat: Format[Option[Timestamp]] =
    new Format[Option[Timestamp]] {
      def reads(json: JsValue): JsResult[Option[Timestamp]] =
        json.validateOpt[Timestamp]

      def writes(optTs: Option[Timestamp]): JsValue = optTs match {
        case Some(ts) => Json.toJson(ts)
        case None     => Json.toJson(None)
      }
    }

  implicit val userFormat: OFormat[User] = Json.format[User]
}

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def firebaseUid = column[String]("firebase_uid", O.Unique)
  def username = column[Option[String]]("username")
  def email = column[Option[String]]("email")
  def role = column[String]("role", O.Default("user"))
  def createdAt = column[Option[Timestamp]](
    "created_at",
    O.Default(Some(new Timestamp(System.currentTimeMillis())))
  )
  def lastLogin = column[Option[Timestamp]]("last_login")

  def * = (
    id.?,
    firebaseUid,
    username,
    email,
    role,
    createdAt,
    lastLogin
  ) <> ((User.apply _).tupled, User.unapply)
}
