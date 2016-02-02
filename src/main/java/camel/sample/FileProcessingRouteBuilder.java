package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class FileProcessingRouteBuilder extends RouteBuilder {

    public static final String DIRECT_PROCESS_FILE = "direct:processFile";

	/**
	 * 
     */
    public void configure() {

    	from("file:src/main/resources/data?noop=true")
    		.to(DIRECT_PROCESS_FILE)
    	.end();

    	from(DIRECT_PROCESS_FILE)
        	.routeId("defaultRoute")
            .choice()
	        	.when(header(Exchange.CONTENT_TYPE).isEqualTo("application/json"))
		    		.log("not going to process json")
		    		.to("file:target/json")
                .when(xpath("/person/city = 'London'"))
                    .log("UK message")
                    .to("file:target/messages/uk")
                .otherwise()
                    .log("Other message")
                    .to("file:target/messages/others");
    }
}
