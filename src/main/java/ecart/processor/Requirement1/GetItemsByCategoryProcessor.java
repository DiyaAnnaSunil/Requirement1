package ecart.processor.Requirement1;

import ecart.model.CategoryResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("getItemsByCategoryProcessor")
public class GetItemsByCategoryProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(GetItemsByCategoryProcessor.class);

    @Override
    public void process(Exchange exchange) {
        String categoryId = exchange.getIn().getHeader("categoryId", String.class);
        String includeSpecial = exchange.getIn().getHeader("includeSpecial", "true", String.class);
        boolean filterSpecial = !Boolean.parseBoolean(includeSpecial);

        Object body = exchange.getIn().getBody();
        List<Document> items = new ArrayList<>();
        if (body instanceof List<?>) {
            for (Object item : (List<?>) body) {
                if (item instanceof Document) {
                    items.add((Document) item);
                }
            }
        }

        Document category = exchange.getProperty("categoryDoc", Document.class);

        logger.info("Processing category ID: {}, includeSpecial: {}", categoryId, includeSpecial);
        logger.info("Category found: {}", category != null ? category.toJson() : "null");
        logger.info("Items before filter: {}", items.size());

        if (filterSpecial) {
            items.removeIf(item -> Boolean.TRUE.equals(item.getBoolean("specialProduct")));
        }

        CategoryResponse response = new CategoryResponse();
        response.setCategoryName(category.getString("categoryName"));
        response.setCategoryDepartment(category.getString("categoryDep"));
        response.setItems(items);

        if (items.isEmpty()) {
            response.setMessage("No items found in this category.");
        }

        logger.info("Final item count: {}", items.size());

        exchange.getMessage().setBody(response);
    }
}
