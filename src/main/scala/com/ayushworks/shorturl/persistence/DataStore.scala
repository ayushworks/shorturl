package com.ayushworks.shorturl.persistence

import cats.Applicative
import cats.implicits._
import scala.collection.concurrent.TrieMap

trait DataStore[F[_]] {

  def save(url: String, shorten: String) : F[String]

  def get(shorten: String): F[Option[String]]

  def exists(url: String): F[Option[String]]

  def stats(shorten: String): F[Int]

  def getAll : F[List[(String, String)]]
}

class DataStoreInMemory[F[_]: Applicative] extends DataStore[F] {

  private val cache = new TrieMap[String, String]
  private val urlCache = new TrieMap[String,String]
  private val statsCache = new TrieMap[String,Int]

  override def save(url: String, shorten: String): F[String] = {
    cache.putIfAbsent(shorten,url)
    urlCache.putIfAbsent(url,shorten)
    shorten.pure[F]
  }

  override def get(shorten: String): F[Option[String]] = {
    val value = cache.get(shorten)
    value map {
      _ => statsCache.get(shorten) match {
        case Some(counter) => statsCache.replace(shorten,counter+1)
        case None => statsCache.put(shorten, 1)
      }
    }
    value.pure[F]
  }

  override def exists(url: String): F[Option[String]] = urlCache.get(url).pure[F]

  override def stats(shorten: String): F[Int] = statsCache.getOrElse(shorten, 0).pure[F]

  override def getAll: F[List[(String, String)]] = cache.toList.pure[F]
}

object DataStoreInMemory {
  def apply[F[_]: Applicative]() = new DataStoreInMemory[F]
}