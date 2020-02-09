package org.ergoplatform.explorer.services

import cats.effect.IO
import org.ergoplatform.explorer.TokenId
import org.ergoplatform.explorer.services.DexContracts.TokenInfo
import org.scalatest.{Matchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class DexContractsSpec
  extends PropSpec
  with Matchers
  with ScalaCheckDrivenPropertyChecks {

  import org.ergoplatform.explorer.db.models.generators._

  property("getTokenPriceFromSellOrderTree") {
    val extractedTokenPrice =
      DexContracts
        .getTokenPriceFromSellOrderTree[IO](sellOrderErgoTree)
        .unsafeRunSync()

    extractedTokenPrice shouldBe 50000000L
  }

  property("Buy orders (enrich ExtendedOutput with token info)") {
    val extractedTokenInfo =
      DexContracts.getTokenInfoFromBuyOrderTree[IO](buyOrderErgoTree).unsafeRunSync()

    val expectedTokenInfo = TokenInfo(
      TokenId("21f84cf457802e66fb5930fb5d45fbe955933dc16a72089bf8980797f24e2fa1"),
      60
    )
    extractedTokenInfo shouldEqual expectedTokenInfo
  }
}