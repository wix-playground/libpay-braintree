package com.wix.pay.braintree

trait BraintreeMerchantParser {
  def parse(merchantKey: String): BraintreeMerchant
  def stringify(merchant: BraintreeMerchant): String
}
