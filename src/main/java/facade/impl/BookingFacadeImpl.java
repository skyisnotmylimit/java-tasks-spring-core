package facade.impl;

import facade.BookingFacade;
import model.Event;
import model.Ticket;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EventService;
import service.TicketService;
import service.UserService;

import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link BookingFacade} which delegates method calls
 * to {@link UserService}, {@link EventService} and {@link TicketService}.
 * <p>
 * Services are injected using constructor-based injection.
 */
public class BookingFacadeImpl implements BookingFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingFacadeImpl.class);

    private final UserService userService;
    private final EventService eventService;
    private final TicketService ticketService;

    public BookingFacadeImpl(UserService userService, EventService eventService, TicketService ticketService) {
        this.userService = userService;
        this.eventService = eventService;
        this.ticketService = ticketService;
        LOGGER.info("BookingFacade initialized");
    }

    @Override
    public Event getEventById(long eventId) {
        LOGGER.debug("getEventById({})", eventId);
        return eventService.getEventById(eventId);
    }

    @Override
    public List<Event> getEventsByTitle(String title, int pageSize, int pageNum) {
        LOGGER.debug("getEventsByTitle('{}', {}, {})", title, pageSize, pageNum);
        return eventService.getEventsByTitle(title, pageSize, pageNum);
    }

    @Override
    public List<Event> getEventsForDay(Date day, int pageSize, int pageNum) {
        LOGGER.debug("getEventsForDay({}, {}, {})", day, pageSize, pageNum);
        return eventService.getEventsForDay(day, pageSize, pageNum);
    }

    @Override
    public Event createEvent(Event event) {
        LOGGER.debug("createEvent({})", event);
        return eventService.createEvent(event);
    }

    @Override
    public Event updateEvent(Event event) {
        LOGGER.debug("updateEvent({})", event);
        return eventService.updateEvent(event);
    }

    @Override
    public boolean deleteEvent(long eventId) {
        LOGGER.debug("deleteEvent({})", eventId);
        return eventService.deleteEvent(eventId);
    }

    @Override
    public User getUserById(long userId) {
        LOGGER.debug("getUserById({})", userId);
        return userService.getUserById(userId);
    }

    @Override
    public User getUserByEmail(String email) {
        LOGGER.debug("getUserByEmail('{}')", email);
        return userService.getUserByEmail(email);
    }

    @Override
    public List<User> getUsersByName(String name, int pageSize, int pageNum) {
        LOGGER.debug("getUsersByName('{}', {}, {})", name, pageSize, pageNum);
        return userService.getUsersByName(name, pageSize, pageNum);
    }

    @Override
    public User createUser(User user) {
        LOGGER.debug("createUser({})", user);
        return userService.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        LOGGER.debug("updateUser({})", user);
        return userService.updateUser(user);
    }

    @Override
    public boolean deleteUser(long userId) {
        LOGGER.debug("deleteUser({})", userId);
        return userService.deleteUser(userId);
    }

    @Override
    public Ticket bookTicket(long userId, long eventId, int place, Ticket.Category category) {
        LOGGER.debug("bookTicket({}, {}, {}, {})", userId, eventId, place, category);
        return ticketService.bookTicket(userId, eventId, place, category);
    }

    @Override
    public List<Ticket> getBookedTickets(User user, int pageSize, int pageNum) {
        LOGGER.debug("getBookedTickets(user={}, {}, {})", user, pageSize, pageNum);
        return ticketService.getBookedTickets(user, pageSize, pageNum);
    }

    @Override
    public List<Ticket> getBookedTickets(Event event, int pageSize, int pageNum) {
        LOGGER.debug("getBookedTickets(event={}, {}, {})", event, pageSize, pageNum);
        return ticketService.getBookedTickets(event, pageSize, pageNum);
    }

    @Override
    public boolean cancelTicket(long ticketId) {
        LOGGER.debug("cancelTicket({})", ticketId);
        return ticketService.cancelTicket(ticketId);
    }
}
