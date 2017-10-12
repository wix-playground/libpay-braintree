package com.wix.pay.braintree.testkit

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ClasspathFileSource
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader


class BraintreeOAuthDriver {

  val serverFilePath = new ClasspathFileSource("wiremock")
  val wireMockServer = new WireMockServer(3000, serverFilePath, false)
  wireMockServer.loadMappingsUsing(new JsonFileMappingsLoader(serverFilePath))

  def start() = wireMockServer.start()

  def stop() = wireMockServer.stop()


}

object BraintreeOAuthDriver {
  val validMerchantToken = "access_token$development$90210$399d9b904522704d1e8c9f848020cc8e"
  val expectedTransactionId = "dcbjzfcy"
  val passingCreditCardNumber = "5555555555554444"
}