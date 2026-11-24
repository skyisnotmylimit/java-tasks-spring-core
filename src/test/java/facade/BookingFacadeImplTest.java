package facade;

import facade.impl.BookingFacadeImpl;
import model.Event;
import model.Ticket;
import model.User;
import model.UserAccount;
import model.impl.EventImpl;
import model.impl.TicketImpl;
import model.impl.UserAccountImpl;
import model.impl.UserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.EventService;
import service.TicketService;
import service.UserAccountService;
import service.UserService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingFacadeImplTest {

    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;
    @Mock
    private TicketService ticketService;
    @Mock
    private UserAccountService userAccountService;

    private BookingFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new BookingFacadeImpl(userService, eventService, ticketService, userAccountService);
    }

    @Test
    void userOperationsAreDelegatedToUserService() {
        User user = new UserImpl(1, "John", "john@example.com");
        when(userService.getUserById(1)).thenReturn(user);
        when(userService.getUserByEmail("john@example.com")).thenReturn(user);
        when(userService.getUsersByName("John", 10, 1)).thenReturn(List.of(user));
        when(userService.createUser(user)).thenReturn(user);
        when(userService.updateUser(user)).thenReturn(user);
        when(userService.deleteUser(1)).thenReturn(true);

        assertEquals(user, facade.getUserById(1));
        assertEquals(user, facade.getUserByEmail("john@example.com"));
        assertEquals(List.of(user), facade.getUsersByName("John", 10, 1));
        assertEquals(user, facade.createUser(user));
        assertEquals(user, facade.updateUser(user));
        assertTrue(facade.deleteUser(1));
    }

    @Test
    void eventOperationsAreDelegatedToEventService() {
        Date day = new Date();
        Event event = new EventImpl(1, "Concert", day, BigDecimal.TEN);
        when(eventService.getEventById(1)).thenReturn(event);
        when(eventService.getEventsByTitle("Concert", 10, 1)).thenReturn(List.of(event));
        when(eventService.getEventsForDay(day, 10, 1)).thenReturn(List.of(event));
        when(eventService.createEvent(event)).thenReturn(event);
        when(eventService.updateEvent(event)).thenReturn(event);
        when(eventService.deleteEvent(1)).thenReturn(true);

        assertEquals(event, facade.getEventById(1));
        assertEquals(List.of(event), facade.getEventsByTitle("Concert", 10, 1));
        assertEquals(List.of(event), facade.getEventsForDay(day, 10, 1));
        assertEquals(event, facade.createEvent(event));
        assertEquals(event, facade.updateEvent(event));
        assertTrue(facade.deleteEvent(1));
    }

    @Test
    void ticketOperationsAreDelegatedToTicketService() {
        User user = new UserImpl(1, "John", "john@example.com");
        Event event = new EventImpl(2, "Concert", new Date());
        Ticket ticket = new TicketImpl(3, 2, 1, Ticket.Category.BAR, 7);
        when(ticketService.bookTicket(1, 2, 7, Ticket.Category.BAR)).thenReturn(ticket);
        when(ticketService.getBookedTickets(user, 10, 1)).thenReturn(List.of(ticket));
        when(ticketService.getBookedTickets(event, 10, 1)).thenReturn(List.of(ticket));
        when(ticketService.cancelTicket(3)).thenReturn(true);

        assertEquals(ticket, facade.bookTicket(1, 2, 7, Ticket.Category.BAR));
        assertEquals(List.of(ticket), facade.getBookedTickets(user, 10, 1));
        assertEquals(List.of(ticket), facade.getBookedTickets(event, 10, 1));
        assertTrue(facade.cancelTicket(3));
        verify(ticketService).bookTicket(1, 2, 7, Ticket.Category.BAR);
    }

    @Test
    void accountOperationsAreDelegatedToUserAccountService() {
        UserAccount account = new UserAccountImpl(1, 1, new BigDecimal("40.00"));
        when(userAccountService.getAccountByUserId(1)).thenReturn(account);
        when(userAccountService.refillAccount(1, BigDecimal.TEN)).thenReturn(account);

        assertEquals(account, facade.getUserAccount(1));
        assertEquals(account, facade.refillAccount(1, BigDecimal.TEN));
        verify(userAccountService).refillAccount(1, BigDecimal.TEN);
    }
}
