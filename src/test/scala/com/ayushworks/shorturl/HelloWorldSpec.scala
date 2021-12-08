package com.ayushworks.shorturl

import cats.effect.IO
import com.ayushworks.shorturl.persistence.DataStoreInMemory
import com.ayushworks.shorturl.services.UrlShortenService.ShortenRequest
import com.ayushworks.shorturl.services.{StatsService, StringShortService, UrlShortenService}
import org.http4s.implicits._
import munit.CatsEffectSuite
import org.http4s.circe._
import io.circe.syntax.EncoderOps
import io.circe.generic.auto._
import org.http4s._

import java.nio.charset.StandardCharsets


class HelloWorldSpec extends CatsEffectSuite {

  val stringShortService = StringShortService.impl[IO]
  val dataStoreService = DataStoreInMemory.apply[IO]()
  val shortenService = UrlShortenService.impl[IO](stringShortService,dataStoreService)
  val statsService = StatsService.impl[IO](dataStoreService)

  test("Shorten url should work") {
    val response = shortenRequest.unsafeRunSync()
    assertEquals(response.status, Status.Ok)
    val body = new String(response.body.compile.toList.unsafeRunSync().toArray, StandardCharsets.UTF_8)
    assertEquals(body,"{\"short\":\"AxMHYw\"}")
  }

  test("redirect from short url") {
    val response = shortenRequest.flatMap(_ => redirectRequest).unsafeRunSync()
    assertEquals(response.status, Status.PermanentRedirect)
  }

  test("stats for one url") {
    assertIO(shortenRequest.flatMap(_ => statsRequest).map(_.status), Status.Ok)
  }

  test("get all stats") {
    assertIO(shortenRequest.flatMap(_ => statsAllRequest).map(_.status), Status.Ok)
  }

  private[this] val shortenRequest: IO[Response[IO]] = {
    val body = ShortenRequest("https://andtvc.com").asJson
    val request = Request[IO](method = Method.POST, uri = uri"/app/shorten").withEntity(body)
    ShorturlRoutes.shorturlRoutes(shortenService).orNotFound(request)
  }

  private[this] val redirectRequest: IO[Response[IO]] = {
    val request = Request[IO](method = Method.GET, uri = uri"/app/AxMHYw")
    ShorturlRoutes.shorturlRoutes(shortenService).orNotFound(request)
  }

  private[this] val statsRequest: IO[Response[IO]] = {
    val statsRequest = Request[IO](method = Method.GET, uri = uri"/app/stats/AxMHYw")
    ShorturlRoutes.statRoutes(statsService).orNotFound(statsRequest)
  }

  private[this] val statsAllRequest: IO[Response[IO]] = {
    val statsRequest = Request[IO](method = Method.GET, uri = uri"/app/stats/all")
    ShorturlRoutes.statRoutes(statsService).orNotFound(statsRequest)
  }
}