package camel.sample;

import java.util.Arrays;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.loadbalancer.CircuitBreakerLoadBalancer;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class CircuitBreakerCamelTest extends CamelTestSupport {

	@EndpointInject(uri = "mock:response")
	public MockEndpoint resultEndpoint;

	@Produce(uri = "direct:input")
	public ProducerTemplate testProducer;

	int threshold = 2;
	String stdResponse = "arrived";

	@Test
	public void testRoute2() throws InterruptedException {

		int sentMessageCount = 5;

		resultEndpoint.expectedMessageCount(threshold);

		resultEndpoint.whenAnyExchangeReceived((ex) -> {
			ex.getIn().setBody(stdResponse);
			ex.setException(new Exception("test"));
		});

		for (int i = 0; i < sentMessageCount + threshold; i++) {
			Exchange ex = sendMessage("direct:input", i);
			String body = ex.getIn().getBody(String.class);
			if (i < threshold) {
				assertEquals(stdResponse, body);
			} else {
				assertEquals(new Integer(i).toString(), body);
			}
		}
		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {

				final CircuitBreakerLoadBalancer circuitBreaker = new CircuitBreakerLoadBalancer(
						Arrays.asList(Exception.class));
				circuitBreaker.setHalfOpenAfter(1000);
				circuitBreaker.setThreshold(threshold);

				from("direct:input")
					.routeId("testRoute")
					.convertBodyTo(String.class)
					.loadBalance(circuitBreaker)
					.to("direct:output")
				.end();
			}
		};
	}

	private Exchange sendMessage(final String endpoint, final Object body) {
		return template.send(endpoint, exchange -> {
			exchange.getIn().setBody(body);
		});
	}

	@Before
	public void mockEndpoints() throws Exception {
		AdviceWithRouteBuilder mock = new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("direct:output").skipSendToOriginalEndpoint().to("mock:response");
			}
		};
		context.getRouteDefinition("testRoute").adviceWith(context, mock);
	}
}
