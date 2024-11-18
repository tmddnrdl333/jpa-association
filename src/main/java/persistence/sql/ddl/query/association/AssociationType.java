package persistence.sql.ddl.query.association;

import jakarta.persistence.OneToMany;
import java.lang.annotation.Annotation;

public enum AssociationType {

    ONE_TO_MANY(OneToMany.class, new OneToManyAssociation()),
    ;

    Class<? extends Annotation> type;
    Association association;

    AssociationType(Class<? extends Annotation> type,
            Association association) {
        this.type = type;
        this.association = association;
    }

    public Class<? extends Annotation> type() {
        return this.type;
    }

    public Association association() {
        return this.association;
    }

}
