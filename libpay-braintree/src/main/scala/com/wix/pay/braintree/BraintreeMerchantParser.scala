package com.wix.pay.braintree

trait BraintreeMerchantParser {
  def parse(merchantKey: String): BraintreeMerchantBase
  def stringify(merchant: BraintreeMerchantBase): String
}
