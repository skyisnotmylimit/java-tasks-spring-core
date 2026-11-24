package service;

import dao.EventDao;
import model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Service containing event related functionality.
 */
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private EventDao eventDao;

    /**
     * DAO is injected via autowiring (setter-based).
     */
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public Event getEventById(long eventId) {
        LOGGER.debug("Getting event by id {}", eventId);
        return eventDao.getById(eventId);
    }

    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        LOGGER.debug("Getting events by title '{}' (pageSize={}, pageNum={})", title, pageSize, pageNum);
        return eventDao.getByTitle(title, pageSize, pageNum);
    }

    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        LOGGER.debug("Getting events for day {} (pageSize={}, pageNum={})", day, pageSize, pageNum);
        return eventDao.getForDay(day, pageSize, pageNum);
    }

    public Event createEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        Event created = eventDao.save(event);
        LOGGER.info("Created event: {}", created);
        return created;
    }

    public Event updateEvent(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        Event updated = eventDao.update(event);
        if (updated == null) {
            LOGGER.warn("Cannot update event with id {}: not found", event.getId());
        } else {
            LOGGER.info("Updated event: {}", updated);
        }
        return updated;
    }

    public boolean deleteEvent(long eventId) {
        boolean deleted = eventDao.delete(eventId);
        LOGGER.info("Deleting event with id {}: {}", eventId, deleted ? "deleted" : "not found");
        return deleted;
    }
}
