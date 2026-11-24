package dao;

import model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.Storage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * DAO for {@link Event} entities stored under the {@code event} namespace.
 */
public class EventDao extends AbstractDao<Event> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDao.class);

    @Override
    protected String getNamespace() {
        return Storage.EVENT_NAMESPACE;
    }

    @Override
    protected Class<Event> getEntityClass() {
        return Event.class;
    }

    @Override
    protected long getEntityId(Event entity) {
        return entity.getId();
    }

    @Override
    protected void setEntityId(Event entity, long id) {
        entity.setId(id);
    }

    public List<Event> getByTitle(String title, int pageSize, int pageNum) {
        LOGGER.debug("Looking up events by title containing '{}' (pageSize={}, pageNum={})", title, pageSize, pageNum);
        List<Event> matched = getAll().stream()
                .filter(event -> event.getTitle() != null && title != null && event.getTitle().contains(title))
                .sorted(comparing(Event::getTitle))
                .toList();
        return paginate(matched, pageSize, pageNum);
    }

    public List<Event> getForDay(Date day, int pageSize, int pageNum) {
        LOGGER.debug("Looking up events for day {} (pageSize={}, pageNum={})", day, pageSize, pageNum);
        if (day == null) {
            return List.of();
        }
        LocalDate targetDay = toLocalDate(day);
        List<Event> matched = getAll().stream()
                .filter(event -> event.getDate() != null && toLocalDate(event.getDate()).equals(targetDay))
                .sorted(comparing(Event::getDate))
                .toList();
        return paginate(matched, pageSize, pageNum);
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
