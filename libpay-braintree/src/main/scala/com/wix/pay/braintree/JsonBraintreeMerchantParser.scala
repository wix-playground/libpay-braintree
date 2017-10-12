package com.wix.pay.braintree

import org.json4s.ShortTypeHints
import org.json4s.native.Serialization

class JsonBraintreeMerchantParser() extends BraintreeMerchantParser {
  implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[BraintreeMerchant], classOf[BraintreeOAuthMerchant])))

  override def parse(merchantKey: String): BraintreeMerchantBase = {
    Serialization.read[BraintreeMerchantBase](merchantKey)
  }

  override def stringify(merchant: BraintreeMerchantBase): String = {
    Serialization.write(merchant)
  }
}
