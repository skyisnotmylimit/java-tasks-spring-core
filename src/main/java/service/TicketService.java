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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service containing ticket booking related functionality.
 */
@Transactional
public class TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    private TicketDao ticketDao;
    private UserDao userDao;
    private EventDao eventDao;
    private UserAccountService userAccountService;

    /**
     * Dependencies are injected via autowiring (setter-based).
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

    public void setUserAccountService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * Books a ticket withdrawing the event ticket price from the user account.
     * The whole operation is performed in a single transaction.
     */
    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        LOGGER.debug("Booking ticket: userId={}, eventId={}, place={}, category={}", userId, eventId, place, category);
        if (!userDao.existsById(userId)) {
            throw new IllegalArgumentException("User with id " + userId + " does not exist");
        }
        Event event = eventDao.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " does not exist"));
        if (ticketDao.existsByEventIdAndPlace(eventId, place)) {
            throw new IllegalStateException("Place " + place + " has already been booked for event " + eventId);
        }
        userAccountService.withdrawFromAccount(userId, event.getTicketPrice());
        Ticket booked = ticketDao.save(new TicketImpl(0, eventId, userId, category, place));
        LOGGER.info("Booked ticket: {}", booked);
        return booked;
    }

    /**
     * Tickets are sorted by event date in descending order.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getBookedTickets(User user, int pageSize, int pageNum) {
        if (user == null) {
            return List.of();
        }
        LOGGER.debug("Getting booked tickets for user {} (pageSize={}, pageNum={})", user.getId(), pageSize, pageNum);
        return List.copyOf(ticketDao.findByUserIdSortedByEventDateDesc(
                user.getId(), PageRequest.of(pageNum - 1, pageSize)));
    }

    /**
     * Tickets are sorted by user email in ascending order.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getBookedTickets(Event event, int pageSize, int pageNum) {
        if (event == null) {
            return List.of();
        }
        LOGGER.debug("Getting booked tickets for event {} (pageSize={}, pageNum={})", event.getId(), pageSize, pageNum);
        return List.copyOf(ticketDao.findByEventIdSortedByUserEmailAsc(
                event.getId(), PageRequest.of(pageNum - 1, pageSize)));
    }

    public boolean cancelTicket(long ticketId) {
        if (!ticketDao.existsById(ticketId)) {
            LOGGER.info("Cancelling ticket with id {}: not found", ticketId);
            return false;
        }
        ticketDao.deleteById(ticketId);
        LOGGER.info("Cancelled ticket with id {}", ticketId);
        return true;
    }
}
