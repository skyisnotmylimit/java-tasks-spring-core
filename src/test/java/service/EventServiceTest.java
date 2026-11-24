package service;

import dao.EventDao;
import model.Event;
import model.impl.EventImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventDao eventDao;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService();
        eventService.setEventDao(eventDao);
    }

    @Test
    void getEventByIdReturnsEventOrNull() {
        EventImpl event = new EventImpl(1, "Concert", new Date(), BigDecimal.TEN);
        when(eventDao.findById(1L)).thenReturn(Optional.of(event));
        when(eventDao.findById(2L)).thenReturn(Optional.empty());

        assertEquals(event, eventService.getEventById(1));
        assertNull(eventService.getEventById(2));
    }

    @Test
    void getEventsByTitleUsesPagination() {
        List<EventImpl> events = List.of(new EventImpl(1, "Concert", new Date()));
        when(eventDao.findByTitleContainingOrderByTitleAsc("Concert", PageRequest.of(0, 10))).thenReturn(events);

        List<Event> result = eventService.getEventsByTitle("Concert", 10, 1);

        assertEquals(1, result.size());
    }

    @Test
    void getEventsForDayQueriesWholeDayRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.MAY, 1, 19, 0, 0);
        Date day = calendar.getTime();
        when(eventDao.findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(any(), any(), eq(PageRequest.of(0, 10))))
                .thenReturn(List.of(new EventImpl(1, "Concert", day)));

        List<Event> result = eventService.getEventsForDay(day, 10, 1);

        assertEquals(1, result.size());
        ArgumentCaptor<Date> fromCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> toCaptor = ArgumentCaptor.forClass(Date.class);
        verify(eventDao).findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
                fromCaptor.capture(), toCaptor.capture(), eq(PageRequest.of(0, 10)));
        assertEquals(24L * 60 * 60 * 1000, toCaptor.getValue().getTime() - fromCaptor.getValue().getTime());
        assertTrue(fromCaptor.getValue().getTime() <= day.getTime());
    }

    @Test
    void getEventsForDayWithNullDayReturnsEmptyList() {
        assertTrue(eventService.getEventsForDay(null, 10, 1).isEmpty());
    }

    @Test
    void createEventSavesEvent() {
        EventImpl event = new EventImpl(0, "Concert", new Date(), new BigDecimal("25.00"));
        when(eventDao.save(event)).thenReturn(new EventImpl(1, "Concert", event.getDate(), event.getTicketPrice()));

        Event created = eventService.createEvent(event);

        assertEquals(1, created.getId());
        assertEquals(new BigDecimal("25.00"), created.getTicketPrice());
    }

    @Test
    void createEventRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(null));
    }

    @Test
    void updateEventReturnsNullWhenNotFound() {
        when(eventDao.existsById(42L)).thenReturn(false);

        assertNull(eventService.updateEvent(new EventImpl(42, "Ghost", new Date())));
    }

    @Test
    void deleteEventDelegatesToDao() {
        when(eventDao.existsById(1L)).thenReturn(true);

        assertTrue(eventService.deleteEvent(1));
        verify(eventDao).deleteById(1L);
    }
}
