package dao;

import model.impl.EventImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

/**
 * DAO for {@link EventImpl} entities based on Spring Data {@link CrudRepository}.
 */
public interface EventDao extends CrudRepository<EventImpl, Long> {

    List<EventImpl> findByTitleContainingOrderByTitleAsc(String title, Pageable pageable);

    List<EventImpl> findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(Date from, Date to, Pageable pageable);
}
