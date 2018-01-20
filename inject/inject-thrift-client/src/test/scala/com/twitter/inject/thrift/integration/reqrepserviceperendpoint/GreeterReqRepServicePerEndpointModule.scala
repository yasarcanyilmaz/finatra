package com.twitter.inject.thrift.integration.reqrepserviceperendpoint

import com.google.inject.Module
import com.twitter.finagle.service.{ReqRep, ResponseClass, ResponseClassifier}
import com.twitter.greeter.thriftscala.{Greeter, InvalidOperation}
import com.twitter.inject.thrift.ThriftMethodBuilderFactory
import com.twitter.inject.thrift.integration.filters.{HiLoggingTypeAgnosticFilter, MethodLoggingTypeAgnosticFilter}
import com.twitter.inject.thrift.modules.{ServicePerEndpointModule, ThriftClientIdModule}
import com.twitter.util.tunable.Tunable
import com.twitter.util.{Duration, Return, Throw}
import scala.util.control.NonFatal

class GreeterReqRepServicePerEndpointModule(
  requestHeaderKey: String,
  timeoutPerRequestTunable: Tunable[Duration]
) extends ServicePerEndpointModule[Greeter.ReqRepServicePerEndpoint, Greeter.MethodPerEndpoint] {

  override val modules: Seq[Module] = Seq(ThriftClientIdModule)

  override val dest = "flag!greeter-thrift-service"
  override val label = "greeter-thrift-client"

  override protected def configureServicePerEndpoint(
    builder: ThriftMethodBuilderFactory[Greeter.ReqRepServicePerEndpoint],
    servicePerEndpoint: Greeter.ReqRepServicePerEndpoint
  ): Greeter.ReqRepServicePerEndpoint = {
    servicePerEndpoint
      .withHi(
        builder
          .method(Greeter.Hi)
          .withTimeoutPerRequest(timeoutPerRequestTunable)
          // method type-agnostic filter
          .withAgnosticFilter(new HiLoggingTypeAgnosticFilter)
          // method type-specific filter
          .filtered(new HiHeadersFilter(requestHeaderKey))
          .withRetryForClassifier(PossiblyRetryableExceptions)
          .service
      )
      .withHello(
        builder
          .method(Greeter.Hello)
          // method type-specific filter
          .filtered(new HelloHeadersFilter(requestHeaderKey))
          // method type-specific filter
          .filtered[HelloFilter]
          .withRetryForClassifier(ByeResponseClassification)
          .service
      )
      .withBye(
        builder
          .method(Greeter.Bye)
          // method type-specific filter
          .filtered(new ByeHeadersFilter(requestHeaderKey))
          // method type-specific filter
          .filtered[ByeFilter]
          .withRetryForClassifier(PossiblyRetryableExceptions)
          .service
      )
      // global filter
      .filtered(new MethodLoggingTypeAgnosticFilter())
  }

  private[this] val ByeResponseClassification: ResponseClassifier =
    ResponseClassifier.named("ByeMethodCustomResponseClassification") {
      case ReqRep(_, Return(result)) if result == "ERROR" => ResponseClass.RetryableFailure
      case ReqRep(_, Return(_)) => ResponseClass.Success
      case ReqRep(_, Throw(InvalidOperation(_))) => ResponseClass.RetryableFailure
      case ReqRep(_, Throw(NonFatal(_))) => ResponseClass.RetryableFailure
      case ReqRep(_, Throw(_)) => ResponseClass.NonRetryableFailure
    }
}
