package com.ayushworks.shorturl

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) =
    ShorturlServer.stream[IO].compile.drain.as(ExitCode.Success)
}
