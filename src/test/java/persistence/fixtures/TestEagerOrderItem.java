package persistence.fixtures;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eager_order_items")
public class TestEagerOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private Integer quantity;

    public TestEagerOrderItem() {
    }

    public TestEagerOrderItem(String product, Integer quantity) {
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
        return "CREATE TABLE IF NOT EXISTS eager_order_items (\n" +
                "    id BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                "    product VARCHAR(255),\n" +
                "    quantity INT,\n" +
                "    order_id BIGINT\n" +
                ");";
    }
}
