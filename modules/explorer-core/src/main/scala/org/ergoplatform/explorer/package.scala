package org.ergoplatform

import cats.Applicative
import cats.instances.either._
import cats.syntax.either._
import cats.syntax.functor._
import doobie.refined.implicits._
import doobie.util.{Get, Put}
import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.{W, refineV}
import eu.timepit.refined.string.{HexStringSpec, MatchesRegex, Url}
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import org.ergoplatform.explorer.Err.RefinementFailed
import org.ergoplatform.explorer.UrlString.fromString
import org.ergoplatform.explorer.constraints._
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import sttp.tapir.json.circe._
import sttp.tapir.{Codec, CodecFormat, DecodeResult, Schema}
import tofu.Raise.ContravariantRaise
import tofu.syntax.raise._

package object explorer {

  type CRaise[F[_], -E] = ContravariantRaise[F, E]

  object constraints {

    final case class ValidOrdering()
    object ValidOrdering {
      implicit def validLongValidate: Validate.Plain[String, ValidOrdering] =
        Validate.fromPredicate(
          x => x.toLowerCase == "asc" || x.toLowerCase == "desc",
          _ => "ValidOrdering",
          ValidOrdering()
        )
    }

    type OrderingString = String Refined ValidOrdering

    type Base58Spec = MatchesRegex[W.`"[1-9A-HJ-NP-Za-km-z]+"`.T]

    type AddressType = String Refined Base58Spec

    type HexStringType = String Refined HexStringSpec

    type UrlStringType = String Refined Url
  }

  /** Persistent modifier id (header, block_transaction, etc.)
    */
  @newtype case class Id(value: String)

  object Id {
    // doobie instances
    implicit def get: Get[Id] = deriving
    implicit def put: Put[Id] = deriving

    // circe instances
    implicit def encoder: Encoder[Id] = deriving
    implicit def decoder: Decoder[Id] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[Id] = deriving

    implicit def jsonCodec: Codec.JsonCodec[Id] =
      implicitly[Codec.JsonCodec[String]].map(Id.apply)(_.value)

    implicit def schema: Schema[Id] =
      jsonCodec.meta.schema.description("Modifier ID")
  }

  @newtype case class TxId(value: String)

  object TxId {
    // doobie instances
    implicit def get: Get[TxId] = deriving
    implicit def put: Put[TxId] = deriving

    // circe instances
    implicit def encoder: Encoder[TxId] = deriving
    implicit def decoder: Decoder[TxId] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[TxId] = deriving

    implicit def jsonCodec: Codec.JsonCodec[TxId] =
      implicitly[Codec.JsonCodec[String]].map(TxId.apply)(_.value)

    implicit def schema: Schema[TxId] =
      jsonCodec.meta.schema.description("Transaction ID")
  }

  @newtype case class BoxId(value: String)

  object BoxId {
    // doobie instances
    implicit def get: Get[BoxId] = deriving
    implicit def put: Put[BoxId] = deriving

    // circe instances
    implicit def encoder: Encoder[BoxId] = deriving
    implicit def decoder: Decoder[BoxId] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[BoxId] = deriving

    implicit def jsonCodec: Codec.JsonCodec[BoxId] =
      implicitly[Codec.JsonCodec[String]].map(BoxId.apply)(_.value)

    implicit def schema: Schema[BoxId] =
      jsonCodec.meta.schema.description("Box ID")
  }

  @newtype case class TokenId(value: HexString)

  object TokenId {
    // doobie instances
    implicit def get: Get[TokenId] = deriving
    implicit def put: Put[TokenId] = deriving

    // circe instances
    implicit def encoder: Encoder[TokenId] = deriving
    implicit def decoder: Decoder[TokenId] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[TokenId] = deriving

    implicit def jsonCodec: Codec.JsonCodec[TokenId] =
      HexString.jsonCodec.map(TokenId.apply)(_.value)

    implicit def schema: Schema[TokenId] =
      jsonCodec.meta.schema.description("Token ID")

    def fromString[
      F[_]: CRaise[*[_], RefinementFailed]: Applicative
    ](s: String): F[TokenId] =
      HexString.fromString(s).map(TokenId.apply)
  }

  // Ergo Address
  @newtype case class Address(value: AddressType) {
    final def unwrapped: String = value.value
  }

  object Address {
    // doobie instances
    implicit def get: Get[Address] = deriving
    implicit def put: Put[Address] = deriving

    // circe instances
    implicit def encoder: Encoder[Address] = deriving
    implicit def decoder: Decoder[Address] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[Address] =
      deriveCodec[String, CodecFormat.TextPlain, Address](
        fromString[Either[Throwable, *]](_),
        _.unwrapped
      )

    implicit def jsonCodec: Codec.JsonCodec[Address] =
      deriveCodec[String, CodecFormat.Json, Address](
        fromString[Either[Throwable, *]](_),
        _.unwrapped
      )

    implicit def schema: Schema[Address] =
      jsonCodec.meta.schema.description("Ergo Address")

    implicit def configReader: ConfigReader[Address] =
      implicitly[ConfigReader[String]].emap { s =>
        fromString[Either[RefinementFailed, *]](s).leftMap(e =>
          CannotConvert(s, s"Refined", e.msg)
        )
      }

    def fromString[
      F[_]: CRaise[*[_], RefinementFailed]: Applicative
    ](s: String): F[Address] =
      refineV[Base58Spec](s)
        .leftMap(RefinementFailed)
        .toRaise[F]
        .map(Address.apply)
  }

  @newtype case class HexString(value: HexStringType) {
    final def unwrapped: String = value.value
  }

  object HexString {
    // doobie instances
    implicit def get: Get[HexString] = deriving
    implicit def put: Put[HexString] = deriving

    // circe instances
    implicit def encoder: Encoder[HexString] = deriving
    implicit def decoder: Decoder[HexString] = deriving

    // tapir instances
    implicit def plainCodec: Codec.PlainCodec[HexString] =
      deriveCodec[String, CodecFormat.TextPlain, HexString](
        fromString[Either[Throwable, *]](_),
        _.unwrapped
      )

    implicit def jsonCodec: Codec.JsonCodec[HexString] =
      deriveCodec[String, CodecFormat.Json, HexString](
        fromString[Either[Throwable, *]](_),
        _.unwrapped
      )

    implicit def schema: Schema[HexString] =
      jsonCodec.meta.schema.description("Hex-encoded string")

    def fromString[
      F[_]: CRaise[*[_], RefinementFailed]: Applicative
    ](s: String): F[HexString] =
      refineV[HexStringSpec](s)
        .leftMap(RefinementFailed)
        .toRaise[F]
        .map(HexString.apply)
  }

  @newtype case class UrlString(value: UrlStringType) {
    final def unwrapped: String = value.value
  }

  object UrlString {
    // doobie instances
    implicit def get: Get[UrlString] = deriving
    implicit def put: Put[UrlString] = deriving

    // circe instances
    implicit def encoder: Encoder[UrlString] = deriving
    implicit def decoder: Decoder[UrlString] = deriving

    implicit def configReader: ConfigReader[UrlString] =
      implicitly[ConfigReader[String]].emap { s =>
        fromString[Either[RefinementFailed, *]](s).leftMap(e =>
          CannotConvert(s, s"Refined", e.msg)
        )
      }

    def fromString[
      F[_]: CRaise[*[_], RefinementFailed]: Applicative
    ](s: String): F[UrlString] =
      refineV[Url](s)
        .leftMap(RefinementFailed)
        .toRaise[F]
        .map(UrlString.apply)
  }

  @newtype case class ContractAttributes(value: Map[String, String])

  object ContractAttributes {
    // circe instances
    implicit def encoder: Encoder[ContractAttributes] = deriving
    implicit def decoder: Decoder[ContractAttributes] = deriving

    implicit def jsonCodec: Codec.JsonCodec[ContractAttributes] =
      implicitly[Codec.JsonCodec[Map[String, String]]]
        .map(ContractAttributes.apply)(_.value)

    implicit def schema: Schema[ContractAttributes] =
      jsonCodec.meta.schema.description("ContractAttributes")
  }

  private def deriveCodec[A, CF <: CodecFormat, T](
    at: A => Either[Throwable, T],
    ta: T => A
  )(implicit c: Codec[A, CF, String]): Codec[T, CF, String] =
    c.mapDecode { x =>
      at(x).fold(DecodeResult.Error(x.toString, _), DecodeResult.Value(_))
    }(ta)
}
