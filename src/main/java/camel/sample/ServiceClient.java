package camel.sample;

import com.tumblr.jumblr.types.Post;

import java.util.List;
import java.util.Map;

public interface ServiceClient {

    List<Post> blogPosts(String blogName, Map<String, Object> options);
}
