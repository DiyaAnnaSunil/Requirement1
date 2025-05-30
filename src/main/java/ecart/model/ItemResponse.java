package ecart.model;

import java.util.List;

public class ItemResponse {
    private String id;
    private String itemName;
    private String categoryName;
    private ItemPrice itemPrice;
    private StockDetails stockDetails;
    private Boolean specialProduct;
   // private List<Review> review;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public ItemPrice getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(ItemPrice itemPrice) {
        this.itemPrice = itemPrice;
    }

    public StockDetails getStockDetails() {
        return stockDetails;
    }

    public void setStockDetails(StockDetails stockDetails) {
        this.stockDetails = stockDetails;
    }

    public Boolean getSpecialProduct() {
        return specialProduct;
    }

    public void setSpecialProduct(Boolean specialProduct) {
        this.specialProduct = specialProduct;
    }

//    public List<Review> getReview() {
//        return review;
//    }
//
//    public void setReview(List<Review> review) {
//        this.review = review;
//    }
}
