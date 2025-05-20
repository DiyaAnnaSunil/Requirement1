package ecart.config;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class RestConfig extends RouteBuilder {
    @Override
    public void configure() {
        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")
                .port(8080);
    }
}