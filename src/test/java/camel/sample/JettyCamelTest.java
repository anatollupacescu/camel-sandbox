package camel.sample;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.loadbalancer.CircuitBreakerLoadBalancer;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Unit test for using http client SO timeout
 *
 * @version
 */
public class JettyCamelTest extends BaseJettyTest {

	private static final int REDELIVERY_COUNT = 3;
	private static final int CALL_COUNT = 3;
	private final Logger LOGGER = Logger.getLogger(JettyCamelTest.class);
	private final AtomicInteger atomicInteger = new AtomicInteger(0);

	@Test
	public void testSendWithSOTimeoutTimeout() throws Exception {
		// MockEndpoint mock = getMockEndpoint("mock:result");
		// mock.expectedMessageCount(1);
		for (int i = 0; i < CALL_COUNT; i++) {
			try {
			template.requestBody("jetty:http://0.0.0.0:{{port}}/myservice", null, String.class);
			} catch (Exception e){}
		}
		assertEquals(CALL_COUNT * (REDELIVERY_COUNT + 1), atomicInteger.get());
		// assertEquals("Bye World", out);
		// assertMockEndpointsSatisfied();
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {

				final CircuitBreakerLoadBalancer circuitBreaker = new CircuitBreakerLoadBalancer(
						Arrays.asList(Exception.class));
				circuitBreaker.setHalfOpenAfter(10000L);
				circuitBreaker.setThreshold(2);

				errorHandler(defaultErrorHandler().maximumRedeliveries(3));
				
/*
				onException(IllegalStateException.class)
				.handled(true)
				.maximumRedeliveries(REDELIVERY_COUNT).setBody(constant("lol"));
*/
				from("jetty://http://0.0.0.0:{{port}}/myservice")
					.transform().constant("Bye World")
					 .loadBalance(circuitBreaker)
					 	.to("direct:error")
					.end()
					.to("mock:result");

				from("direct:error").process(exchange -> {
					atomicInteger.incrementAndGet();
					LOGGER.debug("Throwing exception! circuit breaker state " + circuitBreaker.getState());
					throw new IllegalStateException();
				});
			}
		};
	}
}