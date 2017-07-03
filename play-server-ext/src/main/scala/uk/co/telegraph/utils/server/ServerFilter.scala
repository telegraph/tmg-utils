package uk.co.telegraph.utils.server

import java.util.{Set => JSet}
import javax.inject.{Inject, Singleton}
import play.api.http.DefaultHttpFilters
import play.api.mvc.EssentialFilter
import scala.collection.convert.WrapAsScala._

@Singleton
class ServerFilter @Inject() (applicationFilters:JSet[EssentialFilter])
  extends DefaultHttpFilters(applicationFilters.toSeq:_*)

