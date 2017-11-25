package camel.tumblr;

import org.apache.camel.builder.RouteBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TumblrRoute extends RouteBuilder {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final TumblrFunctions functions;
    private final String tumblrBlogName;
    private final String filesLocation;

    public TumblrRoute(TumblrFunctions functions, String tumblrBlogName, String filesLocation) {
        this.functions = functions;
        this.tumblrBlogName = tumblrBlogName;
        this.filesLocation =  filesLocation + tumblrBlogName;
    }

    @Override
    public void configure() throws Exception {
        from("direct:url")
                .id("ingress")
                .to("direct:fetch-links")
                .log("Preparing to fetch ${body.size} images")
                .to("direct:fetch-images");

        from("direct:fetch-links")
                .setBody().method(functions, "buildOffsetCollection")
                .setHeader("resultCount", constant(new AtomicInteger(0)))
                .split(body(), functions::aggregateResults).executorService(executorService)
                    .stopOnException()
                    .log("Fetching posts starting with offset ${body}")
                    .setBody().method(functions, "callRemoteService");

        from("direct:fetch-images")
                .split(body()).executorService(executorService)
                    .log("Saving link ${body}")
                    .transform().exchange(functions::fetchImage)
                    .to("file://" + filesLocation)
                .end()
                .log("Done with " + tumblrBlogName);
    }
}
