package persistence.proxy;

import org.jetbrains.annotations.NotNull;
import persistence.entity.EntityLazyLoader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class PersistentList<T> implements List<T>, LazyInitializer, InvocationHandler {
    private List<T> target;
    private boolean initialized = false;
    private final Object owner;
    private final EntityLazyLoader lazyLoader;

    public PersistentList(Object owner,
                          EntityLazyLoader lazyLoader) {
        this.owner = owner;
        this.target = null;
        this.lazyLoader = lazyLoader;
    }

    public void initialize() {
        target = (List<T>) lazyLoader.loadLazyCollection(owner);
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
    public boolean isInitialized() {
        return initialized;
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
    public Iterator<T> iterator() {
        return target.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return target.toArray(a);
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
    public boolean containsAll(@NotNull Collection<?> c) {
        return new HashSet<>(target).containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return target.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return target.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return target.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return target.retainAll(c);
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

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return target.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return target.listIterator(index);
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return List.of();
    }
}
