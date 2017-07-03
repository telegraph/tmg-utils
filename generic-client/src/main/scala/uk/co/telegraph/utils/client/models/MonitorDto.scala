package uk.co.telegraph.utils.client.models

case class MonitorDto
(
  cached:Boolean,
  clients:Seq[ClientDetails]
)
