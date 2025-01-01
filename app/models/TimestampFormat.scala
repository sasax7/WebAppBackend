package models

import play.api.libs.json._
import java.sql.Timestamp

object TimestampFormat {
  private val dateFormats = Seq(
    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX"), // Z-style
    new java.text.SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSSX"
    ) // With milliseconds
  )

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
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
      Json.toJson(dateFormats.head.format(ts)) // Primary format for output
  }

  implicit val optionTimestampFormat: Format[Option[Timestamp]] =
    new Format[Option[Timestamp]] {
      def reads(json: JsValue): JsResult[Option[Timestamp]] =
        json.validateOpt[Timestamp]

      def writes(optTs: Option[Timestamp]): JsValue = optTs match {
        case Some(ts) => Json.toJson(ts)
        case None     => JsNull
      }
    }
}
