package persistence.fixtures;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lazy_order_items")
public class TestLazyOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private Integer quantity;

    public TestLazyOrderItem() {
    }

    public TestLazyOrderItem(String product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public static String createTableQuery() {
        return "CREATE TABLE IF NOT EXISTS lazy_order_items (\n" +
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    product VARCHAR(255),\n" +
                "    quantity INT,\n" +
                "    order_id BIGINT\n" +
                ");";
    }
}
