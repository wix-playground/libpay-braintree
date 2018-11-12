package com.wix.pay.braintree.testkit


import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import com.google.api.client.util.Base64
import com.wix.e2e.http.api.StubWebServer
import com.wix.e2e.http.server.WebServerFactory._
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.model.CurrencyAmount

import scala.xml.NodeSeq


class BraintreeDriver(server: StubWebServer) {
  def this(port: Int) = this(aStubWebServer.onPort(port).build)

  private val xmlContentType = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`)

  def start(): Unit = server.start()

  def stop(): Unit = server.stop()

  def reset(): Unit = server.replaceWith()


  def aCreateSaleRequestFor(merchantId: String,
                            publicKey: String,
                            privateKey: String,
                            currencyAmount: CurrencyAmount,
                            creditCard: CreditCard): CreateSaleCtx = {
    new CreateSaleCtx(merchantId, publicKey, privateKey, Some(currencyAmount), Some(creditCard))
  }

  def anySaleRequestFor(merchantId: String, publicKey: String, privateKey: String): CreateSaleCtx = {
    new CreateSaleCtx(merchantId, publicKey, privateKey, None, None)
  }

  abstract class Ctx(val resource: String) {
    /** Verifies that the specified HTTP Entity matches the stubbed request. */
    def isStubbedRequestEntity(entity: HttpEntity, headers: Seq[HttpHeader]): Boolean

    def errors(statusCode: StatusCode) {
      server.appendAll {
        case HttpRequest(
        HttpMethods.POST,
        Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) => HttpResponse(status = statusCode)
      }
    }
  }

  class CreateSaleCtx(merchantId: String,
                      publicKey: String,
                      privateKey: String,
                      currencyAmount: Option[CurrencyAmount],
                      creditCard: Option[CreditCard]) extends Ctx(s"/merchants/$merchantId/transactions") {

    def succeedsWith(transactionId: String) {
      server.appendAll {
        case HttpRequest(
        HttpMethods.POST,
        Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(
              xmlContentType,
              createSuccessfulResponseXml(transactionId)))
      }
    }

    def failsWith(status: String) {
      server.appendAll {
        case HttpRequest(
        HttpMethods.POST,
        Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(
              xmlContentType,
              createErrorResponseXml(status)))
      }
    }

    def failsOnInvalidCardNumber(transactionId: Option[String] = None, status: Option[String] = None) {
      server.appendAll {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) =>
          HttpResponse(
            status = StatusCodes.OK,
            entity = HttpEntity(
              xmlContentType,
              createInvalidCardNumberXml(transactionId, status)))
      }
    }

    override def isStubbedRequestEntity(entity: HttpEntity, headers: Seq[HttpHeader]): Boolean = {
      isAuthorized(headers) && verifyContent(entity)
    }

    private def isAuthorized(headers: Seq[HttpHeader]): Boolean = {
      val expectedValue = "Basic " + Base64.encodeBase64String(s"$publicKey:$privateKey".getBytes("UTF-8"))
      for (header <- headers) {
        if (header.name == "Authorization") {
          return header.value == expectedValue
        }
      }

      false
    }

    private def createSuccessfulResponseXml(transactionId: String): String = {
      <transaction>
        <id>
          {transactionId}
        </id>
        <status>authorized</status>
        <type>sale</type>
        <currency-iso-code>USD</currency-iso-code>
        <amount>1.00</amount>
        <merchant-account-id>OpenRestLLC_instant</merchant-account-id>
        <order-id nil="true"/>
        <created-at type="datetime">2015-02-09T15:09:16Z</created-at>
        <updated-at type="datetime">2015-02-09T15:09:18Z</updated-at>
        <customer>
          <id nil="true"/>
          <first-name nil="true"/>
          <last-name nil="true"/>
          <company nil="true"/>
          <email nil="true"/>
          <website nil="true"/>
          <phone nil="true"/>
          <fax nil="true"/>
        </customer>
        <billing>
          <id nil="true"/>
          <first-name nil="true"/>
          <last-name nil="true"/>
          <company nil="true"/>
          \
          <street-address>7 Hashomron st</street-address>
          <extended-address nil="true"/>
          <locality nil="true"/>
          <region nil="true"/>
          <postal-code>47203</postal-code>
          <country-name nil="true"/>
          <country-code-alpha2 nil="true"/>
          <country-code-alpha3 nil="true"/>
          <country-code-numeric nil="true"/>
        </billing>
        <refund-id nil="true"/>
        <refund-ids type="array"/>
        <refunded-transaction-id nil="true"/>
        <settlement-batch-id nil="true"/>
        <shipping>
          <id nil="true"/>
          <first-name nil="true"/>
          <last-name nil="true"/>
          <company nil="true"/>
          <street-address nil="true"/>
          <extended-address nil="true"/>
          <locality nil="true"/>
          <region nil="true"/>
          <postal-code nil="true"/>
          <country-name nil="true"/>
          <country-code-alpha2 nil="true"/>
          <country-code-alpha3 nil="true"/>
          <country-code-numeric nil="true"/>
        </shipping>
        <custom-fields/>
        <avs-error-response-code nil="true"/>
        <avs-postal-code-response-code>U</avs-postal-code-response-code>
        <avs-street-address-response-code>U</avs-street-address-response-code>
        <cvv-response-code>M</cvv-response-code>
        <gateway-rejection-reason nil="true"/>
        <processor-authorization-code>040041</processor-authorization-code>
        <processor-response-code>1000</processor-response-code>
        <processor-response-text>Approved</processor-response-text>
        <additional-processor-response nil="true"/>
        <voice-referral-number nil="true"/>
        <purchase-order-number nil="true"/>
        <tax-amount nil="true"/>
        <tax-exempt type="boolean">false</tax-exempt>
        <credit-card>
          <token nil="true"/>
          <bin>458010</bin>
          <last-4>0890</last-4>
          <card-type>Visa</card-type>
          <expiration-month>12</expiration-month>
          <expiration-year>2016</expiration-year>
          <customer-location>US</customer-location>
          <cardholder-name>John Doe</cardholder-name>
          <image-url>https://assets.braintreegateway.com/payment_method_logo/visa.png?environment=production</image-url>
          <unique-number-identifier nil="true"/>
          <prepaid>No</prepaid>
          <healthcare>No</healthcare>
          <debit>No</debit>
          <durbin-regulated>No</durbin-regulated>
          <commercial>No</commercial>
          <payroll>No</payroll>
          <issuing-bank>Bank Leumi Le-Israel B.M.</issuing-bank>
          <country-of-issuance>ISR</country-of-issuance>
          <product-id>G</product-id>
          <venmo-sdk type="boolean">false</venmo-sdk>
        </credit-card>
        <status-history type="array">
          <status-event>
            <timestamp type="datetime">2015-02-09T15:09:18Z</timestamp>
            <status>authorized</status>
            <amount>1.00</amount>
            <user>dleshem</user>
            <transaction-source>api</transaction-source>
          </status-event>
        </status-history>
        <plan-id nil="true"/>
        <subscription-id nil="true"/>
        <subscription>
          <billing-period-end-date nil="true"/>
          <billing-period-start-date nil="true"/>
        </subscription>
        <add-ons type="array"/>
        <discounts type="array"/>
        <descriptor>
          <name nil="true"/>
          <phone nil="true"/>
          <url nil="true"/>
        </descriptor>
        <recurring type="boolean">false</recurring>
        <channel nil="true"/>
        <service-fee-amount nil="true"/>
        <escrow-status nil="true"/>
        <disbursement-details>
          <disbursement-date nil="true"/>
          <settlement-amount nil="true"/>
          <settlement-currency-iso-code nil="true"/>
          <settlement-currency-exchange-rate nil="true"/>
          <funds-held nil="true"/>
          <success nil="true"/>
        </disbursement-details>
        <disputes type="array"/>
        <payment-instrument-type>credit_card</payment-instrument-type>
        <processor-settlement-response-code></processor-settlement-response-code>
        <processor-settlement-response-text></processor-settlement-response-text>
      </transaction>.toString
    }

    private def createInvalidCardNumberXml(transactionId: Option[String], status: Option[String]): String = {
      <api-error-response>
        <errors>
          <errors type="array"/>
          <transaction>
            <errors type="array">
              <error>
                <code>91577</code>
                <attribute type="symbol">merchant_account_id</attribute>
                <message>Merchant account does not support payment instrument.</message>
              </error>
            </errors>
            <credit-card>
              <errors type="array">
                <error>
                  <code>81703</code>
                  <attribute type="symbol">number</attribute>
                  <message>Credit card type is not accepted by this merchant account.</message>
                </error>
              </errors>
            </credit-card>
          </transaction>
        </errors>{transactionId.map(x =>
        <transaction>
          <id>
            {x}
          </id>
          <amount>100.0</amount>
          <customer></customer>
          <disbursement-details></disbursement-details>
          <descriptor></descriptor>
          <shipping></shipping>
          <subscription></subscription>
          <merchant-account-id>SOME_MERCHANT_ACCOUNT_ID</merchant-account-id>
          <status>{status.getOrElse("PROCESSOR_DECLINED")}</status>
          <processor-response-code>SomeCode</processor-response-code>
          <processor-response-text>SomeText</processor-response-text>
          <credit-card>
            <expiration-month>05</expiration-month>
            <expiration-year>2016</expiration-year>
          </credit-card>
          <billing nil="true"/>
          <options>
            <submit-for-settlement>true</submit-for-settlement>
          </options>
          <type>sale</type>
        </transaction>
      ).getOrElse(NodeSeq.Empty)}
        <params>
          <transaction>
            <amount>100.0</amount>
            <merchant-account-id>SOME_MERCHANT_ACCOUNT_ID</merchant-account-id>
            <credit-card>
              <expiration-month>05</expiration-month>
              <expiration-year>2016</expiration-year>
            </credit-card>
            <billing nil="true"/>
            <options>
              <submit-for-settlement>true</submit-for-settlement>
            </options>
            <type>sale</type>
          </transaction>
        </params>
        <message>Merchant account does not support payment instrument. Credit card type is not accepted by this merchant account.</message>
      </api-error-response>.toString
    }

    private def createErrorResponseXml(status: String, transactionId: Option[String] = None): String = {
      <api-error-response>
        <errors>
          <errors type="array"/>
        </errors>
        <params>
          <transaction>
            <amount>33.3</amount>
            <merchant-account-id>OpenRestLLC_instant</merchant-account-id>
            <credit-card>
              <cardholder-name>some holder name</cardholder-name>
              <expiration-month>12</expiration-month>
              <expiration-year>2020</expiration-year>
            </credit-card>
            <billing>
              <postal-code>90210</postal-code>
              <street-address>some billing address</street-address>
            </billing>
            <options>
              <submit-for-settlement>false</submit-for-settlement>
            </options>
            <type>sale</type>
          </transaction>
        </params>
        <message>Processor Declined</message>
        <transaction>
          <id>
            {transactionId.getOrElse("mj4m3cm")}
          </id>
          <status>
            {status}
          </status>
          <type>sale</type>
          <currency-iso-code>USD</currency-iso-code>
          <amount>33.30</amount>
          <merchant-account-id>OpenRestLLC_instant</merchant-account-id>
          <order-id nil="true"/>
          <created-at type="datetime">2015-11-05T16:17:54Z</created-at>
          <updated-at type="datetime">2015-11-05T16:17:54Z</updated-at>
          <customer>
            <id nil="true"/>
            <first-name nil="true"/>
            <last-name nil="true"/>
            <company nil="true"/>
            <email nil="true"/>
            <website nil="true"/>
            <phone nil="true"/>
            <fax nil="true"/>
          </customer>
          <billing>
            <id nil="true"/>
            <first-name nil="true"/>
            <last-name nil="true"/>
            <company nil="true"/>
            <street-address>some billing address</street-address>
            <extended-address nil="true"/>
            <locality nil="true"/>
            <region nil="true"/>
            <postal-code>90210</postal-code>
            <country-name nil="true"/>
            <country-code-alpha2 nil="true"/>
            <country-code-alpha3 nil="true"/>
            <country-code-numeric nil="true"/>
          </billing>
          <refund-id nil="true"/>
          <refund-ids type="array"/>
          <refunded-transaction-id nil="true"/>
          <partial-settlement-transaction-ids type="array"/>
          <authorized-transaction-id nil="true"/>
          <settlement-batch-id nil="true"/>
          <shipping>
            <id nil="true"/>
            <first-name nil="true"/>
            <last-name nil="true"/>
            <company nil="true"/>
            <street-address nil="true"/>
            <extended-address nil="true"/>
            <locality nil="true"/>
            <region nil="true"/>
            <postal-code nil="true"/>
            <country-name nil="true"/>
            <country-code-alpha2 nil="true"/>
            <country-code-alpha3 nil="true"/>
            <country-code-numeric nil="true"/>
          </shipping>
          <custom-fields/>
          <avs-error-response-code nil="true"/>
          <avs-postal-code-response-code>U</avs-postal-code-response-code>
          <avs-street-address-response-code>U</avs-street-address-response-code>
          <cvv-response-code>U</cvv-response-code>
          <gateway-rejection-reason nil="true"/>
          <processor-authorization-code nil="true"/>
          <processor-response-code>2038</processor-response-code>
          <processor-response-text>Processor Declined</processor-response-text>
          <additional-processor-response>63 : SERV NOT ALLOWED</additional-processor-response>
          <voice-referral-number nil="true"/>
          <purchase-order-number nil="true"/>
          <tax-amount nil="true"/>
          <tax-exempt type="boolean">false</tax-exempt>
          <credit-card>
            <token nil="true"/>
            <bin>458045</bin>
            <last-4>4580</last-4>
            <card-type>Visa</card-type>
            <expiration-month>12</expiration-month>
            <expiration-year>2020</expiration-year>
            <customer-location>US</customer-location>
            <cardholder-name>some holder name</cardholder-name>
            <image-url>https://assets.braintreegateway.com/payment_method_logo/visa.png?environment=production</image-url>
            <prepaid>Unknown</prepaid>
            <healthcare>Unknown</healthcare>
            <debit>Unknown</debit>
            <durbin-regulated>Unknown</durbin-regulated>
            <commercial>Unknown</commercial>
            <payroll>Unknown</payroll>
            <issuing-bank>Unknown</issuing-bank>
            <country-of-issuance>Unknown</country-of-issuance>
            <product-id>Unknown</product-id>
            <unique-number-identifier nil="true"/>
            <venmo-sdk type="boolean">false</venmo-sdk>
          </credit-card>
          <status-history type="array">
            <status-event>
              <timestamp type="datetime">2015-11-05T16:17:54Z</timestamp>
              <status>processor_declined</status>
              <amount>33.30</amount>
              <user>dleshem</user>
              <transaction-source>api</transaction-source>
            </status-event>
          </status-history>
          <plan-id nil="true"/>
          <subscription-id nil="true"/>
          <subscription>
            <billing-period-end-date nil="true"/>
            <billing-period-start-date nil="true"/>
          </subscription>
          <add-ons type="array"/>
          <discounts type="array"/>
          <descriptor>
            <name nil="true"/>
            <phone nil="true"/>
            <url nil="true"/>
          </descriptor>
          <recurring type="boolean">false</recurring>
          <channel nil="true"/>
          <service-fee-amount nil="true"/>
          <escrow-status nil="true"/>
          <disbursement-details>
            <disbursement-date nil="true"/>
            <settlement-amount nil="true"/>
            <settlement-currency-iso-code nil="true"/>
            <settlement-currency-exchange-rate nil="true"/>
            <funds-held nil="true"/>
            <success nil="true"/>
          </disbursement-details>
          <disputes type="array"/>
          <payment-instrument-type>credit_card</payment-instrument-type>
          <processor-settlement-response-code></processor-settlement-response-code>
          <processor-settlement-response-text></processor-settlement-response-text>
          <three-d-secure-info nil="true"/>
        </transaction>
      </api-error-response>.toString
    }

    private def verifyContent(entity: HttpEntity): Boolean = {
      // TODO: verify creditCard, currencyAmount
      true
    }
  }

}
