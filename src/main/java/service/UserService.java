package service;

import dao.UserDao;
import model.User;
import model.impl.UserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service containing user related functionality.
 */
@Transactional
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private UserDao userDao;

    /**
     * DAO is injected via autowiring (setter-based).
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional(readOnly = true)
    public User getUserById(long userId) {
        LOGGER.debug("Getting user by id {}", userId);
        return userDao.findById(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        LOGGER.debug("Getting user by email '{}'", email);
        return userDao.findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByName(String name, int pageSize, int pageNum) {
        LOGGER.debug("Getting users by name '{}' (pageSize={}, pageNum={})", name, pageSize, pageNum);
        return List.copyOf(userDao.findByNameContainingOrderByNameAsc(name, PageRequest.of(pageNum - 1, pageSize)));
    }

    public User createUser(User user) {
        UserImpl entity = toEntity(user);
        if (entity.getEmail() != null && userDao.findByEmail(entity.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with email '" + entity.getEmail() + "' already exists");
        }
        User created = userDao.save(entity);
        LOGGER.info("Created user: {}", created);
        return created;
    }

    public User updateUser(User user) {
        UserImpl entity = toEntity(user);
        if (!userDao.existsById(entity.getId())) {
            LOGGER.warn("Cannot update user with id {}: not found", entity.getId());
            return null;
        }
        User updated = userDao.save(entity);
        LOGGER.info("Updated user: {}", updated);
        return updated;
    }

    public boolean deleteUser(long userId) {
        if (!userDao.existsById(userId)) {
            LOGGER.info("Deleting user with id {}: not found", userId);
            return false;
        }
        userDao.deleteById(userId);
        LOGGER.info("Deleted user with id {}", userId);
        return true;
    }

    private static UserImpl toEntity(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (!(user instanceof UserImpl entity)) {
            throw new IllegalArgumentException("Unsupported user implementation: " + user.getClass());
        }
        return entity;
    }
}
