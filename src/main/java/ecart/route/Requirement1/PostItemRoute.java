package ecart.route.Requirement1;

import ecart.exception.ItemInsertException;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class PostItemRoute extends RouteBuilder {

    @Override
    public void configure() {

        // Handle validation exception
        onException(ItemInsertException.class)
            .handled(true)
            .log("ItemInsertException: ${exception.message} ${exchangeProperty.existingItemId}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
            .setBody(simple("{ \"${exception.message} ${exchangeProperty.existingItemId}\" }"));

        // Handle generic/unexpected exceptions
        onException(Throwable.class)
            .handled(true)
            .log("Unhandled Exception: ${exception.message}")
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500)) // 500 Internal Server Error
            .setBody(simple("{ \"\"Internal Server Error\" }"));

        // REST Endpoint
        rest("/ecart/insertItem")
            .post()
            .consumes("application/json")
            .produces("application/json")
            .to("direct:insertItem");

        // Main Route Logic
        from("direct:insertItem")
            .routeId("PostItemRoute")
            .unmarshal().json(JsonLibrary.Jackson, Map.class) // Convert JSON to Map instead of POJO
            .process("PostItemProcessor") // Call processor
            .setBody(exchangeProperty("itemData"))
            .setBody(simple("{\"_id\": \"${exchangeProperty.itemData['_id']}\"}"))  // Use correct Map access
            .to("mongodb:myMongoBean?database=cart&collection=cart&operation=findOneByQuery")
            .choice()
            .when(body().isNotNull())
            .setProperty("existingItemId", simple("${body['_id']}"))  // Access _id from Map
            .throwException(new ItemInsertException("Item already exists with ID:"))
            .end()
            .setBody(exchangeProperty("itemData"))
            .to("mongodb:myMongoBean?database=cart&collection=cart&operation=insert")
            .setBody(simple("{\"message\": \"Item inserted successfully.\"}"))
            .setHeader("Content-Type", constant("application/json"));
    }
}
