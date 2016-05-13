package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.RejectedExecutionException;

import static org.hamcrest.CoreMatchers.isA;

public class CircuitBreakerCamelTest2 extends CamelTestSupport {

	@Test
	public void testRoute1() throws InterruptedException {
		Exchange exc = sendMessage("direct:input", "lol");
		assertNotNull(exc.getException());
		assertThat(exc.getException(), isA(Exception.class));
	}

	@Test
	public void testRoute2() throws InterruptedException {
		sendMessage("direct:input", "lol1");
		Exchange exc = sendMessage("direct:input", "lol2");
		assertNotNull(exc.getException());
		assertTrue(exc.getException() instanceof SocketTimeoutException);
	}

	@Test
	public void testRoute3() throws InterruptedException {
		sendMessage("direct:input", "lol");
		sendMessage("direct:input", "lol");
		Exchange exc = sendMessage("direct:input", "lol");
		assertNotNull(exc.getException());
		assertTrue(exc.getException() instanceof RejectedExecutionException);
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {

				from("direct:input")
					.routeId("testRoute")
					.convertBodyTo(String.class)
					.loadBalance()
						.circuitBreaker(2, 500, IOException.class)
						.process(exchange -> {
							throw new SocketTimeoutException();
						})
					.end()
				.end();
			}
		};
	}

	private Exchange sendMessage(final String endpoint, final Object body) {
		return template.send(endpoint, exchange -> {
			exchange.getIn().setBody(body);
		});
	}
}
