package ecart.processor.Requirement1;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecart.model.Item;
import ecart.model.ItemResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;

public class GetItemByIdProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Step 1: Get item from body (MongoDB returned Document)
        Document itemDoc = exchange.getIn().getBody(Document.class);
        if (itemDoc == null) {
            exchange.getIn().setBody(null);
            return;
        }

        // Step 2: Convert to POJO
        Item item = mapper.readValue(itemDoc.toJson(), Item.class);

        // Step 3: Get category document from exchange property (set earlier in route)
        Document categoryDoc = exchange.getProperty("categoryDoc", Document.class);
        String categoryName = (categoryDoc != null) ? categoryDoc.getString("categoryName") : "Unknown";

        // Step 4: Build response
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setItemName(item.getItemName());
        response.setCategoryName(categoryName);
        response.setItemPrice(item.getItemPrice());
        response.setStockDetails(item.getStockDetails());
        response.setSpecialProduct(item.getSpecialProduct());


        exchange.getIn().setBody(response);
    }
}
