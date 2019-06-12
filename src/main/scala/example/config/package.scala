package example

import io.circe.Decoder
import io.circe.generic.semiauto._

package object config {
  implicit val scDec: Decoder[ServerConfig] = deriveDecoder
  implicit val dbDec: Decoder[DatabaseConfig] = deriveDecoder
  implicit val wcDec: Decoder[WebCrawlerConfig] = deriveDecoder

  implicit val appDec: Decoder[ApplicationConfigs] = deriveDecoder
}
