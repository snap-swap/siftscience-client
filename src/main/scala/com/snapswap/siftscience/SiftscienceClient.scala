package com.snapswap.siftscience

import com.snapswap.siftscience.model._

import scala.concurrent.Future

trait SiftscienceClient {
  def accountCreated(profile: String,
                     clientId: Option[String],
                     profileState: String,
                     givenName: Option[String],
                     familyName: Option[String],
                     phone: String,
                     inviter: Option[String],
                     ip: Option[String],
                     accounts: Seq[PaymentMethod] = Seq.empty[PaymentMethod],
                     promotions: Seq[Promotion] = Seq.empty[Promotion],
                     time: Long = nowUTC(),
                     postCallAction: Response => Future[Unit]): Future[Unit]

  def updateAccount(profile: String,
                    clientId: Option[String],
                    profileState: String,
                    ip: Option[String],
                    update: UpdateSiftAccount,
                    time: Long = nowUTC(),
                    postCallAction: Response => Future[Unit]): Future[Unit]

  def transaction(profile: String,
                  clientId: Option[String],
                  profileState: String,
                  ip: Option[String],
                  tx: Transaction,
                  time: Long = nowUTC(),
                  postCallAction: Response => Future[Unit]): Future[Unit]

  protected def nowUTC(): Long
}