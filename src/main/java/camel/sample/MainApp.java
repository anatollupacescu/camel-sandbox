package camel.sample;

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
