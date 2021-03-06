package org.ergoplatform.explorer.grabber

import cats.effect._
import doobie.free.connection.ConnectionIO
import monocle.macros.syntax.lens._
import org.ergoplatform.explorer.MainNetConfiguration
import org.ergoplatform.explorer.db.RealDbTest
import org.ergoplatform.explorer.grabber.GrabberTestNetworkClient.Source
import org.ergoplatform.explorer.protocol.models.ApiFullBlock
import org.ergoplatform.explorer.settings.GrabberAppSettings
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.PropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.duration._

class ChainGrabberSpec
  extends PropSpec
  with ScalaCheckDrivenPropertyChecks
  with RealDbTest
  with MainNetConfiguration {

  import org.ergoplatform.explorer.commonGenerators._
  import org.ergoplatform.explorer.testConstants._

  private lazy val settings =
    GrabberAppSettings(1.second, mainnetNodes, dbSettings, protocolSettings)

  property("Network scanning") {
    forAll(consistentChainGen(12)) { apiBlocks =>
      whenever(apiBlocks.map(_.transactions.transactions).forall(_.nonEmpty)) {
        val networkService = new GrabberTestNetworkClient[IO](Source(apiBlocks))
        ChainGrabber[IO, ConnectionIO](settings, networkService)(xa.trans)
          .flatMap(_.run.take(1L).compile.drain)
          .unsafeRunSync()
      }
    }
  }

  private def consistentChainGen(length: Int): Gen[List[ApiFullBlock]] =
    Gen
      .listOfN(length, implicitly[Arbitrary[ApiFullBlock]].arbitrary)
      .map { rawBlocks =>
        rawBlocks
          .foldLeft(List.empty[ApiFullBlock]) {
            case (Nil, block) =>
              List(block)
            case (acc @ parent :: _, block) =>
              val blockUpd = block
                .lens(_.header.parentId)
                .modify(_ => parent.header.id)
              blockUpd +: acc
          }
          .reverse
          .zipWithIndex
          .map {
            case (block, idx) =>
              val blockId = block.header.id
              block
                .lens(_.header.height)
                .modify(_ => idx)
                .lens(_.header.minerPk)
                .modify(_ => MainNetMinerPk)
                .lens(_.extension.headerId)
                .modify(_ => blockId)
                .lens(_.transactions.headerId)
                .modify(_ => blockId)
                .lens(_.adProofs)
                .modify(_.map(_.copy(headerId = blockId)))
          }
      }

}
