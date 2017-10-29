package uk.co.telegraph.utils

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import org.scalatest.{BeforeAndAfterAll, Suite}

trait TestActorSystemAndMaterializer extends BeforeAndAfterAll { this: Suite =>

  implicit lazy val TestActorSystem: ActorSystem = ActorSystem()
  implicit lazy val TestMaterializer: ActorMaterializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    TestMaterializer.shutdown()
    TestActorSystem.terminate()
  }

}


trait TestActorSystemAndMaterializerWithConfig extends TestActorSystemAndMaterializer { this: Suite =>
  val TestConfig: Config
  override implicit lazy val TestActorSystem: ActorSystem = ActorSystem("test", TestConfig)
}
