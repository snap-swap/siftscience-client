package com.snapswap.siftscience

import com.snapswap.siftscience.model.{PaymentMethod, Promotion, Transaction, UpdateSiftAccount}

import scala.concurrent.Future

trait SiftscienceClient {
  def accountCreated(profile: String,
                     clientId: String,
                     profileState: String,
                     givenName: String,
                     familyName: String,
                     phone: String,
                     inviter: Option[String],
                     accounts: Seq[PaymentMethod],
                     promotions: Seq[Promotion],
                     ip: String,
                     time: Long): Future[Unit]

  def updateAccount(profile: String,
                    clientId: String,
                    profileState: String,
                    ip: String,
                    time: Long,
                    update: UpdateSiftAccount): Future[Unit]

  def transaction(profile: String,
                  clientId: String,
                  profileState: String,
                  ip: String,
                  time: Long,
                  tx: Transaction): Future[Unit]
}