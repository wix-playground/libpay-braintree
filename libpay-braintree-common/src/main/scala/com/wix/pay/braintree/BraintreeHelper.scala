package com.wix.pay.braintree

import com.braintreegateway.TransactionRequest
import com.wix.pay.creditcard.CreditCard

object BraintreeHelper {
  def createTransactionRequest(merchantAccountId: Option[String],
                               creditCard: CreditCard,
                               amount: Double,
                               channel: Option[String] = None,
                               submitForSettlement: Boolean): TransactionRequest = {
    val request = new TransactionRequest().
      amount(toBraintreeAmount(amount))

    merchantAccountId foreach request.merchantAccountId

    channel foreach request.channel

    val creditCardRequest = request.creditCard()
    val billingAddress = request.billingAddress()

    creditCardRequest.number(creditCard.number)
    creditCardRequest.expirationYear(f"${creditCard.expiration.year}%04d")
    creditCardRequest.expirationMonth(f"${creditCard.expiration.month}%02d")
    creditCard.holderName foreach creditCardRequest.cardholderName
    creditCard.csc foreach creditCardRequest.cvv
    creditCard.billingAddress foreach billingAddress.streetAddress
    creditCard.billingPostalCode foreach billingAddress.postalCode

    val options = request.options()
    options.submitForSettlement(submitForSettlement)

    request
  }

  def toBraintreeAmount(amount: Double): java.math.BigDecimal = {
    java.math.BigDecimal.valueOf(amount)
  }
}
