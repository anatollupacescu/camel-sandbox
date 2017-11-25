package camel.sample;

import camel.tumblr.TumblrFunctions;
import camel.tumblr.TumblrRoute;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;

public class TumblrRouteTest extends CamelTestSupport {

    @Mock
    private TumblrFunctions functions;

    @Test
    public void testSaveImages() throws InterruptedException {
        given(functions.buildOffsetCollection()).willReturn(Arrays.asList(0, 20));
        given(functions.callRemoteService(anyInt(), any(AtomicInteger.class))).willThrow(new RuntimeException());
        Object response = template.requestBody("direct:url", new Object());
        assertThat(response, is(notNullValue()));
    }

    @Test
    public void doSaveImages() throws InterruptedException {
        Object response = template.requestBody("direct:url", new Object());
        assertThat(response, is(notNullValue()));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return getRealRouteBuilder();
    }

    private RouteBuilder getMockRouteBuilder() {
        functions = mock(TumblrFunctions.class);
        String filesLocation = "/home/anatol/Desktop/";
        String tumblrBlogName = "surfboards";
        return new TumblrRoute(functions, tumblrBlogName, filesLocation);
    }

    private RouteBuilder getRealRouteBuilder() {
        String tumblrBlogName = "surfboards";
        String filesLocation = "/home/anatol/Desktop/";
        functions = new TumblrFunctions(tumblrBlogName);
        return new TumblrRoute(functions, tumblrBlogName, filesLocation);
    }
}
