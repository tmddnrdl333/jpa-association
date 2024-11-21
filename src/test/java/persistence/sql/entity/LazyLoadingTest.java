package persistence.sql.entity;

import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.Review;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LazyLoadingTest {
    @Test
    void testLazyLoadingProxy() {
        final Order order = new Order(1L, "ORD123", new ArrayList<>());
        final List<Review> proxyReviews = createProxyReviews();
        order.addReviews(proxyReviews);

        final List<Review> reviews = order.getReviews();
        assertThat(Proxy.isProxyClass(reviews.getClass())).isTrue();

        final Review firstReview = reviews.getFirst();
        assertThat(Proxy.isProxyClass(firstReview.getClass())).isFalse();
        assertThat(firstReview).isInstanceOf(Review.class);
    }

    private List<Review> createProxyReviews() {
        return LazyLoadingHandler.createProxy(() -> List.of(new Review(1L, "Good")));
    }
}
