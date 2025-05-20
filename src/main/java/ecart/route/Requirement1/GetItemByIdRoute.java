package ecart.route.Requirement1;
import ecart.exception.ItemNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetItemByIdRoute extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GetItemByIdRoute.class);
    @Override
    public void configure() {

        
        onException(ItemNotFoundException.class)
            .handled(true)
            .log("ItemNotFoundException: ${exception.message}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))  // 404 Not Found
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setBody(simple("{ \"Item not found for ID: ${header.id}\" }"));

       
        onException(Throwable.class)
            .handled(true)
            .log("Unhandled exception occurred: ${exception.message}")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))  // 500 Internal Server Error
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setBody(simple("{ \"Internal Server Error. Please try again later.\" }"));

        rest("/ecart/item")
            .get("/{id}")
            .to("direct:getItemById");

        from("direct:getItemById")
            .routeId("GetItemByIdRoute")
            .log("Fetching item with ID: ${header.id}")
            .setBody(simple("${header.id}"))
            .toD("mongodb:myMongoBean?database=cart&collection=cart&operation=findById")
            .choice()
                .when(body().isNull())
                    .log("No item found for ID: ${header.id}")
                    .throwException(new ItemNotFoundException("Item not found for ID: ${header.id}"))
                .otherwise()
                    .log("Item found for ID: ${header.id}")
                    .marshal().json()
            .end();
            
    }
}
