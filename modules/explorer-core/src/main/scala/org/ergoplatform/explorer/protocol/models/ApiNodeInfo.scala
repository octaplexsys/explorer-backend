package org.ergoplatform.explorer.protocol.models

import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import org.ergoplatform.explorer.{HexString, Id}

/** A model mirroring NodeInfo entity from Ergo node REST API.
  * See `NodeInfo` in https://github.com/ergoplatform/ergo/blob/master/src/main/resources/api/openapi.yaml
  */
final case class ApiNodeInfo(
  currentTime: Long,
  name: String,
  stateType: String,
  difficulty: Long,
  bestFullHeaderId: Id,
  bestHeaderId: Id,
  peersCount: Int,
  unconfirmedCount: Int,
  appVersion: String,
  stateRoot: HexString,
  previousFullHeaderId: Id,
  fullHeight: Int,
  headersHeight: Int,
  stateVersion: HexString,
  launchTime: Long,
  isMining: Boolean
)

object ApiNodeInfo {

  implicit val decoder: Decoder[ApiNodeInfo] = deriveDecoder
}
