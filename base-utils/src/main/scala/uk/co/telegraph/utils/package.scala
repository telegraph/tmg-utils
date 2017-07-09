package uk.co.telegraph

import uk.co.telegraph.utils.config.ConfigExtensions

import scala.language.implicitConversions

package object utils extends ConfigExtensions{

  /**
    * Default System Clock
    */
  implicit val DefaultSystemClock = Clock
}
