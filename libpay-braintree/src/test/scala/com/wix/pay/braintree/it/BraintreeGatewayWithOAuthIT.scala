package com.wix.pay.braintree.it

import com.braintreegateway.Environment
import com.wix.pay.PaymentGateway
import com.wix.pay.braintree._
import com.wix.pay.creditcard.{CreditCard, YearMonth}
import com.wix.pay.model.CurrencyAmount
import org.specs2.mutable.SpecWithJUnit


class BraintreeGatewayWithOAuthIT extends SpecWithJUnit {

  val sandboxValidCC = "5555555555554444"
  val validSandboxToken = "need to create a test token cuz repo is public"

  val merchantParser = new JsonBraintreeOAuthMerchantParser()
  val authorizationParser = new JsonBraintreeAuthorizationParser()

  val someMerchant = new BraintreeOAuthMerchant(token = validSandboxToken)

  val merchantKey = merchantParser.stringify(someMerchant)

  val someCreditCard = CreditCard(sandboxValidCC, YearMonth(2020, 12))

  val braintree: PaymentGateway = new BraintreeGateway(
    merchantParser = merchantParser,
    authorizationParser = authorizationParser,
    environment = Environment.SANDBOX,
    channel = Some("someChannel")
  )

  "sale request via Sandbox Braintree gateway using OAuth merchant" should {

    "pass sale" in {
      val saleResult = braintree.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = CurrencyAmount("USD", 33.3)
      )

      saleResult must beSuccessfulTry
      saleResult.get must not(beEmpty)
    }
  }

}
