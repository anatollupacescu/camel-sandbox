package camel.sample;

import camel.model.ModelA;
import camel.model.ModelB;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.dozer.DozerTypeConverterLoader;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.dozer.DozerBeanMapper;
import org.junit.Test;

import java.util.Arrays;

public class CamelDozerTest extends CamelTestSupport {

	@Produce(uri = "direct:input")
	public ProducerTemplate testProducer;

	@Test
	public void test() throws InterruptedException {
		ModelA body = new ModelA();
		body.setName("Romeo");
		body.setCompany("gsys");
		testProducer.sendBody(body);
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() throws Exception {
				DozerBeanMapper mapper = new DozerBeanMapper(Arrays.asList(new String[] {"dozer.xml"}));
				DozerTypeConverterLoader loader = new DozerTypeConverterLoader();
				loader.init(context, mapper);

				from("direct:input").bean(CamelDozerTest.class, "parseB");
			}
		};
	}

	public static void parseB(ModelB b) {
		System.out.println(b);
	}
}
