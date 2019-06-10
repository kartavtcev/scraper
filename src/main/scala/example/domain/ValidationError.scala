package example.domain

sealed trait ValidationError extends Product with Serializable
case object AlreadyExistsError extends ValidationError
case object NotFoundError extends ValidationError