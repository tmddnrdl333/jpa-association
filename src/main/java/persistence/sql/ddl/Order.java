package persistence.sql.ddl;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    public List<OrderItem> orderItems;

    public Order() {

    }

    public Order(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void addOrderItem(OrderItem orderItem) {
        if(this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(orderItem);
    }
}
