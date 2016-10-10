package com.wix.pay.braintree.it

import com.braintreegateway.Environment
import com.wix.pay.PaymentGateway
import com.wix.pay.braintree._
import com.wix.pay.braintree.testkit.BraintreeOAuthDriver
import com.wix.pay.braintree.testkit.BraintreeOAuthDriver._
import com.wix.pay.creditcard.{CreditCard, YearMonth}
import com.wix.pay.model.CurrencyAmount
import org.specs2.mutable.SpecWithJUnit


class BraintreeGatewayWithOAuthIT extends SpecWithJUnit {

  val driver = new BraintreeOAuthDriver()

  val merchantParser = new JsonBraintreeMerchantParser()
  val authorizationParser = new JsonBraintreeAuthorizationParser()

  val someMerchant = BraintreeOAuthMerchant(validMerchantToken)

  val merchantKey = merchantParser.stringify(someMerchant)

  val someCreditCard = CreditCard(passingCreditCardNumber, YearMonth(2020, 12))


  val braintree: PaymentGateway = new BraintreeGateway(
    merchantParser = merchantParser,
    authorizationParser = authorizationParser,
    environment = Environment.SANDBOX,
    channel = Some("someChannel")
  )

  step {
    driver.start()
  }

  "sale request via Braintree gateway using OAuth merchant" should {

    "pass sale" in {

      val saleResult = braintree.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        currencyAmount = CurrencyAmount("USD", 33.3)
      )

      saleResult.get mustEqual expectedTransactionId
    }
  }

  step {
    driver.stop()
  }

}
