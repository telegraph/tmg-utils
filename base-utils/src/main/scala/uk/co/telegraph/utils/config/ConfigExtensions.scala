package uk.co.telegraph.utils.config

import java.net.URL

import com.typesafe.config.ConfigFactory.parseMap
import com.typesafe.config.{Config, ConfigFactory, ConfigMemorySize}
import uk.co.telegraph.utils.cipher.Decrypter

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try

trait ConfigExtensions {
  implicit def toConfigExtensions(left:Config):ConfigExtensionsImpl = {
    new ConfigExtensionsImpl(left)
  }

  private [utils] class ConfigExtensionsImpl (left:Config){

    import ConfigLoader._

    /**
      * Extract a Generic Type given a path
      */
    def get[A](path:String)(implicit configLoader: ConfigLoader[A]):A = {
      configLoader.load(left, path)
    }

    /**
      * Extract an Option value given a path
      */
    def getOption[A](path:String)(implicit configLoader: ConfigLoader[A]):Option[A] = {
      if( left.hasPath(path) ) Some(get[A](path)) else None
    }

    /**
      * Extract a Try object given a Path
      */
    def getTry[A](path:String)(implicit configLoader: ConfigLoader[A]):Try[A] = {
      Try{ get[A](path) }
    }

    /**
      * Extract a map
      */
    def toMap[A](path:String)(implicit configLoader: ConfigLoader[A]):Map[String, A] = {
      left.getConfig(path).entrySet().map{ x => {
        (x.getKey, left.get[A](s"$path.${x.getKey}"))
      }}
      .toMap
    }

    /**
      * Extract a sub configuration or return empty
      */
    def getConfigOrEmpty(path:String):Config = {
      getOption[Config](path).getOrElse(ConfigFactory.empty)
    }

    /**
      * Transform Keys with a specific prefix
      */
    def prefixKeysWith(prefix:String):Config = parseMap {
      left.root().unwrapped().map({ case (k, v) => s"$prefix.$k" -> v }).toMap[String, AnyRef]
    }

    /**
      * Get Encrypted value
      */
    def getEncrypted[A](path:String)(implicit configLoader:ConfigLoader[A], decrypter:Decrypter):A = {
      val data = decryptPath(path)

      parseMap(Map(path -> data)).get[A](path)
    }

    /**
      * Get encrypted configuration
      */
    def getEncryptedConfig(path:String)(implicit decrypter:Decrypter):Config = {
      (decryptPath _ andThen ConfigFactory.parseString)(path)
    }

    private def decryptPath(path:String)(implicit decrypter:Decrypter):String =
      (get[String] _ ).andThen(decrypter.decrypt)(path)
  }
}


trait ConfigLoader[A] { seft =>
  def load(config:Config, path:String):A
  def map[B](f: A => B ):ConfigLoader[B] = new ConfigLoader[B] {
    override def load(config: Config, path: String): B = {
      f(seft.load(config, path))
    }
  }
}

object ConfigLoader {

  def apply[A]( f: Config => String => A ):ConfigLoader[A] = new ConfigLoader[A] {
    override def load(config: Config, path: String): A = f(config)(path)
  }

  //Config => String
  implicit val stringLoader: ConfigLoader[String] = ConfigLoader(_.getString)
  implicit val seqStringLoader: ConfigLoader[Seq[String]] = ConfigLoader(_.getStringList).map(_.toList)

  //Config => Int
  implicit val intLoader: ConfigLoader[Int] = ConfigLoader(_.getInt)
  implicit val seqIntLoader: ConfigLoader[Seq[Int]] = ConfigLoader(_.getIntList).map(_.toList.map(_.toInt))

  //Config => Double
  implicit val doubleLoader: ConfigLoader[Double] = ConfigLoader(_.getDouble)
  implicit val seqDoubleLoader: ConfigLoader[Seq[Double]] = ConfigLoader(_.getDoubleList).map(_.toList.map(_.toDouble))

  //Config => Float
  implicit val booleanLoader: ConfigLoader[Boolean] = ConfigLoader(_.getBoolean)
  implicit val seqBooleanLoader: ConfigLoader[Seq[Boolean]] = ConfigLoader(_.getBooleanList).map( lst => lst.toList.map({
    case java.lang.Boolean.TRUE  => true
    case java.lang.Boolean.FALSE => false
  }))

  //Config => Config
  implicit val configLoader: ConfigLoader[Config] = ConfigLoader(_.getConfig)
  implicit val seqConfigLoader: ConfigLoader[Seq[Config]] = ConfigLoader(_.getConfigList).map(_.toList)

  //Config => Bytes
  implicit val bytesLoader: ConfigLoader[ConfigMemorySize] = ConfigLoader(_.getMemorySize)
  implicit val seqBytesLoader: ConfigLoader[Seq[ConfigMemorySize]] = ConfigLoader(_.getMemorySizeList).map(_.toList)

  //Config => Url
  implicit val urlLoader: ConfigLoader[URL] = stringLoader.map(new URL(_))
  implicit val seqUrlLoader: ConfigLoader[Seq[URL]] = seqStringLoader.map( _.map(new URL(_)))

  //Config => Duration
  implicit val durationLoader: ConfigLoader[Duration] = ConfigLoader { config => path =>
    if (!config.getIsNull(path)) config.getDuration(path).toNanos nanos else Duration.Inf
  }
  implicit val seqDurationLoader: ConfigLoader[Seq[Duration]] = ConfigLoader(_.getDurationList).map(_.toList.map(_.toNanos.nanos))

  //Config => FiniteDuration
  implicit val finiteDurationLoader: ConfigLoader[FiniteDuration] = ConfigLoader(_.getDuration).map(_.toNanos nanos)
  implicit val seqFiniteDurationLoader: ConfigLoader[Seq[FiniteDuration]] = ConfigLoader(_.getDurationList).map(_.toList.map(_.toNanos.nanos))
}
