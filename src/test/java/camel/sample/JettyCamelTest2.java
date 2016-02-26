package camel.sample;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.URISupport;
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
		template.requestBody("jetty:http://0.0.0.0:{{port}}/service/say/hi", null, String.class);
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				restConfiguration()
					.component("jetty")
					.bindingMode(RestBindingMode.json)
					.dataFormatProperty("prettyPrint", "true")
					.contextPath("service")
				.port("{{port}}");

				rest("/say")
	                .get("/{msg}").consumes("application/json").to("direct:process");

				from("direct:process")
					.process(exchange -> {
						Message msg = exchange.getIn();
						String uri = msg.getHeader(Exchange.HTTP_QUERY, String.class);
						Map<String, Object> parsedQuery = URISupport.parseQuery(uri);
						LOGGER.debug("Parsed query: " + parsedQuery);
					})
				.end();
			}
		};
	}
}