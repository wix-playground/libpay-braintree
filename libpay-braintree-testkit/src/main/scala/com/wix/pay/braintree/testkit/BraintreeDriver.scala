package com.wix.pay.braintree.testkit


import com.google.api.client.util.Base64
import com.wix.hoopoe.http.testkit.EmbeddedHttpProbe
import com.wix.pay.creditcard.CreditCard
import com.wix.pay.model.CurrencyAmount
import spray.http._


class BraintreeDriver(probe: EmbeddedHttpProbe) {
  def this(port: Int) = this(new EmbeddedHttpProbe(port, EmbeddedHttpProbe.NotFoundHandler))

  private val xmlContentType = ContentType(MediaTypes.`application/xml`, HttpCharsets.`UTF-8`)

  def startProbe() {
    probe.doStart()
  }

  def stopProbe() {
    probe.doStop()
  }

  def resetProbe() {
    probe.handlers.clear()
  }

  def aCreateSaleRequestFor(merchantId: String,
                            publicKey: String,
                            privateKey: String,
                            currencyAmount: CurrencyAmount,
                            creditCard: CreditCard): CreateSaleCtx = {
    new CreateSaleCtx(merchantId, publicKey, privateKey, currencyAmount, creditCard)
  }

  abstract class Ctx(val resource: String) {
    /** Verifies that the specified HTTP Entity matches the stubbed request. */
    def isStubbedRequestEntity(entity: HttpEntity, headers: List[HttpHeader]): Boolean

    def errors(statusCode: StatusCode) {
      probe.handlers += {
        case HttpRequest(
        HttpMethods.POST,
        Uri.Path(`resource`),
        headers,
        entity,
        _) if isStubbedRequestEntity(entity, headers) => HttpResponse(status = statusCode)
      }
    }
  }

  class CreateSaleCtx(merchantId: String,
                      publicKey: String,
                      privateKey: String,
                      currencyAmount: CurrencyAmount,
                      creditCard: CreditCard) extends Ctx(s"/merchants/$merchantId/transactions") {

    def succeedsWith(transactionId: String) {
      probe.handlers += {
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
              createSuccessfulResponseXml(transactionId)))
      }
    }

    def failsWith(status: String) {
      probe.handlers += {
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
              createErrorResponseXml(status)))
      }
    }

    def failsOnInvalidCardNumber() {
      probe.handlers += {
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
              createInvalidCardNumberXml()))
      }
    }

    override def isStubbedRequestEntity(entity: HttpEntity, headers: List[HttpHeader]): Boolean = {
      isAuthorized(headers) && verifyContent(entity)
    }

    private def isAuthorized(headers: List[HttpHeader]): Boolean = {
      val expectedValue = "Basic " + Base64.encodeBase64String(s"$publicKey:$privateKey".getBytes("UTF-8"))
      for (header <- headers) {
        if (header.name == "Authorization") {
          return header.value == expectedValue
        }
      }
      false
    }

    private def createSuccessfulResponseXml(transactionId: String): String = {
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<transaction>\n  <id>" +
        transactionId +
        "</id>\n  <status>authorized</status>\n  <type>sale</type>\n  <currency-iso-code>USD</currency-iso-code>\n  <amount>1.00</amount>\n  <merchant-account-id>OpenRestLLC_instant</merchant-account-id>\n  <order-id nil=\"true\"/>\n  <created-at type=\"datetime\">2015-02-09T15:09:16Z</created-at>\n  <updated-at type=\"datetime\">2015-02-09T15:09:18Z</updated-at>\n  <customer>\n    <id nil=\"true\"/>\n    <first-name nil=\"true\"/>\n    <last-name nil=\"true\"/>\n    <company nil=\"true\"/>\n    <email nil=\"true\"/>\n    <website nil=\"true\"/>\n    <phone nil=\"true\"/>\n    <fax nil=\"true\"/>\n  </customer>\n  <billing>\n    <id nil=\"true\"/>\n    <first-name nil=\"true\"/>\n    <last-name nil=\"true\"/>\n    <company nil=\"true\"/>\n    <street-address>7 Hashomron st</street-address>\n    <extended-address nil=\"true\"/>\n    <locality nil=\"true\"/>\n    <region nil=\"true\"/>\n    <postal-code>47203</postal-code>\n    <country-name nil=\"true\"/>\n    <country-code-alpha2 nil=\"true\"/>\n    <country-code-alpha3 nil=\"true\"/>\n    <country-code-numeric nil=\"true\"/>\n  </billing>\n  <refund-id nil=\"true\"/>\n  <refund-ids type=\"array\"/>\n  <refunded-transaction-id nil=\"true\"/>\n  <settlement-batch-id nil=\"true\"/>\n  <shipping>\n    <id nil=\"true\"/>\n    <first-name nil=\"true\"/>\n    <last-name nil=\"true\"/>\n    <company nil=\"true\"/>\n    <street-address nil=\"true\"/>\n    <extended-address nil=\"true\"/>\n    <locality nil=\"true\"/>\n    <region nil=\"true\"/>\n    <postal-code nil=\"true\"/>\n    <country-name nil=\"true\"/>\n    <country-code-alpha2 nil=\"true\"/>\n    <country-code-alpha3 nil=\"true\"/>\n    <country-code-numeric nil=\"true\"/>\n  </shipping>\n  <custom-fields/>\n  <avs-error-response-code nil=\"true\"/>\n  <avs-postal-code-response-code>U</avs-postal-code-response-code>\n  <avs-street-address-response-code>U</avs-street-address-response-code>\n  <cvv-response-code>M</cvv-response-code>\n  <gateway-rejection-reason nil=\"true\"/>\n  <processor-authorization-code>040041</processor-authorization-code>\n  <processor-response-code>1000</processor-response-code>\n  <processor-response-text>Approved</processor-response-text>\n  <additional-processor-response nil=\"true\"/>\n  <voice-referral-number nil=\"true\"/>\n  <purchase-order-number nil=\"true\"/>\n  <tax-amount nil=\"true\"/>\n  <tax-exempt type=\"boolean\">false</tax-exempt>\n  <credit-card>\n    <token nil=\"true\"/>\n    <bin>458010</bin>\n    <last-4>0890</last-4>\n    <card-type>Visa</card-type>\n    <expiration-month>12</expiration-month>\n    <expiration-year>2016</expiration-year>\n    <customer-location>US</customer-location>\n    <cardholder-name>John Doe</cardholder-name>\n    <image-url>https://assets.braintreegateway.com/payment_method_logo/visa.png?environment=production</image-url>\n    <unique-number-identifier nil=\"true\"/>\n    <prepaid>No</prepaid>\n    <healthcare>No</healthcare>\n    <debit>No</debit>\n    <durbin-regulated>No</durbin-regulated>\n    <commercial>No</commercial>\n    <payroll>No</payroll>\n    <issuing-bank>Bank Leumi Le-Israel B.M.</issuing-bank>\n    <country-of-issuance>ISR</country-of-issuance>\n    <product-id>G</product-id>\n    <venmo-sdk type=\"boolean\">false</venmo-sdk>\n  </credit-card>\n  <status-history type=\"array\">\n    <status-event>\n      <timestamp type=\"datetime\">2015-02-09T15:09:18Z</timestamp>\n      <status>authorized</status>\n      <amount>1.00</amount>\n      <user>dleshem</user>\n      <transaction-source>api</transaction-source>\n    </status-event>\n  </status-history>\n  <plan-id nil=\"true\"/>\n  <subscription-id nil=\"true\"/>\n  <subscription>\n    <billing-period-end-date nil=\"true\"/>\n    <billing-period-start-date nil=\"true\"/>\n  </subscription>\n  <add-ons type=\"array\"/>\n  <discounts type=\"array\"/>\n  <descriptor>\n    <name nil=\"true\"/>\n    <phone nil=\"true\"/>\n    <url nil=\"true\"/>\n  </descriptor>\n  <recurring type=\"boolean\">false</recurring>\n  <channel nil=\"true\"/>\n  <service-fee-amount nil=\"true\"/>\n  <escrow-status nil=\"true\"/>\n  <disbursement-details>\n    <disbursement-date nil=\"true\"/>\n    <settlement-amount nil=\"true\"/>\n    <settlement-currency-iso-code nil=\"true\"/>\n    <settlement-currency-exchange-rate nil=\"true\"/>\n    <funds-held nil=\"true\"/>\n    <success nil=\"true\"/>\n  </disbursement-details>\n  <disputes type=\"array\"/>\n  <payment-instrument-type>credit_card</payment-instrument-type>\n  <processor-settlement-response-code></processor-settlement-response-code>\n  <processor-settlement-response-text></processor-settlement-response-text>\n</transaction>"
    }

    private def createInvalidCardNumberXml(): String = {
      """<?xml version="1.0" encoding="UTF-8"?>
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
          </errors>
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
          <message>Merchant account does not support payment instrument.
        Credit card type is not accepted by this merchant account.</message>
        </api-error-response>"""
    }

    private def createErrorResponseXml(status: String): String = {
      s"""<?xml version="1.0" encoding="UTF-8"?>
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
            <id>mj4m3cm</id>
            <status>$status</status>
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
        </api-error-response>"""
    }

    private def verifyContent(entity: HttpEntity): Boolean = {
      // TODO: verify creditCard, currencyAmount
      true
    }
  }
}
