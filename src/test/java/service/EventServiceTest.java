package service;

import dao.EventDao;
import model.Event;
import model.impl.EventImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void getEventByIdDelegatesToDao() {
        Event event = new EventImpl(1, "Concert", new Date());
        when(eventDao.getById(1)).thenReturn(event);

        assertEquals(event, eventService.getEventById(1));
    }

    @Test
    void getEventsByTitleDelegatesToDao() {
        List<Event> events = List.of(new EventImpl(1, "Concert", new Date()));
        when(eventDao.getByTitle("Concert", 10, 1)).thenReturn(events);

        assertEquals(events, eventService.getEventsByTitle("Concert", 10, 1));
    }

    @Test
    void getEventsForDayDelegatesToDao() {
        Date day = new Date();
        List<Event> events = List.of(new EventImpl(1, "Concert", day));
        when(eventDao.getForDay(day, 10, 1)).thenReturn(events);

        assertEquals(events, eventService.getEventsForDay(day, 10, 1));
    }

    @Test
    void createEventSavesEvent() {
        Event event = new EventImpl(0, "Concert", new Date());
        when(eventDao.save(event)).thenReturn(new EventImpl(1, "Concert", event.getDate()));

        Event created = eventService.createEvent(event);

        assertEquals(1, created.getId());
        verify(eventDao).save(event);
    }

    @Test
    void createEventRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(null));
    }

    @Test
    void deleteEventDelegatesToDao() {
        when(eventDao.delete(1)).thenReturn(true);

        assertTrue(eventService.deleteEvent(1));
        verify(eventDao).delete(1);
    }
}
