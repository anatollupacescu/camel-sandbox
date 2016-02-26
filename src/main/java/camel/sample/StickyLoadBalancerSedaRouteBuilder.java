package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.loadbalancer.StickyLoadBalancer;

public class StickyLoadBalancerSedaRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        from("direct:input").

                id("myroute").

                log("Received ${body}").

                loadBalance(new StickyLoadBalancer(header("group"))).

                to("seda:input1", "seda:input2").end();

        from("seda:input1").process(new Processor() {
            private String group;

            public void process(Exchange exc) throws Exception {
                Message m = exc.getIn();
                String inGroup = m.getHeader("group", String.class);
                if (group == null) {
                    this.group = inGroup;
                } else if (!inGroup.equals(group)) {
                    System.out.println("Inconsistent behaviour");
                } else {
                    System.out.println(m.getBody() + " > " + inGroup);
                }
            }
        }).onException(Exception.class).throwException(new IllegalStateException()).end();

        from("seda:input2").process(new Processor() {
            private String group;

            public void process(Exchange exc) throws Exception {
                Message m = exc.getIn();
                String inGroup = m.getHeader("group", String.class);
                if (group == null) {
                    this.group = inGroup;
                } else if (!inGroup.equals(group)) {
                    System.out.println("Inconsistent behaviour");
                } else {
                    System.out.println(m.getBody() + " > " + inGroup);
                }
            }
        }).onException(Exception.class).throwException(new IllegalStateException()).end();
    }
}
