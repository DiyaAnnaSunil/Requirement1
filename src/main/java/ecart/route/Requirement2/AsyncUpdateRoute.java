package ecart.route.Requirement2;
import ecart.exception.InvalidItemException;
import ecart.exception.ItemNotFoundException;
import ecart.exception.StockDetailsNotFoundException;
import ecart.processor.Requirement2.ValidationProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class AsyncUpdateRoute extends RouteBuilder {

    private final ValidationProcessor validationProcessor;

    public AsyncUpdateRoute(ValidationProcessor validationProcessor) {
        this.validationProcessor = validationProcessor;
    }

    @Override
    public void configure() {
        onException(InvalidItemException.class, StockDetailsNotFoundException.class, ItemNotFoundException.class)
                .handled(true)
                .log(LoggingLevel.WARN,"Skipping invalid item:${exception.message}");



        rest("/ecart")
                .post("/asyncUpdateInventory")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:asyncUpdateInventory");

        from("direct:asyncUpdateInventory")
                .routeId("AsyncUpdateInventoryRoute")
                .unmarshal().json()
                .log("Received async update request: ${body}")
                .split().jsonpath("$.items[*]")
                .doTry()
                .process(validationProcessor)
                .marshal().json(true)
                .log("Sending valid item to ActiveMQ: ${body}")
                .to("activemq:queue:inventory.update.queue?exchangePattern=InOnly&deliveryMode=2")
                .doCatch(Exception.class)
                .log(LoggingLevel.WARN,"Item skipped:${exception.message}")
                .end()
                .end()
                .setBody(constant("{\"status\": \"Valid items enqueued for async processing\"}"))
                .setHeader("Content-Type", constant("application/json"));
    }
}
