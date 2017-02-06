package com.wix.pay.braintree


import com.braintreegateway._
import com.braintreegateway.exceptions.BraintreeException
import com.wix.pay.braintree.model.ErrorAttributes
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.model.{CurrencyAmount, Customer, Deal, Payment}
import com.wix.pay.{PaymentErrorException, PaymentException, PaymentGateway, PaymentRejectedException}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class BraintreeGateway(merchantParser: BraintreeMerchantParser = new JsonBraintreeMerchantParser,
                       authorizationParser: BraintreeAuthorizationParser = new JsonBraintreeAuthorizationParser,
                       environment: Environment,
                       channel: Option[String] = None) extends PaymentGateway {

  private def createTransaction(merchantKey: String,
                                creditCard: CreditCard,
                                currencyAmount: CurrencyAmount,
                                submitForSettlement: Boolean): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)

      val braintree = new com.braintreegateway.BraintreeGateway(
        environment, merchant.merchantId, merchant.publicKey, merchant.privateKey)
      val merchantAccountId = merchant.merchantAccountIds(currencyAmount.currency)

      // Transaction
      val request = BraintreeHelper.createTransactionRequest(merchantAccountId, creditCard, currencyAmount.amount, channel, submitForSettlement)

      // Execute
      val result = braintree.transaction.sale(request)
      if (result.isSuccess) {
        result.getTarget.getId
      } else { // Error
        Option(result.getTransaction) match {
          case Some(transaction) => // Transaction error
            transaction.getStatus match {
              case Transaction.Status.PROCESSOR_DECLINED =>
                throw PaymentRejectedException(s"${transaction.getStatus}|${transaction.getProcessorResponseCode}|${transaction.getProcessorResponseText}")
              case _ =>
                throw PaymentErrorException(s"${transaction.getStatus}|${transaction.getProcessorResponseCode}|${transaction.getProcessorResponseText}")
            }
          case None => // Validation error
            handleUnsuccessfulResult(result)
        }
      }
    } match {
      case Success(authorizationKey) => Success(authorizationKey)
      case Failure(e: BraintreeException) => Failure(new PaymentErrorException(message = e.getMessage, cause = e))
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(PaymentErrorException(e.getMessage, e))
    }
  }

  override def authorize(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    require(payment.installments == 1, "Braintree does not support installments")
    createTransaction(merchantKey, creditCard, payment.currencyAmount, submitForSettlement = false) match {
      case Success(transactionId) => Success(authorizationParser.stringify(BraintreeAuthorization(transactionId)))
      case Failure(e) => Failure(e)
    }
  }

  override def capture(merchantKey: String, authorizationKey: String, amount: Double): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      val braintree = new com.braintreegateway.BraintreeGateway(
        environment, merchant.merchantId, merchant.publicKey, merchant.privateKey)

      val result = braintree.transaction.submitForSettlement(authorization.transactionId, BraintreeHelper.toBraintreeAmount(amount))
      if (result.isSuccess) {
        result.getTarget.getId
      } else {
        handleUnsuccessfulResult(result)
      }
    } match {
      case Success(transactionId) => Success(transactionId)
      case Failure(e: BraintreeException) => Failure(new PaymentErrorException(message = e.getMessage, cause = e))
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(PaymentErrorException(e.getMessage, e))
    }
  }

  private def handleUnsuccessfulResult(result: Result[Transaction]): String = {
    val error = result.getErrors.getAllDeepValidationErrors.head
    (error.getAttribute, error.getCode) match {
      case (ErrorAttributes.merchantAccountId, ValidationErrorCode.TRANSACTION_PAYMENT_INSTRUMENT_NOT_SUPPORTED_BY_MERCHANT_ACCOUNT) =>
        throw PaymentRejectedException(s"${error.getAttribute}|${error.getCode}|${error.getMessage}")
      case _ =>
        throw PaymentErrorException(s"${error.getAttribute}|${error.getCode}|${error.getMessage}")
    }
  }

  override def sale(merchantKey: String, creditCard: CreditCard, payment: Payment, customer: Option[Customer], deal: Option[Deal]): Try[String] = {
    require(payment.installments == 1, "Braintree does not support installments")
    createTransaction(merchantKey, creditCard, payment.currencyAmount, submitForSettlement = true)
  }

  override def voidAuthorization(merchantKey: String, authorizationKey: String): Try[String] = {
    Try {
      val merchant = merchantParser.parse(merchantKey)
      val authorization = authorizationParser.parse(authorizationKey)

      val braintree = new com.braintreegateway.BraintreeGateway(
        environment, merchant.merchantId, merchant.publicKey, merchant.privateKey)

      val result = braintree.transaction.voidTransaction(authorization.transactionId)
      if (result.isSuccess) {
        result.getTarget.getId
      } else {
        handleUnsuccessfulResult(result)
      }
    } match {
      case Success(transactionId) => Success(transactionId)
      case Failure(e: BraintreeException) => Failure(new PaymentErrorException(message = e.getMessage, cause = e))
      case Failure(e: PaymentException) => Failure(e)
      case Failure(e) => Failure(new PaymentErrorException(e.getMessage, e))
    }
  }
}
