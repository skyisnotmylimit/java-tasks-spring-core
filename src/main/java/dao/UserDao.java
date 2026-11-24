package dao;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.Storage;

import java.util.List;
import java.util.Objects;

/**
 * DAO for {@link User} entities stored under the {@code user} namespace.
 */
public class UserDao extends AbstractDao<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    @Override
    protected String getNamespace() {
        return Storage.USER_NAMESPACE;
    }

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    protected long getEntityId(User entity) {
        return entity.getId();
    }

    @Override
    protected void setEntityId(User entity, long id) {
        entity.setId(id);
    }

    public User getByEmail(String email) {
        LOGGER.debug("Looking up user by email '{}'", email);
        return getAll().stream()
                .filter(user -> Objects.equals(user.getEmail(), email))
                .findFirst()
                .orElse(null);
    }

    public List<User> getByName(String name, int pageSize, int pageNum) {
        LOGGER.debug("Looking up users by name containing '{}' (pageSize={}, pageNum={})", name, pageSize, pageNum);
        List<User> matched = getAll().stream()
                .filter(user -> user.getName() != null && name != null && user.getName().contains(name))
                .sorted(comparing(User::getName))
                .toList();
        return paginate(matched, pageSize, pageNum);
    }
}
