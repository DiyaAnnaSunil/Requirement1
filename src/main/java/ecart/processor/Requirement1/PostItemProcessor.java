package ecart.processor.Requirement1;

import ecart.exception.ItemInsertException;
import ecart.model.Item;
import ecart.model.ItemPrice;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component("PostItemProcessor")
public class PostItemProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Item item = exchange.getIn().getBody(Item.class);

        if (item == null || item.getId() == null || item.getCategoryId() == null || item.getItemPrice() == null) {
            throw new ItemInsertException("Missing required fields in input JSON.");
        }

        ItemPrice price = item.getItemPrice();
        if (price.getBasePrice() <= 0 || price.getSellingPrice() <= 0) {
            throw new ItemInsertException("Base price and selling price must be greater than zero.");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy - MM - dd HH: mm: ss");
        String formattedDate = java.time.LocalDateTime.now().format(formatter);
        item.setLastUpdateDate(formattedDate);


        exchange.setProperty("itemData", item);
    }
}
