package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

public class MockHttpRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("jetty://http://0.0.0.0:8082/game-configuration-api/v2/configurations/context/game/virgingames/slot/platforms/phone")
        .process(exchange -> {
            exchange.getOut().setBody("{");
        })
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200));
    }
}
