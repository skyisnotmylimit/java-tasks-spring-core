package dao;

import model.impl.UserAccountImpl;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * DAO for {@link UserAccountImpl} entities based on Spring Data {@link CrudRepository}.
 */
public interface UserAccountDao extends CrudRepository<UserAccountImpl, Long> {

    Optional<UserAccountImpl> findByUserId(long userId);
}
