package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class MockHttpRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("jetty://http://0.0.0.0:{{server.port}}/game-configuration-api/v3")
                .removeHeader("Camel*")
                .setBody().simple("${headers}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}
