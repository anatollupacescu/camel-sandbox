package camel.sample;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TumblrRouteTest extends CamelTestSupport {

    public static final String TUMBLR_BLOG_NAME = "beautieshunter";

    @Test
    public void mainTest() throws InterruptedException {
        String url = TUMBLR_BLOG_NAME + ".tumblr.com";
        Exchange response = template.request("direct:url", exchange -> exchange.getIn().setHeader("url", url));
        assertThat(response, is(notNullValue()));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {

            private final int LIMIT = 20;
            private final int MAX_RESULTS = 1000;
            private final JumblrClient client = realClient();

            @Override
            public void configure() throws Exception {
                from("direct:url")
                        .id("ingress")
                        .setHeader("offset", constant(0))
                        .setHeader("continue", constant(Boolean.TRUE))
                        .setBody(constant(new HashSet<String>()))
                        .to("direct:fetch")
                        .process(exchange -> {
                            Set lines = exchange.getIn().getBody(Set.class);
                            Path file = Paths.get("/home/anatol/Desktop/" + TUMBLR_BLOG_NAME);
                            Files.write(file, lines, Charset.forName("UTF-8"));
                        })
                .log("Done with " + TUMBLR_BLOG_NAME);

                from("direct:fetch")
                        .id("worker")
                        .transform()
                            .exchange(this::callRemoteService)
                        .log("Total size now: ${body.size}")
                        .choice()
                            .when(header("continue").isEqualTo(Boolean.TRUE))
                            .to("direct:fetch");
            }

            private Set<String> callRemoteService(Exchange exchange) {
                Message message = exchange.getIn();
                String url = message.getHeader("url", String.class);
                Integer offset = message.getHeader("offset", Integer.class);
                Map<String, Object> params = requestMap(offset);
                List<Post> posts = client.blogPosts(url, params);
                Set results = message.getBody(Set.class);
                List<String> urls = posts.stream()
                        .map(post -> (PhotoPost) post)
                        .flatMap(photoPost -> photoPost.getPhotos().stream())
                        .map(Photo::getOriginalSize)
                        .filter(photo -> photo.getHeight() > 1000)
                        .map(PhotoSize::getUrl)
                        .collect(Collectors.toList());
                results.addAll(urls);
                message.setHeader("continue", posts.size() == 20 && results.size() < MAX_RESULTS ? Boolean.TRUE : Boolean.FALSE);
                message.setHeader("offset", offset + LIMIT);
                return results;
            }

            private Map<String, Object> requestMap(Object offset) {
                Map<String, Object> params = new HashMap<>();
                params.put("type", "photo");
                params.put("offset", offset);
                params.put("limit", LIMIT);
                return params;
            }

            private JumblrClient realClient() {
                final JumblrClient client = new JumblrClient("8bUSukewD12wuZuDJJfreXB0MfpIvvf2zA9UcrO98la5bq8zcG", "eUyE30fVKXEmCtJsuRfirOyXmUPIa5z1l0uca5ZAiz4h8zUrGa");
                client.setToken("a28i9tBzd2USOlm9y8j8UxGkEqnSnTM5phffo1JngwqGmguuRP", "wjFZZB65F4hEEYqdtlaFptiN0WllQxbndL5kPHbgmMJ3rfZrKP");
                return client;//(blogName, options) ->
            }
        };
    }
}
