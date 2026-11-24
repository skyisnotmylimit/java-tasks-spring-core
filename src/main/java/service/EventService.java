package service;

import dao.EventDao;
import model.Event;
import model.impl.EventImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service containing event related functionality.
 */
@Transactional
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private EventDao eventDao;

    /**
     * DAO is injected via autowiring (setter-based).
     */
    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Transactional(readOnly = true)
    public Event getEventById(long eventId) {
        LOGGER.debug("Getting event by id {}", eventId);
        return eventDao.findById(eventId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        LOGGER.debug("Getting events by title '{}' (pageSize={}, pageNum={})", title, pageSize, pageNum);
        return List.copyOf(eventDao.findByTitleContainingOrderByTitleAsc(title, PageRequest.of(pageNum - 1, pageSize)));
    }

    @Transactional(readOnly = true)
    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        LOGGER.debug("Getting events for day {} (pageSize={}, pageNum={})", day, pageSize, pageNum);
        if (day == null) {
            return List.of();
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDate targetDay = day.toInstant().atZone(zone).toLocalDate();
        Date from = Date.from(targetDay.atStartOfDay(zone).toInstant());
        Date to = Date.from(targetDay.plusDays(1).atStartOfDay(zone).toInstant());
        return List.copyOf(eventDao.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
                from, to, PageRequest.of(pageNum - 1, pageSize)));
    }

    public Event createEvent(Event event) {
        Event created = eventDao.save(toEntity(event));
        LOGGER.info("Created event: {}", created);
        return created;
    }

    public Event updateEvent(Event event) {
        EventImpl entity = toEntity(event);
        if (!eventDao.existsById(entity.getId())) {
            LOGGER.warn("Cannot update event with id {}: not found", entity.getId());
            return null;
        }
        Event updated = eventDao.save(entity);
        LOGGER.info("Updated event: {}", updated);
        return updated;
    }

    public boolean deleteEvent(long eventId) {
        if (!eventDao.existsById(eventId)) {
            LOGGER.info("Deleting event with id {}: not found", eventId);
            return false;
        }
        eventDao.deleteById(eventId);
        LOGGER.info("Deleted event with id {}", eventId);
        return true;
    }

    private static EventImpl toEntity(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (!(event instanceof EventImpl entity)) {
            throw new IllegalArgumentException("Unsupported event implementation: " + event.getClass());
        }
        return entity;
    }
}
