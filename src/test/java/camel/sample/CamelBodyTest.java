package camel.sample;

import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CamelBodyTest extends CamelTestSupport {

	@Test
	@Ignore
	public void test() throws InterruptedException, IOException {
		List<String> numbers = Arrays.asList("1", "2", "3");
        Object list = template.requestBody("direct:input", numbers , List.class);
		assertThat(list, is(notNullValue()));
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:input")
						.transform().inMessage(this::toList)
						.split().body(List.class, this::spliBy)
                        .log("splitted ${body}")
                .end();
			}

            private List<String> toList(Message message) {
			    return message.getBody(List.class);
            }

            private List<?> spliBy(List<String> input) {
			    return input;
			}
        };
	}
}
