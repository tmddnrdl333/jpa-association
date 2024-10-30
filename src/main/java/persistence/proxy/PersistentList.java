package persistence.proxy;

import org.jetbrains.annotations.NotNull;
import persistence.entity.EntityLoader;
import persistence.sql.definition.EntityTableMapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PersistentList<T> implements PersistentCollection, List<T>, InvocationHandler {
    private List<T> target;
    private final Class<T> elementType;
    private boolean initialized = false;
    private final Object owner;
    private final EntityLoader entityLoader;

    public PersistentList(Object owner, EntityLoader entityLoader, Class<T> elementType) {
        this.owner = owner;
        this.target = null;
        this.elementType = elementType;
        this.entityLoader = entityLoader;
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return target.contains(o);
    }

    @NotNull
    @Override
    public Iterator iterator() {
        return target.stream().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public boolean add(T t) {
        return target.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return target.remove(o);
    }

    @Override
    public boolean addAll(@NotNull Collection c) {
        return target.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection c) {
        return target.addAll(index, c);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public T get(int index) {
        return target.get(index);
    }

    @Override
    public T set(int index, T element) {
        return target.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        target.add(index, element);
    }

    @Override
    public T remove(int index) {
        return target.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return target.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return target.lastIndexOf(o);
    }

    @Override
    public ListIterator listIterator() {
        return target.listIterator();
    }

    @Override
    public ListIterator listIterator(int index) {
        return target.listIterator(index);
    }

    @NotNull
    @Override
    public List subList(int fromIndex, int toIndex) {
        return target.subList(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        return target.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        return target.removeAll(c);
    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        return target.containsAll(c);
    }

    @NotNull
    @Override
    public Object[] toArray(@NotNull Object[] a) {
        return target.toArray(a);
    }

    public void initialize() {
        EntityTableMapper ownerTableMapper = new EntityTableMapper(owner);
        target = (List<T>) entityLoader.loadLazyCollection(elementType, ownerTableMapper);
        initialized = true;
    }

    @Override
    public Object getImplementation() {
        if (!initialized) {
            initialize();
        }
        return target;
    }

    @Override
    public Object getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!initialized) {
            initialize();
        }

        if (method.getName().equals("getImplementation")) {
            return getImplementation();
        }

        return method.invoke(target, args);
    }
}
