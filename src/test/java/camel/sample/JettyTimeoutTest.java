package camel.sample;

import org.apache.camel.ExchangeTimedOutException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * Unit test for using http client SO timeout
 *
 * @version 
 */
public class JettyTimeoutTest extends BaseJettyTest {

    @Test
    public void testSendWithSOTimeoutNoTimeout() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        String out = template.requestBody("jetty:http://localhost:{{port}}/myservice?httpClient.timeout=5000", null, String.class);
        assertEquals("Bye World", out);

        assertMockEndpointsSatisfied();
    }
    
    @Test
    public void testSendWithSOTimeoutTimeout() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        try {
            // we use a timeout of 1 second
            template.requestBody("jetty:http://localhost:{{port}}/myservice?httpClient.timeout=1000", null, String.class);
            fail("Should throw an exception");
        } catch (RuntimeCamelException e) {
            assertIsInstanceOf(ExchangeTimedOutException.class, e.getCause());
        }

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jetty://http://localhost:{{port}}/myservice")
                    // but we wait for 2 sec before reply is sent back
                    .delay(2000)
                    .transform().constant("Bye World").to("mock:result");
            }
        };
    }
}