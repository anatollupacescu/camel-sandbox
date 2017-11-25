package camel.sample;

import org.apache.camel.builder.RouteBuilder;

public class SqlRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:get-offset")
                .setBody(constant("SELECT * FROM TUMBLR.OFFSET WHERE blogId = :?blogId"))
                .to("jdbc:tumblr?useHeadersAsParameters=true")
                .end();

        from("direct:insert")
                .setHeader("min", constant(123))
                .setBody(constant("insert into articles(name, category, tags, author) values (:?name, :?category, :?tags, 'Admin'"))
                .to("jdbc:tumblr?useHeadersAsParameters=true");
    }
}
