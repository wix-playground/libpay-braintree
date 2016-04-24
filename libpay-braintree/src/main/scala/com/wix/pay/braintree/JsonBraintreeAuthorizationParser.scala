package com.wix.pay.braintree

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

class JsonBraintreeAuthorizationParser() extends BraintreeAuthorizationParser {
  implicit val formats = DefaultFormats

  override def parse(authorizationKey: String): BraintreeAuthorization = {
    Serialization.read[BraintreeAuthorization](authorizationKey)
  }

  override def stringify(authorization: BraintreeAuthorization): String = {
    Serialization.write(authorization)
  }
}
