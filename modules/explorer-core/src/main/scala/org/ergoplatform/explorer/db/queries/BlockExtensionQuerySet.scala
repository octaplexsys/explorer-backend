package org.ergoplatform.explorer.db.queries

import doobie.implicits._
import doobie.refined.implicits._
import doobie.util.query.Query0
import org.ergoplatform.explorer.Id
import org.ergoplatform.explorer.db.models.BlockExtension

/** A set of queries for doobie implementation of [BlockExtensionRepo].
  */
object BlockExtensionQuerySet extends QuerySet {

  import org.ergoplatform.explorer.db.doobieInstances._

  val tableName: String = "node_extensions"

  val fields: List[String] = List(
    "header_id",
    "digest",
    "fields"
  )

  def getByHeaderId(headerId: Id): Query0[BlockExtension] =
    sql"select * from node_extensions where header_id = $headerId"
      .query[BlockExtension]
}
