package camel;

import camel.sample.FileProcessingRouteBuilder;
import camel.sample.MockHttpRouteBuilder;
import camel.sample.StickyLoadBalancerSedaRouteBuilder;
import org.apache.camel.main.Main;

public class MainApp {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.addRouteBuilder(new FileProcessingRouteBuilder());
        main.addRouteBuilder(new StickyLoadBalancerSedaRouteBuilder());
        main.addRouteBuilder(new MockHttpRouteBuilder());
        main.run(args);
    }
}
