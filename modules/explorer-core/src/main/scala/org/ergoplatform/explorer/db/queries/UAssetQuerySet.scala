package org.ergoplatform.explorer.db.queries

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.Fragments
import doobie.util.query.Query0
import org.ergoplatform.explorer.BoxId
import org.ergoplatform.explorer.db.models.UAsset

object UAssetQuerySet extends QuerySet {

  val tableName: String = "node_u_assets"

  val fields: List[String] = List(
    "token_id",
    "box_id",
    "value"
  )

  def getAllByBoxId(boxId: BoxId): Query0[UAsset] =
    sql"select * from node_u_assets where box_id = $boxId".query[UAsset]

  def getAllByBoxIds(boxIds: NonEmptyList[BoxId]): Query0[UAsset] =
    (sql"select * from node_u_assets " ++ Fragments.in(fr"where box_id", boxIds))
      .query[UAsset]
}
