package com.wix.pay.braintree

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonBraintreeMerchantParser() extends BraintreeMerchantParser {
  implicit val formats = DefaultFormats

  override def parse(merchantKey: String): BraintreeMerchant = {
    Serialization.read[BraintreeMerchant](merchantKey)
  }

  override def stringify(merchant: BraintreeMerchant): String = {
    Serialization.write(merchant)
  }
}
