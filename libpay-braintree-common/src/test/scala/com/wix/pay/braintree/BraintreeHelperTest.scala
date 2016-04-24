/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) 2006-2014, Wix LTD.             **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */
package com.wix.pay.braintree

import com.braintreegateway.TransactionRequest
import com.wix.pay.creditcard.{CreditCard, YearMonth}
import org.specs2.matcher.Scope
import org.specs2.mutable.SpecWithJUnit

import scala.xml.XML


class BraintreeHelperTest extends SpecWithJUnit {
  trait Ctx extends Scope {}

  private def getChannel(transaction: TransactionRequest): Option[String] = {
    // TransactionRequest doesn't provide direct access to the channel member
    val xml = XML.loadString(transaction.toXML)
    Option((xml \ "channel").text) match {
      case Some(channel) => channel match {
        case "" => None
        case _ => Some(channel)
      }
      case None => None
    }
  }

  "createTransactionRequest" should {
    val someMerchantAccountId = "some merchant account ID"
    val someCard = CreditCard(
      number = "4580458045804580",
      expiration = YearMonth(
        year = 2020,
        month = 1
      )
    )
    val someAmount = 11.1

    "include channel in transaction when given a channel" in new Ctx {
      val someChannel = "someChannel"

      val transaction = BraintreeHelper.createTransactionRequest(
        merchantAccountId = someMerchantAccountId,
        creditCard = someCard,
        amount = someAmount,
        channel = Some(someChannel),
        submitForSettlement = true
      )

      getChannel(transaction) must beEqualTo(Some(someChannel))
    }

    "not include channel in transaction when not given a channel" in new Ctx {
      val transaction = BraintreeHelper.createTransactionRequest(
        merchantAccountId = someMerchantAccountId,
        creditCard = someCard,
        amount = someAmount,
        submitForSettlement = true
      )

      getChannel(transaction) must beEqualTo(None)
    }
  }
}
