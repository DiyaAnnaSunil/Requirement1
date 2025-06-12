package ecart.processor.Requirement2;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import ecart.model.ItemStockUpdate;
import ecart.model.StockUpdate;
import ecart.model.UpdateInventoryRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsyncValidationProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncValidationProcessor.class);

    private final MongoDatabase mongoDatabase;

    public AsyncValidationProcessor(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        UpdateInventoryRequest request = exchange.getIn().getBody(UpdateInventoryRequest.class);
        if (request == null || request.getItems() == null) {

            throw new IllegalArgumentException("Missing 'items' in request");
        }

        List<ItemStockUpdate> items = request.getItems();
        List<String> ids = items.stream()
                .map(ItemStockUpdate::get_id)
                .collect(Collectors.toList());

        MongoCollection<Document> collection = mongoDatabase.getCollection("cart");

        // Query DB once for all valid IDs in request batch
        List<String> validIds = new ArrayList<>();
        for (Document doc : collection.find(new Document("_id", new Document("$in", ids)))) {
            validIds.add(doc.getString("_id"));
        }

        List<ItemStockUpdate> validItems = new ArrayList<>();
        List<String> invalidItemMessages = new ArrayList<>();

        // Validate each item individually
        for (ItemStockUpdate item : items) {
            String id = item.get_id();
            StockUpdate stock = item.getStockUpdate();

            if (id == null || !validIds.contains(id)) {
                invalidItemMessages.add("Invalid or missing ID for item: " + id);
                continue;  // skip invalid item
            }
            if (stock == null) {
                invalidItemMessages.add("Missing stock update for item: " + id);
                continue;  // skip invalid item
            }
            if (stock.getSoldOut() < 0 || stock.getDamaged() < 0) {
                invalidItemMessages.add("Negative stock update for item: " + id);
                continue;  // skip invalid item
            }

            validItems.add(item);  // valid item, add to output list
        }

        // Log all invalid items as a single warning, so you have visibility
        if (!invalidItemMessages.isEmpty()) {
            LOG.warn("Skipping invalid items: {}", String.join("; ", invalidItemMessages));
        }

        // Pass only valid items downstream
        exchange.getIn().setBody(validItems);
    }
}
