package camel.sample;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CameRNGTestClient extends CamelTestSupport {

	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	@Ignore
	public void test() throws InterruptedException, IOException {
		int i = nextInt(3);
		assertEquals(2, i);
	}

    public int nextInt(int n) throws IOException {
        List<String> numbers = Collections.singletonList(String.valueOf(n));
		List<String> list = context.createProducerTemplate().requestBody("direct:input", numbers , List.class);
        return Integer.valueOf(list.get(0));
    }
    
	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:input").routeId("testRoute").marshal().json(JsonLibrary.Jackson)
                .to("jetty:http://localhost:8080/rng/numbers")
                .unmarshal().json(JsonLibrary.Jackson, List.class)
                .end();
			}
		};
	}
}
