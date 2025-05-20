package ecart.processor.Requirement1;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UpdateInventoryResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        try {
            List<Map<String, Object>> results = exchange.getProperty("results", List.class);

            // Validate input
            if (results == null || results.isEmpty()) {
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                exchange.getMessage().setBody(createErrorResponse("Invalid request: results missing or empty"));
                return;
            }

            List<Map<String, Object>> success = new ArrayList<>();
            List<Map<String, Object>> failed = new ArrayList<>();
            boolean allNotFound = true;

            // Process results
            for (Map<String, Object> result : results) {
                if ("success".equals(result.get("status"))) {
                    success.add(result);
                    allNotFound = false;
                } else {
                    failed.add(result);
                    if (!"Item not found".equals(result.get("reason"))) {
                        allNotFound = false;
                    }
                }
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("successfulUpdates", success);
            response.put("failedUpdates", failed);

            // Set HTTP response code
            if (allNotFound && !results.isEmpty()) {
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                exchange.getMessage().setBody(createErrorResponse("All requested items not found"));
            } else if (success.isEmpty()) {
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                exchange.getMessage().setBody(response);
            } else {
                exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
                exchange.getMessage().setBody(response);
            }

        } catch (Exception e) {
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            exchange.getMessage().setBody(createErrorResponse("Internal server error"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }
}