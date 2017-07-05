package uk.co.telegraph.utils

import java.net.URL

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try
import scala.collection.convert.WrapAsScala._
import scala.collection.convert.WrapAsJava._

import ConfigExtensions._

private [utils] class ConfigExtensions(left:Config){

  /**
    * Get a finite duration for a specific Path
    */
  def getFiniteDuration(path:String):FiniteDuration =
    left.getDuration(path).toNanos nanos

  /**
    * Convert a SubConfig into a Map[String, String]
    */
  def getMap(path:String):Map[String, String] = {
    left.getConfig(path)
    .root()
    .unwrapped().map({
      case (k, v) => k -> v.toString
    })
    .toMap
  }

  /**
    * Get a subConfiguration if exists. Otherwise, empty
    */
  def tryGetConfig(path:String):Config =
    if(left.hasPath(path)) left.getConfig(path) else EmptyConfig

  /**
    * Gets an option string for a given Path
    */
  def getOptionString(path:String):Option[String] =
    Try(left.getString(path)).toOption

  /**
    * Extract Url from Path
    */
  def getUrl(path:String):URL =
    new URL(left.getString(path))

  /**
    * This method is used to add a prefix to all Config Keys
    * @param prefix Prefix to be added
    *
    * @return
    */
  def prefixKeysWith(prefix:String):Config = ConfigFactory.parseMap {
    left.root().unwrapped().map({ case (k, v) => s"$prefix.$k" -> v }).toMap[String, AnyRef]
  }
}

object ConfigExtensions{
  val EmptyConfig = ConfigFactory.empty()
}
