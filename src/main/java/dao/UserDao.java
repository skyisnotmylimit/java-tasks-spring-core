package dao;

import model.impl.UserImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

/**
 * DAO for {@link UserImpl} entities based on Spring Data {@link CrudRepository}.
 */
public interface UserDao extends CrudRepository<UserImpl, Long> {

    Optional<UserImpl> findByEmail(String email);

    List<UserImpl> findByNameContainingOrderByNameAsc(String name, Pageable pageable);
}
