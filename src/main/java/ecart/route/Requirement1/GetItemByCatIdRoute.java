package ecart.route.Requirement1;

import ecart.exception.CategoryNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class GetItemByCatIdRoute extends RouteBuilder {

    @Override
    public void configure() {

    	onException(CategoryNotFoundException.class)
        .handled(true)
        .logHandled(true)
        .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400)) // 400 Bad Request
        .setBody(simple("{ \"Invalid categoryId: ${header.categoryId}\" }"));

    onException(Throwable.class)
        .handled(true)
        .logHandled(true)
        .log("Unhandled exception occurred: ${exception.message}")
        .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500)) // 500 Internal Server Error
        .setBody(simple("{ \"Internal Server Error. Please try again later.\" }"));

        rest("/ecart/items")
            .get("/{categoryId}")
            .to("direct:getItemsByCategory");

        from("direct:getItemsByCategory")
            .routeId("GetItemByCategoryId")
            .setHeader("categoryId", simple("${header.categoryId}"))
            .setBody(simple("${header.categoryId}"))
            .to("mongodb:myMongoBean?database=cart&collection=categories&operation=findById")
            .choice()
                .when(body().isNull())
                    .throwException(new CategoryNotFoundException("Invalid categoryId: ${header.categoryId}"))
            .end()
            // Save category doc to property
            .setProperty("categoryDoc", body())
            .setHeader("CamelMongoDbCriteria", simple("{\"categoryId\": \"${header.categoryId}\"}"))
            .to("mongodb:myMongoBean?database=cart&collection=cart&operation=findAll")
            .process("getItemsByCategoryProcessor")
            .marshal().json();
    }
}
