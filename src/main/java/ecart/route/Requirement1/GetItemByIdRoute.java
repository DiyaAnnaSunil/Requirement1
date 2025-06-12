package ecart.route.Requirement1;

import ecart.exception.ItemNotFoundException;
import ecart.processor.Requirement1.GetItemByIdProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GetItemByIdRoute extends RouteBuilder {

    @Override
    public void configure() {
        // Handle item not found
        onException(ItemNotFoundException.class)
                .handled(true)
                .log("ItemNotFoundException: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{ \"Item not found for ID: ${header.id}\" }"));
        // Handle other exceptions
        onException(Throwable.class)
                .handled(true)
                .log("Unhandled exception: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(simple("{ \"Internal Server Error. Please try again later.\" }"));

        // REST endpoint
        rest("/ecart/item")
                .get("/{id}")
                .to("direct:getItemById");

        from("direct:getItemById")
                .routeId("GetItemByIdRoute")
                .log("Fetching item with ID: ${header.id}")

                // Step 1: Get item by ID from MongoDB
                .setBody(simple("${header.id}"))
                .to("mongodb:myMongoBean?database=cart&collection=cart&operation=findById")

                // Step 2: If not found, throw exception
                .choice()
                .when(body().isNull())
                .log("No item found for ID: ${header.id}")
                .throwException(ItemNotFoundException.class, "Item not found for ID: ${header.id}")
                .end()

                // Step 3: Save item document and extract categoryId
                .setProperty("itemDoc", body())
                .setHeader("categoryId", jsonpath("$.categoryId"))

                // Step 4: Lookup category from MongoDB
                .setBody(simple("${header.categoryId}"))
                .to("mongodb:myMongoBean?database=cart&collection=categories&operation=findById")
                .setProperty("categoryDoc", body())

                // Step 5: Restore itemDoc and call processor
                .setBody(simple("${exchangeProperty.itemDoc}"))
                .process(new GetItemByIdProcessor())
                .marshal().json();
    }
}
