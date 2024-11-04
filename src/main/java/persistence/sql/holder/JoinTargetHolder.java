package persistence.sql.holder;

import persistence.sql.ddl.impl.JoinTargetDefinition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JoinTargetHolder {
    private static final JoinTargetHolder INSTANCE = new JoinTargetHolder();

    private static final Map<Class<?>, Set<JoinTargetDefinition>> CONTEXT = new HashMap<>();

    private JoinTargetHolder() {
    }

    public static JoinTargetHolder getInstance() {
        return INSTANCE;
    }

    public void add(Class<?> clazz, JoinTargetDefinition definitions) {
        CONTEXT.computeIfAbsent(clazz, k -> new HashSet<>()).add(definitions);
    }

    public void add(JoinTargetDefinition definition) {
        add(definition.getTargetEntity(), definition);
    }

    public Set<JoinTargetDefinition> get(Class<?> clazz) {
        return CONTEXT.get(clazz);
    }
}
