package storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Common in-memory storage for all domain entities.
 * <p>
 * Entities are stored in a single java map under separate namespaces, e.g.
 * {@code {"ticket:12345" -> Ticket{id=12345, place=23, ...}}}.
 */
public class Storage {

    private static final Logger LOGGER = LoggerFactory.getLogger(Storage.class);

    public static final String USER_NAMESPACE = "user";
    public static final String EVENT_NAMESPACE = "event";
    public static final String TICKET_NAMESPACE = "ticket";

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public Object get(String namespace, long id) {
        return data.get(key(namespace, id));
    }

    public void put(String namespace, long id, Object entity) {
        String key = key(namespace, id);
        LOGGER.debug("Storing entity under key '{}': {}", key, entity);
        data.put(key, entity);
    }

    public Object remove(String namespace, long id) {
        String key = key(namespace, id);
        LOGGER.debug("Removing entity under key '{}'", key);
        return data.remove(key);
    }

    /**
     * Lists all entities stored under the given namespace.
     */
    public List<Object> getAll(String namespace) {
        String prefix = namespace + ":";
        return data.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * Raw access to the underlying map (used by the data initializer).
     */
    public void putRaw(String key, Object entity) {
        LOGGER.debug("Storing entity under key '{}': {}", key, entity);
        data.put(key, entity);
    }

    public int size() {
        return data.size();
    }

    public void clear() {
        LOGGER.debug("Clearing storage");
        data.clear();
    }

    private static String key(String namespace, long id) {
        return namespace + ":" + id;
    }
}
