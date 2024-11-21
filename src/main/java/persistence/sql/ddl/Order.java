package persistence.sql.ddl;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Review> reviews;

    public Order(final Long id, final String orderNumber, final List<OrderItem> orderItems) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.orderItems = orderItems;
    }

    protected Order() {
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void addReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "Order{" +
               "id=" + id +
               ", orderNumber='" + orderNumber + '\'' +
               ", orderItems=" + orderItems +
               '}';
    }
}
