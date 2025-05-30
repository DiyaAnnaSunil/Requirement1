package ecart.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemStockUpdate {
    private String _id;
    @JsonProperty("stockDetails")
    private StockUpdate stockUpdate;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public StockUpdate getStockUpdate() {
        return stockUpdate;
    }

    public void setStockUpdate(StockUpdate stockUpdate) {
        this.stockUpdate = stockUpdate;
    }
}
