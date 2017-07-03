package uk.co.telegraph.utils.server.model

sealed trait RequestMsg{
  type Type
  val message:Type
}

object RequestMsg {
  def apply[T](msg:T):RequestMsg = new RequestMsg {
    type Type   = T
    val message = msg
  }
}