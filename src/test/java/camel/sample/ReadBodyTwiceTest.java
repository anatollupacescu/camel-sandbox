package camel.sample;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class ReadBodyTwiceTest extends CamelTestSupport {

    @EndpointInject(uri = "mock:response")
    public MockEndpoint resultEndpoint;

    @Test
    public void testRoute2() throws InterruptedException {

        resultEndpoint.expectedMessageCount(1);

        Exchange ex = sendMessage("direct:input", new ByteArrayInputStream("test".getBytes()));
        String body = ex.getIn().getBody(String.class);
        assertEquals("test", body);
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from("direct:input")
                        .routeId("testRoute")
                        .streamCaching()
                        .log("${body}")
                        .to("log:mr.camel?level=INFO&showAll=true&multiline=true")
                        .log("${body}")
                        .convertBodyTo(String.class)
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
