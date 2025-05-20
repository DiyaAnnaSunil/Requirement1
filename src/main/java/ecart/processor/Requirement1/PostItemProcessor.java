package ecart.processor.Requirement1;

import ecart.exception.ItemInsertException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("PostItemProcessor")
public class PostItemProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // Get the data as a Map from the message body
        Map<String, Object> item = exchange.getIn().getBody(Map.class);

        if (item == null || item.get("_id") == null || item.get("categoryId") == null || item.get("itemPrice") == null) {
            throw new ItemInsertException("Missing required fields in input JSON.");
        }

        // Validate prices (Cast to Map to handle nested itemPrice)
        Map<String, Object> price = (Map<String, Object>) item.get("itemPrice");
        if (price == null) {
            throw new ItemInsertException("Item price is missing.");
        }

        // Safely get the price values as Numbers (handles Integer, Double, etc.)
        Number basePrice = (Number) price.get("basePrice");
        Number sellingPrice = (Number) price.get("sellingPrice");

        // Ensure the base price and selling price are both valid and greater than zero
        if (basePrice == null || sellingPrice == null || basePrice.doubleValue() <= 0 || sellingPrice.doubleValue() <= 0) {
            throw new ItemInsertException("Base price and selling price must be greater than zero.");
        }

        // Set the item as a property for further use
        exchange.setProperty("itemData", item);
    }
}
