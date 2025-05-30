package ecart.route.Requirement1;

import ecart.exception.CategoryNotFoundException;
import ecart.exception.ItemInsertException;
import ecart.model.Item;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class PostItemRoute extends RouteBuilder {

    @Override
    public void configure() {

        // Handle ItemInsertException
        onException(ItemInsertException.class)
                .handled(true)
                .log("ItemInsertException: ${exception.message} ${exchangeProperty.existingItemId}")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("{ \"${exception.message} ${exchangeProperty.existingItemId}\" }"));

        // Handle CategoryNotFoundException
        onException(CategoryNotFoundException.class)
                .handled(true)
                .log("CategoryNotFoundException: ${exception.message} ${exchangeProperty.InvalidCatId}")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("{\"${exception.message} ${exchangeProperty.InvalidCatId}\" }"));

        // Handle all other exceptions
        onException(Throwable.class)
                .handled(true)
                .log("Unhandled Exception: ${exception.message}")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("{  \"Internal Server Error\" }"));

        // REST endpoint definition
        rest("/ecart/insertItem")
                .post()
                .consumes("application/json")
                .produces("application/json")
                .to("direct:insertItem");

        // Route logic
        from("direct:insertItem")
                .routeId("PostItemRoute")

                // Unmarshal JSON to Item POJO
                .unmarshal().json(JsonLibrary.Jackson, Item.class)

                // Process input (validation, set lastUpdateDate)
                .process("PostItemProcessor")

                // Extract itemId for use in MongoDB queries
                .setProperty("itemId", simple("${exchangeProperty.itemData.id}"))

                // Check if item already exists
                .setBody(simple("{\"_id\": \"${exchangeProperty.itemId}\"}"))
                .to("mongodb:myMongoBean?database=cart&collection=cart&operation=findOneByQuery")
                .choice()
                .when(body().isNotNull())
                .setProperty("existingItemId", simple("${body['_id']}"))
                .throwException(new ItemInsertException("Item already exists with ID:"))
                .end()

                // Check if categoryId is valid
                .setBody(simple("{\"_id\": \"${exchangeProperty.itemData.categoryId}\"}"))
                .to("mongodb:myMongoBean?database=cart&collection=categories&operation=findOneByQuery")
                .choice()
                .when(body().isNull())
                .setProperty("InvalidCatId", simple("${exchangeProperty.itemData.categoryId}"))
                .throwException(new CategoryNotFoundException("Invalid category ID provided."))
                .end()

                // Insert new item into MongoDB
                .setBody(exchangeProperty("itemData"))
                .to("mongodb:myMongoBean?database=cart&collection=cart&operation=insert")

                // Success response
                .setBody(constant("{ \"Item inserted successfully.\" }"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"));
    }
}
