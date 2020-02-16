package org.ergoplatform.explorer.http.api

import cats.effect.{ExitCode, Resource}
import cats.free.Free.catsFreeMonadForFree
import cats.syntax.functor._
import doobie.free.connection.ConnectionIO
import monix.eval.{Task, TaskApp}
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.explorer.db.DbTrans
import org.ergoplatform.explorer.http.api.settings.Settings
import org.ergoplatform.explorer.http.api.v0.HttpApiV0
import pureconfig.generic.auto._

object Application extends TaskApp {

  def run(args: List[String]): Task[ExitCode] =
    resources(args.headOption).use {
      case (settings, xa) =>
        implicit val e: ErgoAddressEncoder = settings.protocolSettings.addressEncoder
        HttpApiV0[Task, ConnectionIO](settings.httpSettings)(xa)
          .use(_ => Task.never)
          .as(ExitCode.Success)
    }

  private def resources(configPathOpt: Option[String]) =
    for {
      settings <- Resource.liftF(Settings.load(configPathOpt))
      xa       <- DbTrans[Task](settings.dbSettings).map(_.trans)
    } yield (settings, xa)
}