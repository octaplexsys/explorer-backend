package org.ergoplatform.explorer.db.repositories

import cats.data.NonEmptyList
import cats.implicits._
import doobie.implicits._
import fs2.Stream
import org.ergoplatform.explorer.db.algebra.LiftConnectionIO
import org.ergoplatform.explorer.db.syntax.liftConnectionIO._
import org.ergoplatform.explorer.db.models.Asset
import org.ergoplatform.explorer.db.models.aggregates.ExtendedOutput
import org.ergoplatform.explorer.{Address, BoxId, TokenId}

/** [[Asset]] data access operations.
  */
trait AssetRepo[D[_], S[_[_], _]] {

  /** Put a given `asset` to persistence.
    */
  def insert(asset: Asset): D[Unit]

  /** Put a given list of assets to persistence.
    */
  def insertMany(assets: List[Asset]): D[Unit]

  /** Get all assets belonging to a given `boxId`.
    */
  def getAllByBoxId(boxId: BoxId): D[List[Asset]]

  /** Get all assets belonging to a given list of `boxId`.
    */
  def getAllByBoxIds(boxIds: NonEmptyList[BoxId]): D[List[Asset]]

  /** Get all addresses holding an asset with a given `assetId`.
    */
  def getAllHoldingAddresses(
    tokenId: TokenId,
    offset: Int,
    limit: Int
  ): S[D, Address]

  /** Get boxes where all tokens where issued
    * according to EIP-4 https://github.com/ergoplatform/eips/blob/master/eip-0004.md
    */
  def getAllIssuingBoxes(offset: Int, limit: Int): S[D, ExtendedOutput]

  /** Get boxes where given tokens where issued
    * according to EIP-4 https://github.com/ergoplatform/eips/blob/master/eip-0004.md
    */
  def getIssuingBoxesByTokenIds(tokenIds: NonEmptyList[TokenId]): D[List[ExtendedOutput]]
}

object AssetRepo {

  def apply[D[_]: LiftConnectionIO]: AssetRepo[D, Stream] =
    new Live[D]

  final private class Live[D[_]: LiftConnectionIO] extends AssetRepo[D, Stream] {

    import org.ergoplatform.explorer.db.queries.{AssetQuerySet => QS}

    def insert(asset: Asset): D[Unit] =
      QS.insert(asset).void.liftConnectionIO

    def insertMany(assets: List[Asset]): D[Unit] =
      QS.insertMany(assets).void.liftConnectionIO

    def getAllByBoxId(boxId: BoxId): D[List[Asset]] =
      QS.getAllByBoxId(boxId).to[List].liftConnectionIO

    def getAllByBoxIds(boxIds: NonEmptyList[BoxId]): D[List[Asset]] =
      QS.getAllByBoxIds(boxIds).to[List].liftConnectionIO

    def getAllHoldingAddresses(
      tokenId: TokenId,
      offset: Int,
      limit: Int
    ): Stream[D, Address] =
      QS.getAllHoldingAddresses(tokenId, offset, limit)
        .stream
        .translate(LiftConnectionIO[D].liftConnectionIOK)

    def getAllIssuingBoxes(offset: Int, limit: Int): Stream[D, ExtendedOutput] =
      QS.getAllIssuingBoxes(offset, limit)
        .stream
        .translate(LiftConnectionIO[D].liftConnectionIOK)

    def getIssuingBoxesByTokenIds(
      tokenIds: NonEmptyList[TokenId]
    ): D[List[ExtendedOutput]] =
      QS.getIssuingBoxes(tokenIds).to[List].liftConnectionIO
  }
}
