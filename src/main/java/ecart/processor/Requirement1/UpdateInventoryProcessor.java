package ecart.processor.Requirement1;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateInventoryProcessor implements Processor {

   private final MongoDatabase mongoDatabase;
   public UpdateInventoryProcessor(MongoDatabase mongoDatabase)
   {
	   this.mongoDatabase=mongoDatabase;
   }
    

    @Override
    public void process(Exchange exchange) {
        Map<String, Object> body = exchange.getIn().getBody(Map.class);
        Object itemsObj = body.get("items");

        List<Map<String, Object>> results = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection("cart");

        if (!(itemsObj instanceof List)) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "failed");
            error.put("reason", "'items' field is missing or not a list");
            results.add(error);
            exchange.setProperty("results", results);
            return;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) itemsObj;

        for (Map<String, Object> item : items) {
            String id = (String) item.get("_id");
            Map<String, Object> stockDetails = (Map<String, Object>) item.get("stockDetails");

            Map<String, Object> result = new HashMap<>();
            result.put("_id", id);

            try {
                int soldOut = Integer.parseInt(stockDetails.get("soldOut").toString());
                int damaged = Integer.parseInt(stockDetails.get("damaged").toString());

                Document existing = collection.find(new Document("_id", id)).first();

                if (existing == null) {
                    result.put("status", "failed");
                    result.put("reason", "Item not found");
                } else {
                    Document stock = (Document) existing.get("stockDetails");
                    int availableStock = stock.getInteger("availableStock", 0);
                    int newStock = availableStock - soldOut - damaged;

                    if (newStock < 0) {
                        result.put("status", "failed");
                        result.put("reason", "Stock would go below zero");
                    } else {
                        stock.put("availableStock", newStock);
                        collection.updateOne(new Document("_id", id),
                                new Document("$set", new Document("stockDetails", stock)));
                        result.put("status", "success");
                        result.put("message", "Updated successfully");
                    }
                }
            } catch (Exception e) {
                result.put("status", "failed");
                result.put("reason", e.getMessage());
            }

            results.add(result);
        }

        exchange.setProperty("results", results);
    }
}