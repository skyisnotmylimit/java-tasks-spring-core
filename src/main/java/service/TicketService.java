package service;

import dao.EventDao;
import dao.TicketDao;
import dao.UserDao;
import model.Event;
import model.Ticket;
import model.User;
import model.impl.TicketImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Service containing ticket booking related functionality.
 */
public class TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    private TicketDao ticketDao;
    private UserDao userDao;
    private EventDao eventDao;

    /**
     * DAOs are injected via autowiring (setter-based).
     */
    public void setTicketDao(TicketDao ticketDao) {
        this.ticketDao = ticketDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        LOGGER.debug("Booking ticket: userId={}, eventId={}, place={}, category={}", userId, eventId, place, category);
        if (userDao.getById(userId) == null) {
            throw new IllegalArgumentException("User with id " + userId + " does not exist");
        }
        if (eventDao.getById(eventId) == null) {
            throw new IllegalArgumentException("Event with id " + eventId + " does not exist");
        }
        if (ticketDao.isPlaceBooked(eventId, place)) {
            throw new IllegalStateException(
                    "Place " + place + " has already been booked for event " + eventId);
        }
        Ticket ticket = new TicketImpl(0, eventId, userId, category, place);
        Ticket booked = ticketDao.save(ticket);
        LOGGER.info("Booked ticket: {}", booked);
        return booked;
    }

    /**
     * Tickets are sorted by event date in descending order.
     */
    public List<Ticket> getBookedTickets(User user, int pageSize, int pageNum) {
        if (user == null) {
            return List.of();
        }
        LOGGER.debug("Getting booked tickets for user {} (pageSize={}, pageNum={})", user.getId(), pageSize, pageNum);
        List<Ticket> tickets = ticketDao.getByUserId(user.getId()).stream()
                .sorted(Comparator.comparing(this::eventDateOf,
                        Comparator.nullsLast(Comparator.<Date>naturalOrder().reversed())))
                .toList();
        return paginate(tickets, pageSize, pageNum);
    }

    /**
     * Tickets are sorted by user email in ascending order.
     */
    public List<Ticket> getBookedTickets(Event event, int pageSize, int pageNum) {
        if (event == null) {
            return List.of();
        }
        LOGGER.debug("Getting booked tickets for event {} (pageSize={}, pageNum={})", event.getId(), pageSize, pageNum);
        List<Ticket> tickets = ticketDao.getByEventId(event.getId()).stream()
                .sorted(Comparator.comparing(this::userEmailOf,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        return paginate(tickets, pageSize, pageNum);
    }

    public boolean cancelTicket(long ticketId) {
        boolean cancelled = ticketDao.delete(ticketId);
        LOGGER.info("Cancelling ticket with id {}: {}", ticketId, cancelled ? "cancelled" : "not found");
        return cancelled;
    }

    private Date eventDateOf(Ticket ticket) {
        Event event = eventDao.getById(ticket.getEventId());
        return event == null ? null : event.getDate();
    }

    private String userEmailOf(Ticket ticket) {
        User user = userDao.getById(ticket.getUserId());
        return user == null ? null : user.getEmail();
    }

    private static List<Ticket> paginate(List<Ticket> tickets, int pageSize, int pageNum) {
        if (pageSize <= 0 || pageNum <= 0) {
            throw new IllegalArgumentException(
                    "pageSize and pageNum must be positive, got pageSize=" + pageSize + ", pageNum=" + pageNum);
        }
        return tickets.stream()
                .skip((long) (pageNum - 1) * pageSize)
                .limit(pageSize)
                .toList();
    }
}
