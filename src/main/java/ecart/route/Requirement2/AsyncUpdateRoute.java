package ecart.route.Requirement2;

import ecart.processor.Requirement2.AsyncValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class AsyncUpdateRoute extends RouteBuilder {

    private final AsyncValidationProcessor asyncValidationProcessor;

    public AsyncUpdateRoute(AsyncValidationProcessor asyncValidationProcessor) {
        this.asyncValidationProcessor = asyncValidationProcessor;
    }

    @Override
    public void configure() {

        rest("/ecart")
                .post("/asyncUpdateInventory")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:asyncUpdateInventory");

        from("direct:asyncUpdateInventory")
                .routeId("AsyncUpdateInventoryRoute")
                .unmarshal().json(JsonLibrary.Jackson, ecart.model.UpdateInventoryRequest.class)
                .process(asyncValidationProcessor)
                .split(body()).parallelProcessing()
                .marshal().json(true)
                .log("Sending valid item to ActiveMQ: ${body}")
                .to("activemq:queue:inventory.update.queue?exchangePattern=InOnly&deliveryMode=2")
                .end()
                .setBody(constant("{\"status\": \"Valid items enqueued for async processing\"}"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
    }
}