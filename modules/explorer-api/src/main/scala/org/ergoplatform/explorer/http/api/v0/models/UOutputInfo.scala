package org.ergoplatform.explorer.http.api.v0.models

import io.circe.generic.semiauto.deriveCodec
import io.circe.{Codec, Json}
import org.ergoplatform.explorer.{BoxId, HexString}
import org.ergoplatform.explorer.db.models.{UAsset, UOutput}
import sttp.tapir.{Schema, SchemaType}
import sttp.tapir.generic.Derived

final case class UOutputInfo(
  boxId: BoxId,
  value: Long,
  creationHeight: Int,
  ergoTree: HexString,
  assets: List[AssetInfo],
  additionalRegisters: Json
)

object UOutputInfo {

  implicit val codec: Codec[UOutputInfo] = deriveCodec

  implicit val schema: Schema[UOutputInfo] =
    implicitly[Derived[Schema[UOutputInfo]]].value
      .modify(_.boxId)(_.description("Id of the corresponding box"))
      .modify(_.value)(_.description("Amount of nanoERGs containing in the box"))
      .modify(_.creationHeight)(_.description("Approximate height the box was created"))
      .modify(_.ergoTree)(_.description("Encoded script"))
      .modify(_.additionalRegisters)(_.description("Arbitrary key->value dictionary"))

  implicit private def registersSchema: Schema[Json] =
    Schema(
      SchemaType.SOpenProduct(
        SchemaType.SObjectInfo("AdditionalRegisters"),
        Schema(SchemaType.SString)
      )
    )

  def apply(out: UOutput, assets: List[UAsset]): UOutputInfo =
    UOutputInfo(
      out.boxId,
      out.value,
      out.creationHeight,
      out.ergoTree,
      assets.map(AssetInfo.apply),
      out.additionalRegisters
    )
}
