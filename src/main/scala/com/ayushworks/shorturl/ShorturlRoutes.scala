package com.ayushworks.shorturl

import com.ayushworks.shorturl.services.{StatsService, UrlShortenService}
import com.ayushworks.shorturl.services.UrlShortenService.ShortenRequest
import cats.effect.Sync
import cats.effect.kernel.Async
import cats.syntax.all._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Uri}
import org.http4s.headers.Location

object ShorturlRoutes {

  def shorturlRoutes[F[_]: Async](U: UrlShortenService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "app" / "shorten" =>
        for {
          request <- req.as[ShortenRequest]
          response <- Ok(U.shorten(request))
        } yield response

      case GET -> Root/ "app"/ shorten =>
        U.getFullUrl(shorten).flatMap {
          case Some(url) => PermanentRedirect(Location(Uri.unsafeFromString(url)))
          case None => NotFound(s"no short url $shorten found")
        }
    }
  }


  def statRoutes[F[_]: Sync](S: StatsService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root/ "app"/ "stats" / "all" =>
        for {
          urls <- S.getAll
          resp <- Ok(urls)
        } yield resp

      case GET -> Root/ "app"/ "stats" / shorten =>
        for {
          count <- S.stats(shorten)
          resp <- Ok(count)
        } yield resp
    }
  }
}