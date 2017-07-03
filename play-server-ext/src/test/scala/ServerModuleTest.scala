import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.Key.get
import com.google.inject.multibindings.Multibinder.newSetBinder
import com.google.inject.{AbstractModule, Guice, Injector, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.Mockito
import org.scalatest._
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.filters.cors.{CORSConfig, CORSFilter}
import uk.co.telegraph.utils.client.GenericClient
import uk.co.telegraph.utils.client.monitor.Monitor
import uk.co.telegraph.utils.server.ServerModule

import scala.collection.convert.WrapAsScala._

class ServerModuleTest
  extends FreeSpec
  with Matchers
  with BeforeAndAfter
  with BeforeAndAfterAll
  with OneInstancePerTest
{

  import ServerModuleTest._

  val InjectorTest: Injector = Guice.createInjector(new MockModule(), new ServerModule())

  override protected def afterAll(): Unit = {
    ActorSystemTest.terminate()
  }

  "Given the ServerModule, " - {

    "I should be able to provide a monitor" in {
      InjectorTest.getProvider(classOf[Monitor]).get() shouldBe a [Monitor]
    }

    "I should be able to have 3 filters: CORSFilter, EventIdFilter, AuthFilter" in {
      InjectorTest.getBindings.map(_._1.getTypeLiteral.getType.getTypeName) should contain ("java.util.Set<uk.co.telegraph.utils.client.GenericClient>")
    }
  }
}

object ServerModuleTest{

  val ConfigTest: Config = ConfigFactory.load("application.conf")

  implicit val ActorSystemTest = ActorSystem("server-module", ConfigTest)
  implicit val MaterializerTest = ActorMaterializer()

  val CORSConfigMock: CORSConfig = Mockito.mock(classOf[CORSConfig])
  val HttpErrorHandlerMock: HttpErrorHandler = Mockito.mock(classOf[HttpErrorHandler])

  class MockModule extends AbstractModule{
    override def configure(): Unit = {
      newSetBinder(binder(), get(classOf[GenericClient]))
      bind(classOf[ActorSystem]).toInstance(ActorSystemTest)
      bind(classOf[Materializer]).toInstance(MaterializerTest)
      bind(classOf[Configuration]).toInstance(Configuration.empty)
    }

    @Provides
    def corsFilterProvider:CORSFilter = {
      new CORSFilter()
    }
  }
}
