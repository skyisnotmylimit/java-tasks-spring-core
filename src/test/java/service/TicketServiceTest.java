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
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
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
    @Mock
    private UserAccountService userAccountService;

    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
        ticketService.setTicketDao(ticketDao);
        ticketService.setUserDao(userDao);
        ticketService.setEventDao(eventDao);
        ticketService.setUserAccountService(userAccountService);
    }

    @Test
    void bookTicketWithdrawsTicketPriceAndSavesTicket() {
        BigDecimal price = new BigDecimal("50.00");
        when(userDao.existsById(1L)).thenReturn(true);
        when(eventDao.findById(2L)).thenReturn(Optional.of(new EventImpl(2, "Concert", new Date(), price)));
        when(ticketDao.existsByEventIdAndPlace(2, 10)).thenReturn(false);
        when(ticketDao.save(any())).thenAnswer(invocation -> {
            TicketImpl ticket = invocation.getArgument(0);
            ticket.setId(99);
            return ticket;
        });

        Ticket booked = ticketService.bookTicket(1, 2, 10, Ticket.Category.PREMIUM);

        verify(userAccountService).withdrawFromAccount(1, price);
        assertEquals(99, booked.getId());
        assertEquals(1, booked.getUserId());
        assertEquals(2, booked.getEventId());
        assertEquals(10, booked.getPlace());
    }

    @Test
    void bookTicketFailsWhenPlaceAlreadyBooked() {
        when(userDao.existsById(1L)).thenReturn(true);
        when(eventDao.findById(2L)).thenReturn(Optional.of(new EventImpl(2, "Concert", new Date())));
        when(ticketDao.existsByEventIdAndPlace(2, 10)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));
        verify(userAccountService, never()).withdrawFromAccount(anyLong(), any());
        verify(ticketDao, never()).save(any());
    }

    @Test
    void bookTicketFailsWhenWithdrawalFails() {
        when(userDao.existsById(1L)).thenReturn(true);
        when(eventDao.findById(2L)).thenReturn(
                Optional.of(new EventImpl(2, "Concert", new Date(), new BigDecimal("100.00"))));
        when(ticketDao.existsByEventIdAndPlace(2, 10)).thenReturn(false);
        doThrow(new IllegalStateException("insufficient funds"))
                .when(userAccountService).withdrawFromAccount(1, new BigDecimal("100.00"));

        assertThrows(IllegalStateException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));
        verify(ticketDao, never()).save(any());
    }

    @Test
    void bookTicketRejectsUnknownUserOrEvent() {
        when(userDao.existsById(1L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));

        when(userDao.existsById(1L)).thenReturn(true);
        when(eventDao.findById(2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.bookTicket(1, 2, 10, Ticket.Category.STANDARD));
    }

    @Test
    void getBookedTicketsForUserDelegatesToSortedQuery() {
        User user = new UserImpl(1, "John", "john@example.com");
        List<TicketImpl> tickets = List.of(new TicketImpl(1, 1, 1, Ticket.Category.STANDARD, 1));
        when(ticketDao.findByUserIdSortedByEventDateDesc(1, PageRequest.of(0, 10))).thenReturn(tickets);

        assertEquals(1, ticketService.getBookedTickets(user, 10, 1).size());
    }

    @Test
    void getBookedTicketsForEventDelegatesToSortedQuery() {
        Event event = new EventImpl(1, "Concert", new Date());
        List<TicketImpl> tickets = List.of(new TicketImpl(1, 1, 1, Ticket.Category.STANDARD, 1));
        when(ticketDao.findByEventIdSortedByUserEmailAsc(1, PageRequest.of(0, 10))).thenReturn(tickets);

        assertEquals(1, ticketService.getBookedTickets(event, 10, 1).size());
    }

    @Test
    void getBookedTicketsForNullUserOrEventReturnsEmptyList() {
        assertTrue(ticketService.getBookedTickets((User) null, 10, 1).isEmpty());
        assertTrue(ticketService.getBookedTickets((Event) null, 10, 1).isEmpty());
    }

    @Test
    void cancelTicketDelegatesToDao() {
        when(ticketDao.existsById(1L)).thenReturn(true);
        when(ticketDao.existsById(2L)).thenReturn(false);

        assertTrue(ticketService.cancelTicket(1));
        verify(ticketDao).deleteById(1L);
        assertFalse(ticketService.cancelTicket(2));
    }
}
