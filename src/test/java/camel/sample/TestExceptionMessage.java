package camel.sample;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class TestExceptionMessage extends CamelTestSupport {

	private static final String DIRECT_INPUT = "direct:input";

	@Produce(uri = DIRECT_INPUT)
	private ProducerTemplate testProducer;

	private AtomicInteger counter = new AtomicInteger(0);
	
	@Test
	public void testRoute2() throws InterruptedException {
		sendMessage(DIRECT_INPUT, 0);
		sendMessage(DIRECT_INPUT, 1);
		sendMessage(DIRECT_INPUT, 2);
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {

				onException(Exception.class)
				.handled(true)
				.process(ex -> {
					System.out.println(ex.getIn().getBody());
				});

				onException(IOException.class)
				.handled(true)
				.process(ex -> {
					System.out.println(ex.getIn().getBody());
				});

				onException(SocketException.class)
				.handled(true)
				.process(ex -> {
					System.out.println(counter.get());
				});	

				from(DIRECT_INPUT)
					.routeId("testRoute")
					.to("direct:other")
					.end();

				from("direct:other")
					.errorHandler(noErrorHandler())
					.loadBalance().circuitBreaker(2, 1000L, Exception.class)
					.process( ex -> { 
						counter.incrementAndGet();
						throw new ConnectException();
					})
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
