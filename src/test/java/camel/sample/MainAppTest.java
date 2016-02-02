package camel.sample;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

public class MainAppTest extends CamelTestSupport {

	@EndpointInject(uri = "mock:response")
	public MockEndpoint resultEndpoint;

	@Test
	public void test1() throws InterruptedException {
		resultEndpoint.expectedMessageCount(1);
		context.createProducerTemplate().send(FileProcessingRouteBuilder.DIRECT_PROCESS_FILE, exchange -> {
			exchange.getIn().setBody("{}");
			exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
		});
		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new FileProcessingRouteBuilder();
	}

	@Before
	public void mockEndpoints() throws Exception {
		AdviceWithRouteBuilder mock = new AdviceWithRouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("file:*").skipSendToOriginalEndpoint().to("mock:response");
			}
		};
		context.getRouteDefinition("defaultRoute").adviceWith(context, mock);
	}
}
