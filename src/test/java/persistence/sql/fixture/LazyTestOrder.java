package persistence.sql.fixture;

import jakarta.persistence.CascadeType;
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
@Table(name = "lazy_orders")
public class LazyTestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id")
    private List<LazyTestOrderItem> orderItems = new ArrayList<>();

    public LazyTestOrder(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LazyTestOrder() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public List<LazyTestOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<LazyTestOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(LazyTestOrderItem orderItem) {
        orderItems.add(orderItem);
    }
}
