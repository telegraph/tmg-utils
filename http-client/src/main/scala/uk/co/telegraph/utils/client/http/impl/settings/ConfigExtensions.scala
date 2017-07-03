package uk.co.telegraph.utils.client.http.impl.settings

import java.net.URL

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.{HEAD, getForKey}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try

/**
  * Extends config object with more features
  */
private [settings] class ConfigExtensions(left:Config){

  def getMap(path:String):Map[String, String] = {
    left.getConfig(path)
      .root()
      .unwrapped().map({
      case (k, v) => k -> v.toString
    })
      .toMap
  }

  def tryGetConfig(path:String):Config = {
    if(left.hasPath(path)) left.getConfig(path) else ConfigFactory.empty()
  }

  def getFiniteDuration(path:String):FiniteDuration = {
    left.getDuration(path).toNanos nanos
  }

  def getHttpMethod(path:String):HttpMethod = {
    Try(left.getString(path)).toOption
      .flatMap  ( x => getForKey( x.toUpperCase ) )
      .getOrElse(HEAD)
  }

  def getUrl(path:String):URL =
    new URL(left.getString(path))

  def prefixKeysWith(prefix:String):Config = {
    ConfigFactory.parseMap {
      left.root().unwrapped().map({ case (k, v) => s"$prefix.$k" -> v }).toMap[String, AnyRef]
    }
  }
}
