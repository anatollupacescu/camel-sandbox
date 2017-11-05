package camel.sample;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TumblrRoute extends RouteBuilder {

    private final int LIMIT = 20;
    private final int MAX_RESULTS = 30;
    private final ServiceClient client;

    public TumblrRoute(ServiceClient client) {
        this.client = client;
    }

    @Override
    public void configure() throws Exception {
        from("direct:url")
                .id("ingress")
                .setHeader("offset", constant(0))
                .setHeader("continue", constant(Boolean.TRUE))
                .setBody(constant(new ArrayList<String>()))
                .to("direct:fetch");

        from("direct:fetch")
                .id("worker")
                .bean(this, "process")
                .choice()
                .when(header("continue").isEqualTo(Boolean.TRUE))
                .to("direct:fetch");
    }

    public List<String> process(@Body List<String> results, @Headers Map<String, Object> headers) {
        String url = (String) headers.get("url");
        Integer offset = (Integer) headers.get("offset");
        Map<String, Object> params = requestMap(offset);
        List<Post> posts = client.blogPosts(url, params);
        headers.put("continue", posts.size() == 20 && results.size() < MAX_RESULTS ? Boolean.TRUE : Boolean.FALSE);
        headers.put("offset", offset + LIMIT);
        List<String> urls = posts.stream()
                .map(post -> (PhotoPost) post)
                .flatMap(photoPost -> photoPost.getPhotos().stream())
                .map(Photo::getOriginalSize)
                .filter(photo -> photo.getHeight() > 1000)
                .map(PhotoSize::getUrl)
                .collect(Collectors.toList());
        results.addAll(urls);
        return results;
    }

    private Map<String, Object> requestMap(Object offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "photo");
        params.put("offset", offset);
        params.put("limit", LIMIT);
        return params;
    }
}
