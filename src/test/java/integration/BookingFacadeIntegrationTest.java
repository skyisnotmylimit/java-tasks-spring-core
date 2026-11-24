package integration;

import facade.BookingFacade;
import model.Event;
import model.Ticket;
import model.User;
import model.impl.EventImpl;
import model.impl.UserImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests which instantiate the Application Context directly,
 * look up the facade bean and perform real-life scenarios.
 */
class BookingFacadeIntegrationTest {

    private ClassPathXmlApplicationContext context;
    private BookingFacade facade;

    @BeforeEach
    void setUp() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        facade = context.getBean("bookingFacade", BookingFacade.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void storageIsInitializedWithPreparedDataFromFile() {
        User preloadedUser = facade.getUserById(1);
        Event preloadedEvent = facade.getEventById(1);

        assertNotNull(preloadedUser);
        assertEquals("john.doe@example.com", preloadedUser.getEmail());
        assertNotNull(preloadedEvent);
        assertEquals("Spring Concert", preloadedEvent.getTitle());
        assertEquals(1, facade.getBookedTickets(preloadedUser, 10, 1).size());
    }

    @Test
    void realLifeScenarioCreateUserCreateEventBookAndCancelTicket() {
        // Create user
        User user = facade.createUser(new UserImpl(0, "Alice Wonder", "alice.wonder@example.com"));
        assertTrue(user.getId() > 0);
        assertEquals(user.getId(), facade.getUserByEmail("alice.wonder@example.com").getId());

        // Create event
        Event event = facade.createEvent(new EventImpl(0, "Integration Gala", dateOf(2026, Calendar.JULY, 20)));
        assertTrue(event.getId() > 0);
        assertFalse(facade.getEventsByTitle("Integration Gala", 10, 1).isEmpty());

        // Book ticket for this event for the user
        Ticket ticket = facade.bookTicket(user.getId(), event.getId(), 42, Ticket.Category.PREMIUM);
        assertTrue(ticket.getId() > 0);
        assertEquals(user.getId(), ticket.getUserId());
        assertEquals(event.getId(), ticket.getEventId());

        List<Ticket> userTickets = facade.getBookedTickets(user, 10, 1);
        assertEquals(1, userTickets.size());
        assertEquals(ticket.getId(), userTickets.get(0).getId());

        List<Ticket> eventTickets = facade.getBookedTickets(event, 10, 1);
        assertEquals(1, eventTickets.size());

        // Booking the same place again fails
        assertThrows(IllegalStateException.class,
                () -> facade.bookTicket(user.getId(), event.getId(), 42, Ticket.Category.STANDARD));

        // Cancel the ticket
        assertTrue(facade.cancelTicket(ticket.getId()));
        assertTrue(facade.getBookedTickets(user, 10, 1).isEmpty());
        assertFalse(facade.cancelTicket(ticket.getId()));
    }

    @Test
    void updateAndDeleteScenario() {
        User user = facade.createUser(new UserImpl(0, "Temp User", "temp.user@example.com"));
        user.setName("Renamed User");
        assertEquals("Renamed User", facade.updateUser(user).getName());
        assertEquals(1, facade.getUsersByName("Renamed", 10, 1).size());
        assertTrue(facade.deleteUser(user.getId()));

        Event event = facade.createEvent(new EventImpl(0, "Temp Event", dateOf(2026, Calendar.AUGUST, 1)));
        event.setTitle("Renamed Event");
        assertEquals("Renamed Event", facade.updateEvent(event).getTitle());
        assertEquals(1, facade.getEventsForDay(dateOf(2026, Calendar.AUGUST, 1), 10, 1).size());
        assertTrue(facade.deleteEvent(event.getId()));
    }

    @Test
    void bookedTicketsForUserAreSortedByEventDateDescending() {
        User user = facade.createUser(new UserImpl(0, "Sorter", "sorter@example.com"));
        Event earlyEvent = facade.createEvent(new EventImpl(0, "Early Event", dateOf(2026, Calendar.MARCH, 1)));
        Event lateEvent = facade.createEvent(new EventImpl(0, "Late Event", dateOf(2026, Calendar.DECEMBER, 1)));

        facade.bookTicket(user.getId(), earlyEvent.getId(), 1, Ticket.Category.STANDARD);
        facade.bookTicket(user.getId(), lateEvent.getId(), 1, Ticket.Category.STANDARD);

        List<Ticket> tickets = facade.getBookedTickets(user, 10, 1);

        assertEquals(2, tickets.size());
        assertEquals(lateEvent.getId(), tickets.get(0).getEventId());
        assertEquals(earlyEvent.getId(), tickets.get(1).getEventId());
    }

    private static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, 19, 0, 0);
        return calendar.getTime();
    }
}
