package service;

import dao.UserDao;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service containing user related functionality.
 */
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private UserDao userDao;

    /**
     * DAO is injected via autowiring (setter-based).
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUserById(long userId) {
        LOGGER.debug("Getting user by id {}", userId);
        return userDao.getById(userId);
    }

    public User getUserByEmail(String email) {
        LOGGER.debug("Getting user by email '{}'", email);
        return userDao.getByEmail(email);
    }

    public List<User> getUsersByName(String name, int pageSize, int pageNum) {
        LOGGER.debug("Getting users by name '{}' (pageSize={}, pageNum={})", name, pageSize, pageNum);
        return userDao.getByName(name, pageSize, pageNum);
    }

    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getEmail() != null && userDao.getByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("User with email '" + user.getEmail() + "' already exists");
        }
        User created = userDao.save(user);
        LOGGER.info("Created user: {}", created);
        return created;
    }

    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        User updated = userDao.update(user);
        if (updated == null) {
            LOGGER.warn("Cannot update user with id {}: not found", user.getId());
        } else {
            LOGGER.info("Updated user: {}", updated);
        }
        return updated;
    }

    public boolean deleteUser(long userId) {
        boolean deleted = userDao.delete(userId);
        LOGGER.info("Deleting user with id {}: {}", userId, deleted ? "deleted" : "not found");
        return deleted;
    }
}
