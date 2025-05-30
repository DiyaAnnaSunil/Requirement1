package ecart.processor.Requirement1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import ecart.model.ItemStockUpdate;
import ecart.model.StockUpdate;
import ecart.model.UpdateInventoryRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UpdateInventoryProcessor implements Processor {

    private final MongoDatabase mongoDatabase;

    public UpdateInventoryProcessor(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public void process(Exchange exchange) {
        UpdateInventoryRequest request = exchange.getIn().getBody(UpdateInventoryRequest.class);
        List<Map<String, Object>> results = new ArrayList<>();
        MongoCollection<Document> collection = mongoDatabase.getCollection("cart");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateTimeStr = LocalDateTime.now().format(formatter);

        if (request == null || request.getItems() == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "failed");
            error.put("reason", "'items' field is missing or null");
            results.add(error);
            exchange.setProperty("results", results);
            return;
        }

        for (ItemStockUpdate item : request.getItems()) {
            String id = item.get_id();
            StockUpdate stockUpdate = item.getStockUpdate();

            Map<String, Object> result = new HashMap<>();
            result.put("_id", id);

            try {
                int soldOut = stockUpdate.getSoldOut();
                int damaged = stockUpdate.getDamaged();

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

                        Document updateDoc = new Document();
                        updateDoc.put("stockDetails", stock);
                        updateDoc.put("lastUpdateDate", currentDateTimeStr);

                        collection.updateOne(new Document("_id", id),
                                new Document("$set", updateDoc));

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
