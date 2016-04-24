package com.wix.pay.braintree

trait BraintreeAuthorizationParser {
  def parse(authorizationKey: String): BraintreeAuthorization
  def stringify(authorization: BraintreeAuthorization): String
}
