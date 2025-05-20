package ecart.processor.Requirement2;
import com.mongodb.client.MongoDatabase;
import ecart.exception.InvalidItemException;
import ecart.exception.StockDetailsNotFoundException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component

public class ValidationProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationProcessor.class);
    private final MongoDatabase mongoDatabase;

    public ValidationProcessor(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    @Override
    public void process(Exchange exchange) {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);

        if (item == null) {
            throw new InvalidItemException("Item is null");
        }

        String id = (String) item.get("_id");
        if (id == null || id.isEmpty()) {
            throw new InvalidItemException("Missing _id in item: " + item);
        }


        Map<String, Object> stockDetails = (Map<String, Object>) item.get("stockDetails");
        if (stockDetails == null) {
            throw new StockDetailsNotFoundException("Missing stockDetails in item: " + item);
        }

        try {
            int soldOut = Integer.parseInt(stockDetails.get("soldOut").toString());
            int damaged = Integer.parseInt(stockDetails.get("damaged").toString());

            if (soldOut < 0 || damaged < 0) {
                throw new InvalidItemException("Negative soldOut or damaged: " + item);
            }
        } catch (Exception e) {
            throw new InvalidItemException("Invalid soldOut or damaged value in item: " + item);
        }

        Document existing = mongoDatabase.getCollection("cart").find(new Document("_id", id)).first();
        if (existing == null) {
            throw new InvalidItemException("Invalid ItemId: " + id);
        }
    }
}
