package sample.persistence.association;

import java.lang.reflect.Field;
import sample.persistence.constraint.ForeignKeyConstraint;

public interface Association {

    ForeignKeyConstraint foreignKeyConstraint(Class<?> clazz, Field field);

}
