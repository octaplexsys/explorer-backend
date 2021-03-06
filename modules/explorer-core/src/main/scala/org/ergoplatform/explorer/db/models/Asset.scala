package org.ergoplatform.explorer.db.models

import org.ergoplatform.explorer.{BoxId, Id, TokenId}

/** Entity representing `node_assets` table.
  */
final case class Asset(
  tokenId: TokenId,
  boxId: BoxId,
  headerId: Id,
  amount: Long
)
