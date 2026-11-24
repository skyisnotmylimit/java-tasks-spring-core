package storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.impl.EventImpl;
import model.impl.TicketImpl;
import model.impl.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Spring {@link BeanPostProcessor} which initializes the {@link Storage} bean with
 * prepared data from a JSON file during application start.
 * <p>
 * The path to the file is set via a property placeholder resolved from an external
 * property file (see {@code application.properties}).
 */
public class StorageDataInitializer implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageDataInitializer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String dataFilePath;

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Storage storage) {
            LOGGER.info("Initializing storage bean '{}' with prepared data from '{}'", beanName, dataFilePath);
            loadData(storage);
        }
        return bean;
    }

    private void loadData(Storage storage) {
        if (dataFilePath == null || dataFilePath.isBlank()) {
            LOGGER.warn("No data file configured, storage will start empty");
            return;
        }
        Resource resource = new DefaultResourceLoader().getResource(dataFilePath);
        if (!resource.exists()) {
            LOGGER.warn("Prepared data file '{}' not found, storage will start empty", dataFilePath);
            return;
        }
        try (InputStream inputStream = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(inputStream);
            int loaded = 0;
            for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                Object entity = toEntity(entry.getKey(), entry.getValue());
                if (entity != null) {
                    storage.putRaw(entry.getKey(), entity);
                    loaded++;
                }
            }
            LOGGER.info("Loaded {} entities into storage from '{}'", loaded, dataFilePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prepared data from " + dataFilePath, e);
        }
    }

    private Object toEntity(String key, JsonNode node) throws IOException {
        if (key.startsWith(Storage.USER_NAMESPACE + ":")) {
            return objectMapper.treeToValue(node, UserImpl.class);
        }
        if (key.startsWith(Storage.EVENT_NAMESPACE + ":")) {
            return objectMapper.treeToValue(node, EventImpl.class);
        }
        if (key.startsWith(Storage.TICKET_NAMESPACE + ":")) {
            return objectMapper.treeToValue(node, TicketImpl.class);
        }
        LOGGER.warn("Unknown namespace for key '{}', entry skipped", key);
        return null;
    }
}
