package persistence.fixtures;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eager_orders")
public class TestEagerOrder {

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<TestEagerOrderItem> orderItems = new ArrayList<>();

    public TestEagerOrder() {
    }

    public TestEagerOrder(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public List<TestEagerOrderItem> getOrderItems() {
        return orderItems;
    }

    public static String createTableQuery() {

        return "CREATE TABLE IF NOT EXISTS eager_orders (\n" +
                "    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,\n" +
                "    orderNumber VARCHAR(255)\n" +
                ");";
    }
}
