package com.wix.pay.braintree


import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope


class JsonBraintreeMerchantParserTest extends SpecWithJUnit {
  trait Ctx extends Scope {
    val merchantParser: BraintreeMerchantParser = new JsonBraintreeMerchantParser
  }

  "stringify and then parse" should {
    "yield a merchant similar to the original one" in new Ctx {
      val someMerchant = BraintreeMerchant(
        merchantId = "some merchant ID",
        publicKey = "some public key",
        privateKey = "some private key",
        merchantAccountIds = Some(Map(
          "USD" -> "some merchant account ID",
          "EUR" -> "some other merchant account ID"
        ))
      )

      val merchantKey = merchantParser.stringify(someMerchant)
      merchantParser.parse(merchantKey) must beEqualTo(someMerchant)
    }
  }
}
