package persistence.sql.ddl;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    private Long id;
    private String content;

    public Review(final Long id, final String content) {
        this.id = id;
        this.content = content;
    }

    protected Review() {
    }
}
