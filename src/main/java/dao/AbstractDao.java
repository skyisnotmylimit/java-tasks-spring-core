package dao;

import storage.Storage;

import java.util.Comparator;
import java.util.List;

/**
 * Base class for DAO objects working with the common in-memory {@link Storage}.
 *
 * @param <T> entity type
 */
public abstract class AbstractDao<T> {

    protected Storage storage;

    /**
     * Storage is injected in a setter-based way.
     */
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    protected abstract String getNamespace();

    protected abstract Class<T> getEntityClass();

    protected abstract long getEntityId(T entity);

    protected abstract void setEntityId(T entity, long id);

    public T getById(long id) {
        Object entity = storage.get(getNamespace(), id);
        return entity == null ? null : getEntityClass().cast(entity);
    }

    public List<T> getAll() {
        return storage.getAll(getNamespace()).stream()
                .map(getEntityClass()::cast)
                .toList();
    }

    /**
     * Saves a new entity assigning it an auto-generated id.
     */
    public synchronized T save(T entity) {
        long nextId = getAll().stream()
                .mapToLong(this::getEntityId)
                .max()
                .orElse(0L) + 1;
        setEntityId(entity, nextId);
        storage.put(getNamespace(), nextId, entity);
        return entity;
    }

    /**
     * Updates an existing entity. Returns {@code null} if the entity does not exist.
     */
    public T update(T entity) {
        long id = getEntityId(entity);
        if (storage.get(getNamespace(), id) == null) {
            return null;
        }
        storage.put(getNamespace(), id, entity);
        return entity;
    }

    public boolean delete(long id) {
        return storage.remove(getNamespace(), id) != null;
    }

    /**
     * Applies 1-based pagination to a list.
     */
    protected static <E> List<E> paginate(List<E> items, int pageSize, int pageNum) {
        if (pageSize <= 0 || pageNum <= 0) {
            throw new IllegalArgumentException(
                    "pageSize and pageNum must be positive, got pageSize=" + pageSize + ", pageNum=" + pageNum);
        }
        return items.stream()
                .skip((long) (pageNum - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }

    protected static <E, K extends Comparable<K>> Comparator<E> comparing(java.util.function.Function<E, K> extractor) {
        return Comparator.comparing(extractor, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
