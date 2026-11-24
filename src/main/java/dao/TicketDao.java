package dao;

import model.impl.TicketImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * DAO for {@link TicketImpl} entities based on Spring Data {@link CrudRepository}.
 */
public interface TicketDao extends CrudRepository<TicketImpl, Long> {

    /**
     * Tickets of a user sorted by event date in descending order.
     */
    @Query("select t from TicketImpl t join EventImpl e on t.eventId = e.id "
            + "where t.userId = :userId order by e.date desc")
    List<TicketImpl> findByUserIdSortedByEventDateDesc(@Param("userId") long userId, Pageable pageable);

    /**
     * Tickets of an event sorted by user email in ascending order.
     */
    @Query("select t from TicketImpl t join UserImpl u on t.userId = u.id "
            + "where t.eventId = :eventId order by u.email asc")
    List<TicketImpl> findByEventIdSortedByUserEmailAsc(@Param("eventId") long eventId, Pageable pageable);

    boolean existsByEventIdAndPlace(long eventId, int place);
}
