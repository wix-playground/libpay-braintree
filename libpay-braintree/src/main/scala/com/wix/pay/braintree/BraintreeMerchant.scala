package com.wix.pay.braintree

/**
 * @param merchantAccountIds  Maps currency (ISO 4217) to merchant account id. on absence default is used (can be set in Braintree account)
 */
class BraintreeMerchantBase(val merchantAccountIds: Option[Map[String, String]])

case class BraintreeMerchant(merchantId: String,
                             publicKey: String,
                             privateKey: String,
                             override val merchantAccountIds: Option[Map[String, String]]) extends BraintreeMerchantBase(merchantAccountIds)

case class BraintreeOAuthMerchant(token: String,
                                  override val merchantAccountIds: Option[Map[String, String]] = None
                                  ) extends BraintreeMerchantBase(merchantAccountIds)