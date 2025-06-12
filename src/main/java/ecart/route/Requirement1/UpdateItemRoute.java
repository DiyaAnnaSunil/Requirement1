package ecart.route.Requirement1;
import ecart.exception.ItemNotFoundException;
import ecart.exception.StockUpdateException;
import ecart.model.UpdateInventoryRequest;
import ecart.processor.Requirement1.UpdateInventoryProcessor;
import ecart.processor.Requirement1.UpdateInventoryResponseProcessor;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UpdateItemRoute extends RouteBuilder {

    private final UpdateInventoryProcessor updateInventoryProcessor;
    private final UpdateInventoryResponseProcessor updateInventoryResponseProcessor;
    public UpdateItemRoute(UpdateInventoryProcessor updateInventoryProcessor,UpdateInventoryResponseProcessor updateInventoryResponseProcessor)
    {
    	this.updateInventoryProcessor=updateInventoryProcessor;
    	this.updateInventoryResponseProcessor=updateInventoryResponseProcessor;
    }

    @Override
    public void configure() {

        // Custom Exception Handling
        onException(ItemNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{ \"error\": \"${exception.message}\" }"));

        onException(StockUpdateException.class)
                .handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{ \"error\": \"${exception.message}\" }"));


        // Basic Exception Handling for JSON format only (not status codes here)
        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{ \"error\": \"${exception.message}\" }"));

        rest("/ecart/updateInventory")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:updateInventory");
        

        from("direct:updateInventory")
        		.routeId("UpdateItem")
               // .unmarshal().json()
                .log("Received update inventory request: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, UpdateInventoryRequest.class)
                .process(updateInventoryProcessor) // NEW: processor handles per-item logic
                .process(updateInventoryResponseProcessor) // NEW: handles response formatting and HTTP code
                .marshal().json(true)
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
    }
}