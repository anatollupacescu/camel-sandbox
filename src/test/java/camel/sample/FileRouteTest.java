package camel.sample;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.util.FileUtil;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class FileRouteTest extends CamelTestSupport {

    public static final String TUMBLR_BLOG_NAME = "nudefree74";

    @Test
    public void mainTest() throws InterruptedException {
        List<String> input = Arrays.asList(
                "http://localhost:8080/unu.png",
                "http://localhost:8080/doi.jpg",
                "http://localhost:8080/trei.png");
        Exchange response = template.request("direct:url", exchange -> exchange.getIn().setBody(input));
        assertThat(response, is(notNullValue()));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:url")
                        .split(body()).executorService(Executors.newFixedThreadPool((10)))
                            .setHeader(Exchange.HTTP_URI, body())
                            .log("Saving link ${body}")
                            .transform().exchange(this::fetchImage)
                            .to("file:///home/anatol/Desktop/tumblr2")
                        .end()
                .log("Done with " + TUMBLR_BLOG_NAME);
            }

            private Object fetchImage(Exchange exchange) {
                String fileLocation = exchange.getIn().getBody(String.class);
                ByteArrayOutputStream baos;
                try {
                    URL urlObject = new URL(fileLocation);
                    BufferedImage image = ImageIO.read(urlObject);
                    baos = new ByteArrayOutputStream();
                    String filename = FileUtil.stripPath(fileLocation);
                    String ext = FileUtil.onlyExt(fileLocation);
                    ImageIO.write(image, ext, baos);
                    exchange.getIn().setHeader(Exchange.FILE_NAME, filename);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return baos;
            }
        };
    }
}
