package camel.tumblr;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.*;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TumblrFunctions {

    private final Logger log = LoggerFactory.getLogger(TumblrFunctions.class);

    private final int LIMIT_PER_PAGE = 20;

    private final String blogUrl;

    private final JumblrClient client = realClient();

    public TumblrFunctions(String blogUrl) {
        this.blogUrl = blogUrl;
    }

    public List<Integer> buildOffsetCollection() {
        int limit = LIMIT_PER_PAGE;
        Blog posts = client.blogInfo(blogUrl);
        int postCount = posts.getPostCount();
        if (postCount > 5000) postCount = 5000;
        int size = postCount / limit;
        if (postCount % limit > 0) size++;
        return IntStream.range(0, size).map(i -> i * limit).boxed().collect(Collectors.toList());
    }

    public List<String> callRemoteService(@Body Integer offset, @Header("resultCount") AtomicInteger resultCount) {
        Map<String, Object> params = requestMap(offset);
        if (resultCount.get() > 5000) {
            return Collections.emptyList();
        }
        List<Post> posts = client.blogPosts(blogUrl, params);
        List<String> postList = posts.stream()
                .map(post -> (PhotoPost) post)
                .flatMap(photoPost -> photoPost.getPhotos().stream())
                .map(Photo::getOriginalSize)
                .filter(photo -> photo.getHeight() > 1000)
                .map(PhotoSize::getUrl)
                .collect(Collectors.toList());
        int total = resultCount.addAndGet(postList.size());
        log.debug("We've got a total of " + total + " results");
        return postList;
    }

    private Map<String, Object> requestMap(Object offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "photo");
        params.put("offset", offset);
        params.put("limit", LIMIT_PER_PAGE);
        return params;
    }

    public Object fetchImage(Exchange exchange) {
        String fileLocation = exchange.getIn().getBody(String.class);
        ByteArrayOutputStream baos;
        try {
            java.net.URL urlObject = new URL(fileLocation);
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

    private JumblrClient realClient() {
        final JumblrClient client = new JumblrClient("8bUSukewD12wuZuDJJfreXB0MfpIvvf2zA9UcrO98la5bq8zcG", "eUyE30fVKXEmCtJsuRfirOyXmUPIa5z1l0uca5ZAiz4h8zUrGa");
        client.setToken("a28i9tBzd2USOlm9y8j8UxGkEqnSnTM5phffo1JngwqGmguuRP", "wjFZZB65F4hEEYqdtlaFptiN0WllQxbndL5kPHbgmMJ3rfZrKP");
        return client;
    }

    public Exchange aggregateResults(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange != null) {
            List body = newExchange.getIn().getBody(List.class);
            List otherList = oldExchange.getIn().getBody(List.class);
            body.addAll(otherList);
            newExchange.getOut().setBody(body);
        }
        return newExchange;
    }
}
