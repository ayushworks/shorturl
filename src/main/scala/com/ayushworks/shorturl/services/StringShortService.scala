package com.ayushworks.shorturl.services

import cats.Applicative
import com.ayushworks.shorturl.util._
import cats.implicits._

trait StringShortService[F[_]] {
  def short(url: String): F[String]
}

object StringShortService {

  implicit def apply[F[_]](implicit ev: StringShortService[F]): StringShortService[F] = ev

  def impl[F[_]: Applicative]: StringShortService[F] = (url: String) => {
    val md5Bytes = md5(url)

    val byteArr = md5Bytes.slice(12, 16)

    val builder = new StringBuilder()
    byteArr.foreach { r =>
      val bytes = Array(r)
      isValidUTF8(bytes) match {
        case Some(charBuffer) => builder.append(charBuffer)
        case None => builder.append("\\x%02X ".format(r))
      }
    }

    val utf8Str = builder.toString().replaceAll(" ", "")
    base64(utf8Str).dropRight(2)
  }.pure[F]

}
