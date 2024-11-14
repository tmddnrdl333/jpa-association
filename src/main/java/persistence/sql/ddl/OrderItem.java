package persistence.sql.ddl;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String product;

    private Integer quantity;

    private Long orderId;

    public OrderItem() {

    }

    public OrderItem(String product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public OrderItem(Long orderId, String product, Integer quantity) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
