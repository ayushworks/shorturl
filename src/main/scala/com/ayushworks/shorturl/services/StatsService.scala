package com.ayushworks.shorturl.services

import cats.Applicative
import com.ayushworks.shorturl.persistence.DataStore
import com.ayushworks.shorturl.services.StatsService.{ShortUrls, StatResult}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import cats.implicits._

trait StatsService[F[_]] {
    def stats(url: String): F[StatResult]
    def getAll: F[ShortUrls]
}

object StatsService {

  final case class StatResult(accessCount: Int) extends AnyVal

  object StatResult {
    implicit val statResultEncoder: Encoder[StatResult] = deriveEncoder[StatResult]
    implicit def responseEntityEncoder[F[_]]: EntityEncoder[F, StatResult] =
      jsonEncoderOf
  }

  final case class ShortUrls(result: List[ShortUrl]) extends AnyVal

  object ShortUrls {
    implicit val shortUrlsEncoder: Encoder[ShortUrls] = deriveEncoder[ShortUrls]
    implicit def responseEntityEncoder[F[_]]: EntityEncoder[F, ShortUrls] =
      jsonEncoderOf
  }

  final case class ShortUrl(short: String, url: String)

  object ShortUrl {
    implicit val shortUrlEncoder: Encoder[ShortUrl] = deriveEncoder[ShortUrl]
    implicit def responseEntityEncoder[F[_]]: EntityEncoder[F, ShortUrl] =
      jsonEncoderOf
  }

  def impl[F[_]: Applicative](D: DataStore[F]): StatsService[F] = new StatsService[F] {
    override def stats(url: String): F[StatResult] = for {
      count <- D.stats(url)
    } yield StatResult(count)

    override def getAll: F[ShortUrls] = for {
      tuples <- D.getAll
    } yield ShortUrls(tuples.map{
      case (short,url) => ShortUrl(short,url)
    })
  }
}
