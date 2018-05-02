package uk.co.telegraph.utils.adt

/**
  * This is for ATD Enumerations
  */

/**
  * Example Implementation:
  *
  * sealed trait SortType extends Enumeration
  * object SortType extends EnumerationCompanion[SortType] {
  *   case object Ascending extends SortType
  *   case object Descending extends SortType
  *
  *   def from = {
  *     case Ascending.value => Ascending
  *     case Descending.value => Descending
  *   }
  * }
  */
trait Enumeration { self =>
  final val value = self.toString
}

/**
  * Example Implementation:
  *
  * sealed trait SortType extends Enumeration
  * object SortType extends EnumerationCompanion[SortType] {
  *   case object Ascending extends SortType
  *   case object Descending extends SortType
  *
  *   def from = {
  *     case Ascending.value => Ascending
  *     case Descending.value => Descending
  *   }
  * }
  *
  * @tparam E the Enumeration
  */
trait EnumerationCompanion[E <: Enumeration] {
  def from: PartialFunction[String, E]
}


/**
  * Example Implementation:
  *
  * sealed trait SortType extends AliasedEnumeration
  * object SortType extends EnumerationCompanion[SortType] {
  *   case object Ascending extends SortType { val aliases = Set("asc")}
  *   case object Descending extends SortType { val aliases = Set("desc")}
  *
  *   def from = {
  *     case str if Ascending.value || Ascending.aliases.contains(str) => Ascending
  *     case str if Descending.value || Descending.aliases.contains(str) => Descending
  *   }
  * }
  */
trait AliasedEnumeration extends Enumeration {

  /**
    * @return the given aliases for an Enumeration, to be used for ADT Enum "from"
    */
  def aliases: Set[String]
}
