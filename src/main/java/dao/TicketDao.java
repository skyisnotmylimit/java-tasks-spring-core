package dao;

import model.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.Storage;

import java.util.List;

/**
 * DAO for {@link Ticket} entities stored under the {@code ticket} namespace.
 */
public class TicketDao extends AbstractDao<Ticket> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketDao.class);

    @Override
    protected String getNamespace() {
        return Storage.TICKET_NAMESPACE;
    }

    @Override
    protected Class<Ticket> getEntityClass() {
        return Ticket.class;
    }

    @Override
    protected long getEntityId(Ticket entity) {
        return entity.getId();
    }

    @Override
    protected void setEntityId(Ticket entity, long id) {
        entity.setId(id);
    }

    public List<Ticket> getByUserId(long userId) {
        LOGGER.debug("Looking up tickets for user {}", userId);
        return getAll().stream()
                .filter(ticket -> ticket.getUserId() == userId)
                .toList();
    }

    public List<Ticket> getByEventId(long eventId) {
        LOGGER.debug("Looking up tickets for event {}", eventId);
        return getAll().stream()
                .filter(ticket -> ticket.getEventId() == eventId)
                .toList();
    }

    public boolean isPlaceBooked(long eventId, int place) {
        return getAll().stream()
                .anyMatch(ticket -> ticket.getEventId() == eventId && ticket.getPlace() == place);
    }
}
