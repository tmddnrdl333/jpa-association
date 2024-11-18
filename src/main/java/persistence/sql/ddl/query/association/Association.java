package persistence.sql.ddl.query.association;

import java.lang.reflect.Field;
import persistence.sql.ddl.query.constraint.ForeignKeyConstraint;

public interface Association {

    ForeignKeyConstraint foreignKeyConstraint(Class<?> clazz, Field field);

}
