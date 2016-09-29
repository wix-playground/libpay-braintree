package com.wix.pay.braintree

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonBraintreeOAuthMerchantParser() extends BraintreeMerchantParser {
  implicit val formats = DefaultFormats

  override def parse(merchantKey: String): BraintreeOAuthMerchant = {
    Serialization.read[BraintreeOAuthMerchant](merchantKey)
  }

  override def stringify(merchant: BraintreeMerchantBase): String = {
    Serialization.write(merchant)
  }
}
