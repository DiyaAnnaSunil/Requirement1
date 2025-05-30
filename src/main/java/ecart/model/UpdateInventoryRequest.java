package ecart.model;

import java.util.List;

public class UpdateInventoryRequest {
    private List<ItemStockUpdate> items;

    public List<ItemStockUpdate> getItems() {
        return items;
    }

    public void setItems(List<ItemStockUpdate> items) {
        this.items = items;
    }
}
