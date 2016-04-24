package com.wix.pay.braintree

/**
 * @param merchantAccountIds  Maps currency (ISO 4217) to merchant account id
 */
case class BraintreeMerchant(merchantId: String, publicKey: String, privateKey: String, merchantAccountIds: Map[String, String])
