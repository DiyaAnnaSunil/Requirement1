package ecart.model;
import java.util.List;
import org.bson.Document;

public class CategoryResponse {
    private String categoryName;
    private String categoryDepartment;
    private List<Document> items;
    private String message;

    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryDepartment() {
        return categoryDepartment;
    }

    public void setCategoryDepartment(String categoryDepartment) {
        this.categoryDepartment = categoryDepartment;
    }

    public List<Document> getItems() {
        return items;
    }

    public void setItems(List<Document> items) {
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
