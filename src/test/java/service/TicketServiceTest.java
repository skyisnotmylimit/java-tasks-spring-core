package service;

import dao.EventDao;
import dao.TicketDao;
import dao.UserDao;
import model.Event;
import model.Ticket;
import model.User;
import model.impl.EventImpl;
import model.impl.TicketImpl;
import model.impl.UserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketDao ticketDao;
    @Mock
    private UserDao userDao;
    @Mock
    private EventDao eventDao;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
        ticketService.setTicketDao(ticketDao);
        ticketService.setUserDao(userDao);
        ticketService.setEventDao(eventDao);
    }

    @Test
    void bookTicketSavesTicketForExistingUserAndEvent() {
        when(userDao.getById(1)).thenReturn(new UserImpl(1, "John", "john@example.com"));
        when(eventDao.getById(2)).thenReturn(new EventImpl(2, "Concert", new Date()));
        when(ticketDao.isPlaceBooked(2, 10)).thenReturn(false);
        when(ticketDao.save(any())).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(99);
            return ticket;
        });

        Ticket booked = ticketService.bookTicket(1, 2, 10, Ticket.Category.PREMIUM);

        assertEquals(99, booked.getId());
        assertEquals(1, booked.getUserId());
        assertEquals(2, booked.getEventId());
        assertEquals(10, booked.getPlace());
        assertEquals(Ticket.Category.PREMIUM, booked.getCategory());
    }

    @Test
    void bookTicketThrowsIllegalStateWhenPlaceAlreadyBooked() {
        when(userDao.getById(1)).thenReturn(new UserImpl(1, "John", "john@example.com"));
        when(eventDao.getById(2)).thenReturn(new EventImpl(2, "Concert", new Date()));
        when(ticketDao.isPlaceBooked(2, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));
        verify(ticketDao, never()).save(any());
    }

    @Test
    void bookTicketRejectsUnknownUserOrEvent() {
        when(userDao.getById(1)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));

        when(userDao.getById(1)).thenReturn(new UserImpl(1, "John", "john@example.com"));
        when(eventDao.getById(2)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));
    }

    @Test
    void getBookedTicketsForUserSortedByEventDateDescending() {
        User user = new UserImpl(1, "John", "john@example.com");
        Ticket earlyTicket = new TicketImpl(1, 1, 1, Ticket.Category.STANDARD, 1);
        Ticket lateTicket = new TicketImpl(2, 2, 1, Ticket.Category.STANDARD, 2);
        when(ticketDao.getByUserId(1)).thenReturn(List.of(earlyTicket, lateTicket));
        when(eventDao.getById(1)).thenReturn(new EventImpl(1, "Early", dateOf(2026, Calendar.MAY, 1)));
        when(eventDao.getById(2)).thenReturn(new EventImpl(2, "Late", dateOf(2026, Calendar.JUNE, 1)));

        List<Ticket> tickets = ticketService.getBookedTickets(user, 10, 1);

        assertEquals(List.of(lateTicket, earlyTicket), tickets);
    }

    @Test
    void getBookedTicketsForEventSortedByUserEmailAscending() {
        Event event = new EventImpl(1, "Concert", new Date());
        Ticket ticketOfB = new TicketImpl(1, 1, 2, Ticket.Category.STANDARD, 1);
        Ticket ticketOfA = new TicketImpl(2, 1, 1, Ticket.Category.STANDARD, 2);
        when(ticketDao.getByEventId(1)).thenReturn(List.of(ticketOfB, ticketOfA));
        when(userDao.getById(1)).thenReturn(new UserImpl(1, "A", "a@example.com"));
        when(userDao.getById(2)).thenReturn(new UserImpl(2, "B", "b@example.com"));

        List<Ticket> tickets = ticketService.getBookedTickets(event, 10, 1);

        assertEquals(List.of(ticketOfA, ticketOfB), tickets);
    }

    @Test
    void getBookedTicketsForNullUserOrEventReturnsEmptyList() {
        assertTrue(ticketService.getBookedTickets((User) null, 10, 1).isEmpty());
        assertTrue(ticketService.getBookedTickets((Event) null, 10, 1).isEmpty());
    }

    @Test
    void cancelTicketDelegatesToDao() {
        when(ticketDao.delete(1)).thenReturn(true);

        assertTrue(ticketService.cancelTicket(1));
        verify(ticketDao).delete(1);
    }

    private static Date dateOf(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month, day, 19, 0, 0);
        return calendar.getTime();
    }
}
