package org.ergoplatform.explorer.http.api.v0.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.Derived

import scala.math.BigDecimal

final case class StatsSummary(
  blocksCount: Long,
  blocksAvgTime: Long,
  totalCoins: Long,
  totalTransactionsCount: Long,
  totalFee: Long,
  totalOutput: BigDecimal,
  estimatedOutput: BigDecimal,
  totalMinerRevenue: Long,
  percentEarnedTransactionsFees: Double,
  percentTransactionVolume: Double,
  costPerTx: Long,
  lastDifficulty: Long,
  totalHashrate: Long
)

object StatsSummary {

  def empty: StatsSummary =
    StatsSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0d, 0d, 0L, 0L, 0L)

  implicit val codec: Codec[StatsSummary] = deriveCodec

  implicit val schema: Schema[StatsSummary] =
    implicitly[Derived[Schema[StatsSummary]]].value
      .modify(_.blocksCount)(_.description("Number of block within defined period"))
      .modify(_.blocksAvgTime)(_.description("Avg. block time within defined period"))
      .modify(_.totalCoins)(_.description("Total number of coins transferred within defined period"))
      .modify(_.totalTransactionsCount)(_.description("Total number of transactions within defined period"))
      .modify(_.totalFee)(_.description("Total amount of tx fees within defined period"))
      .modify(_.totalOutput)(_.description("Total amount of unspent outputs within defined period"))
      .modify(_.estimatedOutput)(_.description("Total amount of estimated outputs within defined period"))
      .modify(_.totalMinerRevenue)(_.description("Total miner revenue within defined period"))
      .modify(_.percentEarnedTransactionsFees)(_.description("Percent of tx fees in total amount of coins within defined period"))
      .modify(_.percentTransactionVolume)(_.description("Percent of miner rewards in total amount of coins within defined period"))
      .modify(_.costPerTx)(_.description("Avg. transaction fee within defined period"))
      .modify(_.lastDifficulty)(_.description("Latest network difficulty"))
      .modify(_.totalHashrate)(_.description("Total network hashrate within defined period"))
}
