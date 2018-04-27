package uk.co.telegraph.utils.server

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, OneInstancePerTest, Suite}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

trait TestActorSystemAndMaterializer extends BeforeAndAfterAll with ScalaFutures with OneInstancePerTest{ this: Suite =>

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  implicit lazy val TestActorSystem: ActorSystem = ActorSystem(name = "test", config = ConfigFactory.empty())
  implicit lazy val TestMaterializer: ActorMaterializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    TestMaterializer.shutdown()
    TestActorSystem.terminate()
  }
}