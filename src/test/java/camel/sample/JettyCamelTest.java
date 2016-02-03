package camel.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * Unit test for using http client SO timeout
 *
 * @version
 */
public class JettyCamelTest extends BaseJettyTest {

	@Test
	public void testSendWithSOTimeoutTimeout() throws Exception {
		MockEndpoint mock = getMockEndpoint("mock:result");
		mock.expectedMessageCount(1);
		String out = template.requestBody("jetty:http://0.0.0.0:{{port}}/myservice", null, String.class);
		assertEquals("Bye World", out);
		assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				errorHandler(defaultErrorHandler().maximumRedeliveries(3));

				onException(IllegalStateException.class)
					.handled(true)
					.maximumRedeliveries(2)
					.to("log:camel.sample?level=ERROR&showCaughtException=true&showStackTrace=true&multiline=true")
					.log(LoggingLevel.INFO, "processing exception");

				from("jetty://http://0.0.0.0:{{port}}/myservice")
					.log(LoggingLevel.INFO,"£ to direct error")
					.to("direct:error")
					.transform().constant("Bye World").to("mock:result");

				AtomicInteger atomicInteger = new AtomicInteger(0);

				from("direct:error")
					.log(LoggingLevel.INFO, "£ throwing Exception")
					.process(exchange -> {
						if (atomicInteger.incrementAndGet() < 3) {
							throw new IllegalStateException();
						}
					});
			}
		};
	}
}