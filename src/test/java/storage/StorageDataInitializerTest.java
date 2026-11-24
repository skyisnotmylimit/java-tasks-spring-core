package storage;

import model.impl.EventImpl;
import model.impl.TicketImpl;
import model.impl.UserImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StorageDataInitializerTest {

    @Test
    void populatesStorageBeanFromPreparedDataFile() {
        Storage storage = new Storage();
        StorageDataInitializer initializer = new StorageDataInitializer();
        initializer.setDataFilePath("classpath:prepared-data.json");

        initializer.postProcessAfterInitialization(storage, "storage");

        assertEquals(8, storage.size());
        assertInstanceOf(UserImpl.class, storage.get("user", 1));
        assertInstanceOf(EventImpl.class, storage.get("event", 1));
        assertInstanceOf(TicketImpl.class, storage.get("ticket", 1));
        assertNotNull(((EventImpl) storage.get("event", 1)).getDate());
        assertEquals(10, ((TicketImpl) storage.get("ticket", 1)).getPlace());
    }

    @Test
    void ignoresNonStorageBeans() {
        StorageDataInitializer initializer = new StorageDataInitializer();
        Object bean = new Object();

        assertEquals(bean, initializer.postProcessAfterInitialization(bean, "someBean"));
    }

    @Test
    void leavesStorageEmptyWhenFileIsMissing() {
        Storage storage = new Storage();
        StorageDataInitializer initializer = new StorageDataInitializer();
        initializer.setDataFilePath("classpath:does-not-exist.json");

        initializer.postProcessAfterInitialization(storage, "storage");

        assertEquals(0, storage.size());
    }

    @Test
    void failsOnMalformedDataFile() {
        Storage storage = new Storage();
        StorageDataInitializer initializer = new StorageDataInitializer();
        initializer.setDataFilePath("classpath:malformed-data.json");

        assertThrows(IllegalStateException.class,
                () -> initializer.postProcessAfterInitialization(storage, "storage"));
    }
}
