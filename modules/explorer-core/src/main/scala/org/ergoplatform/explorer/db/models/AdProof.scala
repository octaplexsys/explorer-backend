package org.ergoplatform.explorer.db.models

import org.ergoplatform.explorer.protocol.models.ApiAdProof
import org.ergoplatform.explorer.{HexString, Id}

/** Entity representing `node_ad_proofs` table.
  */
final case class AdProof(
  headerId: Id,
  proofBytes: HexString, // serialized and hex-encoded AVL+ tree path
  digest: HexString      // hex-encoded tree root hash
)

object AdProof {

  def fromApi(apiAdProof: ApiAdProof): AdProof =
    AdProof(
      apiAdProof.headerId,
      apiAdProof.proofBytes,
      apiAdProof.digest
    )
}
