package camel.sample;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Unit test for using http client SO timeout
 *
 * @version
 */
public class JettyCamelTest2 extends BaseJettyTest {

	private final Logger LOGGER = Logger.getLogger(JettyCamelTest2.class);

	@Test
	public void testSendWithSOTimeoutTimeout() throws Exception {
		Object body = template.requestBody("jetty:http://0.0.0.0:{{port}}/service/say/hi", null, String.class);
		assertEquals("hi", body);
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				restConfiguration()
					.component("jetty")
					.bindingMode(RestBindingMode.off)
					.dataFormatProperty("prettyPrint", "true")
					.contextPath("service")
				.port("{{port}}");

				rest("/say")
	                .get("/{msg}").consumes("text/plain").to("direct:process");

				from("direct:process")
					.process(exchange -> {
						Message msg = exchange.getIn();
						String greeting = msg.getHeader("msg", String.class);
						LOGGER.debug("Client said: " + greeting);
						msg.setBody(greeting);
					})
				.end();
			}
		};
	}
}