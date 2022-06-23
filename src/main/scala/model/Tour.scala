package model

case class Tour(
  source: String,
  destination: String,
  cost: String,
  description: String,
  id: Option[Long] = None,
)


