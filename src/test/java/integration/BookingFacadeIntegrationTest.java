package integration;

import facade.BookingFacade;
import model.Event;
import model.Ticket;
import model.User;
import model.UserAccount;
import model.impl.EventImpl;
import model.impl.UserImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests which instantiate the Application Context directly,
 * look up the facade bean and perform real-life scenarios against the database.
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
    void databaseIsInitializedWithPreparedData() {
        User preloadedUser = facade.getUserById(1);
        Event preloadedEvent = facade.getEventById(1);
        UserAccount preloadedAccount = facade.getUserAccount(1);

        assertNotNull(preloadedUser);
        assertEquals("john.doe@example.com", preloadedUser.getEmail());
        assertNotNull(preloadedEvent);
        assertEquals("Spring Concert", preloadedEvent.getTitle());
        assertEquals(0, preloadedEvent.getTicketPrice().compareTo(new BigDecimal("50.00")));
        assertNotNull(preloadedAccount);
        assertEquals(0, preloadedAccount.getAmount().compareTo(new BigDecimal("100.00")));
        assertEquals(1, facade.getBookedTickets(preloadedUser, 10, 1).size());
    }

    @Test
    void realLifeScenarioWithAccountRefillBookingAndCancellation() {
        // Create user
        User user = facade.createUser(new UserImpl(0, "Alice Wonder", "alice.wonder@example.com"));
        assertTrue(user.getId() > 0);
        assertNull(facade.getUserAccount(user.getId()));

        // Refill account
        UserAccount account = facade.refillAccount(user.getId(), new BigDecimal("100.00"));
        assertEquals(0, account.getAmount().compareTo(new BigDecimal("100.00")));

        // Create event with ticket price
        Event event = facade.createEvent(
                new EventImpl(0, "Integration Gala", dateOf(2026, Calendar.JULY, 20), new BigDecimal("60.00")));
        assertTrue(event.getId() > 0);

        // Book ticket: money must be withdrawn according to ticketPrice
        Ticket ticket = facade.bookTicket(user.getId(), event.getId(), 42, Ticket.Category.PREMIUM);
        assertTrue(ticket.getId() > 0);
        assertEquals(0, facade.getUserAccount(user.getId()).getAmount().compareTo(new BigDecimal("40.00")));

        List<Ticket> userTickets = facade.getBookedTickets(user, 10, 1);
        assertEquals(1, userTickets.size());
        assertEquals(ticket.getId(), userTickets.get(0).getId());

        // Same place cannot be booked twice
        assertThrows(IllegalStateException.class,
                () -> facade.bookTicket(user.getId(), event.getId(), 42, Ticket.Category.STANDARD));

        // Insufficient funds for another expensive booking
        assertThrows(IllegalStateException.class,
                () -> facade.bookTicket(user.getId(), event.getId(), 43, Ticket.Category.STANDARD));

        // Cancel the ticket
        assertTrue(facade.cancelTicket(ticket.getId()));
        assertTrue(facade.getBookedTickets(user, 10, 1).isEmpty());
        assertFalse(facade.cancelTicket(ticket.getId()));
    }

    @Test
    void bookingFailsWithoutAccountAndSucceedsForFreeEvent() {
        User user = facade.createUser(new UserImpl(0, "No Money", "no.money@example.com"));
        Event paidEvent = facade.createEvent(
                new EventImpl(0, "Paid Event", dateOf(2026, Calendar.SEPTEMBER, 5), new BigDecimal("10.00")));
        Event freeEvent = facade.createEvent(
                new EventImpl(0, "Free Event", dateOf(2026, Calendar.SEPTEMBER, 6), BigDecimal.ZERO));

        // No account -> cannot pay
        assertThrows(IllegalStateException.class,
                () -> facade.bookTicket(user.getId(), paidEvent.getId(), 1, Ticket.Category.STANDARD));

        // Free event can be booked without an account
        Ticket freeTicket = facade.bookTicket(user.getId(), freeEvent.getId(), 1, Ticket.Category.STANDARD);
        assertTrue(freeTicket.getId() > 0);
    }

    @Test
    void refillValidation() {
        User user = facade.createUser(new UserImpl(0, "Refill Guy", "refill.guy@example.com"));

        assertThrows(IllegalArgumentException.class, () -> facade.refillAccount(user.getId(), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> facade.refillAccount(9999, BigDecimal.TEN));

        facade.refillAccount(user.getId(), new BigDecimal("5.00"));
        facade.refillAccount(user.getId(), new BigDecimal("7.50"));
        assertEquals(0, facade.getUserAccount(user.getId()).getAmount().compareTo(new BigDecimal("12.50")));
    }

    @Test
    void updateAndDeleteScenario() {
        User user = facade.createUser(new UserImpl(0, "Temp User", "temp.user@example.com"));
        user.setName("Renamed User");
        assertEquals("Renamed User", facade.updateUser(user).getName());
        assertEquals(1, facade.getUsersByName("Renamed", 10, 1).size());
        assertTrue(facade.deleteUser(user.getId()));

        Event event = facade.createEvent(
                new EventImpl(0, "Temp Event", dateOf(2026, Calendar.AUGUST, 1), new BigDecimal("15.00")));
        event.setTitle("Renamed Event");
        assertEquals("Renamed Event", facade.updateEvent(event).getTitle());
        assertEquals(1, facade.getEventsForDay(dateOf(2026, Calendar.AUGUST, 1), 10, 1).size());
        assertTrue(facade.deleteEvent(event.getId()));
    }

    @Test
    void bookedTicketsForUserAreSortedByEventDateDescending() {
        User user = facade.createUser(new UserImpl(0, "Sorter", "sorter@example.com"));
        facade.refillAccount(user.getId(), new BigDecimal("100.00"));
        Event earlyEvent = facade.createEvent(
                new EventImpl(0, "Early Event", dateOf(2026, Calendar.MARCH, 1), BigDecimal.TEN));
        Event lateEvent = facade.createEvent(
                new EventImpl(0, "Late Event", dateOf(2026, Calendar.DECEMBER, 1), BigDecimal.TEN));

        facade.bookTicket(user.getId(), earlyEvent.getId(), 1, Ticket.Category.STANDARD);
        facade.bookTicket(user.getId(), lateEvent.getId(), 1, Ticket.Category.STANDARD);

        List<Ticket> tickets = facade.getBookedTickets(user, 10, 1);

        assertEquals(2, tickets.size());
        assertEquals(lateEvent.getId(), tickets.get(0).getEventId());
        assertEquals(earlyEvent.getId(), tickets.get(1).getEventId());
    }

    @Test
    void bookedTicketsForEventAreSortedByUserEmailAscending() {
        Event event = facade.createEvent(
                new EventImpl(0, "Email Sort Event", dateOf(2026, Calendar.OCTOBER, 10), BigDecimal.TEN));
        User userB = facade.createUser(new UserImpl(0, "User B", "zz.last@example.com"));
        User userA = facade.createUser(new UserImpl(0, "User A", "aa.first@example.com"));
        facade.refillAccount(userA.getId(), new BigDecimal("50.00"));
        facade.refillAccount(userB.getId(), new BigDecimal("50.00"));

        facade.bookTicket(userB.getId(), event.getId(), 1, Ticket.Category.STANDARD);
        facade.bookTicket(userA.getId(), event.getId(), 2, Ticket.Category.STANDARD);

        List<Ticket> tickets = facade.getBookedTickets(event, 10, 1);

        assertEquals(2, tickets.size());
        assertEquals(userA.getId(), tickets.get(0).getUserId());
        assertEquals(userB.getId(), tickets.get(1).getUserId());
    }

    private static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, 19, 0, 0);
        return calendar.getTime();
    }
}
