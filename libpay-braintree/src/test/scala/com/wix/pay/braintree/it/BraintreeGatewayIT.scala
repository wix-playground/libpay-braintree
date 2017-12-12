package com.wix.pay.braintree.it


import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope
import akka.http.scaladsl.model.StatusCodes
import com.braintreegateway.Environment
import com.wix.pay.braintree._
import com.wix.pay.braintree.testkit.BraintreeDriver
import com.wix.pay.creditcard.{CreditCard, CreditCardOptionalFields, YearMonth}
import com.wix.pay.model.{CurrencyAmount, Payment}
import com.wix.pay.{PaymentErrorException, PaymentGateway, PaymentRejectedException}


class BraintreeGatewayIT extends SpecWithJUnit {
  val braintreePort = 10001

  val driver = new BraintreeDriver(port = braintreePort)
  val merchantParser = new JsonBraintreeMerchantParser()
  val authorizationParser = new JsonBraintreeAuthorizationParser()

  val someChannel = "someChannel"

  val someMerchant = BraintreeMerchant(
    merchantId = "someMerchantID",
    publicKey = "somePublicKey",
    privateKey = "somePrivateKey",
    merchantAccountIds = Map("USD" -> "someMerchantAccountID"))
  val merchantKey: String = merchantParser.stringify(someMerchant)
  val someCurrencyAmount = CurrencyAmount("USD", 33.3)
  val somePayment = Payment(someCurrencyAmount, 1)
  val someCreditCard = CreditCard(
    "4580458045804580",
    YearMonth(2020, 12),
    Some(CreditCardOptionalFields.withFields(
      csc = Some("123"),
      holderName = Some("some holder name"),
      billingAddress = Some("some billing address"),
      billingPostalCode = Some("90210"))))


  step {
    driver.start()
  }

  sequential


  trait Ctx extends Scope {
    val braintree: PaymentGateway = new BraintreeGateway(
      merchantParser = merchantParser,
      authorizationParser = authorizationParser,
      environment = new Environment(s"http://localhost:$braintreePort", "http://auth.venmo.dev:9292", Array(), "test"),
      channel = Some(someChannel)
    )

    driver.reset()
  }


  "sale request via Braintree gateway" should {
    "gracefully fail on invalid card number" in new Ctx {
      driver.aCreateSaleRequestFor(
        someMerchant.merchantId,
        someMerchant.publicKey,
        someMerchant.privateKey,
        someCurrencyAmount,
        someCreditCard) failsOnInvalidCardNumber()

      braintree.sale(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beAFailedTry(check = beAnInstanceOf[PaymentRejectedException])
    }
  }


  "authorize request via Braintree gateway" should {
    "gracefully fail on invalid merchant key" in new Ctx {
      driver.aCreateSaleRequestFor(
        someMerchant.merchantId,
        someMerchant.publicKey,
        someMerchant.privateKey,
        someCurrencyAmount,
        someCreditCard) errors StatusCodes.Unauthorized

      braintree.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beAFailedTry(check = beAnInstanceOf[PaymentErrorException])
    }

    "successfully yield an authorization key on valid request" in new Ctx {
      val someTransactionId = "someTransactionID"
      val authorizationKey: String = authorizationParser.stringify(BraintreeAuthorization(someTransactionId))

      driver.aCreateSaleRequestFor(
        someMerchant.merchantId,
        someMerchant.publicKey,
        someMerchant.privateKey,
        someCurrencyAmount,
        someCreditCard) succeedsWith someTransactionId

      braintree.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beASuccessfulTry(check = ===(authorizationKey))
    }

    "gracefully fail on rejected payment" in new Ctx {
      driver.aCreateSaleRequestFor(
        someMerchant.merchantId,
        someMerchant.publicKey,
        someMerchant.privateKey,
        someCurrencyAmount,
        someCreditCard) failsWith "processor_declined"

      braintree.authorize(
        merchantKey = merchantKey,
        creditCard = someCreditCard,
        payment = somePayment) must beAFailedTry(check = beAnInstanceOf[PaymentRejectedException])
    }
  }


  step {
    driver.stop()
  }
}
