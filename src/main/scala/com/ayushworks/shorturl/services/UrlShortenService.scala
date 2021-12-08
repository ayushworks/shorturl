package com.ayushworks.shorturl.services

import cats.effect.Concurrent
import com.ayushworks.shorturl.persistence.DataStore
import com.ayushworks.shorturl.services.UrlShortenService.{ShortenRequest, ShortenResponse}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._
import cats.implicits._

trait UrlShortenService[F[_]] {
  def shorten(request: ShortenRequest): F[ShortenResponse]
  def getFullUrl(short: String): F[Option[String]]
}

object UrlShortenService {

  final case class ShortenRequest(uri: String) extends AnyVal

  object ShortenRequest {
    implicit val requestDecoder: Decoder[ShortenRequest] = deriveDecoder[ShortenRequest]
    implicit def requestEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, ShortenRequest] = jsonOf
  }

  final case class ShortenResponse(short: String) extends AnyVal

  object ShortenResponse {
    implicit val shortenResponseEncoder: Encoder[ShortenResponse] = deriveEncoder[ShortenResponse]
    implicit def responseEntityEncoder[F[_]]: EntityEncoder[F, ShortenResponse] =
      jsonEncoderOf
  }


  def impl[F[_]: Concurrent](S: StringShortService[F], D: DataStore[F]): UrlShortenService[F] = new UrlShortenService[F] {
    override def shorten(request: ShortenRequest): F[ShortenResponse] =
      for {
        opt <- getFromDataStore(request.uri)
        result <- opt match {
          case Some(short) =>
            short.pure[F]
          case None =>
            shortenAndStore(request.uri)
        }
      } yield ShortenResponse(result)

      def getFromDataStore(uri: String): F[Option[String]] = D.exists(uri)

      def shortenAndStore(uri: String): F[String] = for {
        shorten <- S.short(uri)
        dbStore <- D.save(uri, shorten)
      } yield dbStore

    override def getFullUrl(short: String): F[Option[String]] = D.get(short)
  }


}
