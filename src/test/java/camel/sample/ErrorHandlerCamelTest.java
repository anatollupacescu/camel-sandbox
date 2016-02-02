package camel.sample;

import org.apache.camel.EndpointInject;
import org.apache.camel.ErrorHandlerFactory;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class ErrorHandlerCamelTest extends CamelTestSupport {

	@EndpointInject(uri = "mock:response")
	public MockEndpoint resultEndpoint;

	@Produce(uri = "direct:input")
	public ProducerTemplate testProducer;

	int redeliveryCount = 3;

	@Test
	public void testRoute3() throws InterruptedException {

		final int m_count = 5;

		resultEndpoint.expectedMessageCount(redeliveryCount + (redeliveryCount * m_count));

		resultEndpoint.whenAnyExchangeReceived((ex) -> {
			ex.getIn().setBody("arrived");
			ex.setException(new Exception("test"));
		});

		for (int i = 0; i < m_count; i++) {
			Exchange ex = sendMessage("direct:input", i);
			assertEquals("arrived", ex.getIn().getBody(String.class));
		}

		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {

				final ErrorHandlerFactory errorHandler = defaultErrorHandler().maximumRedeliveries(redeliveryCount)
						.redeliveryDelay(1000).backOffMultiplier(1).retryAttemptedLogLevel(LoggingLevel.WARN)
						.retriesExhaustedLogLevel(LoggingLevel.ERROR);

				from("direct:input").routeId("testRoute").errorHandler(errorHandler).to("direct:output").end();
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
