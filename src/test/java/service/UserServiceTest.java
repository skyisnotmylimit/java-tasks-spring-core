package service;

import dao.UserDao;
import model.User;
import model.impl.UserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.setUserDao(userDao);
    }

    @Test
    void getUserByIdDelegatesToDao() {
        User user = new UserImpl(1, "John", "john@example.com");
        when(userDao.getById(1)).thenReturn(user);

        assertEquals(user, userService.getUserById(1));
    }

    @Test
    void getUserByEmailDelegatesToDao() {
        User user = new UserImpl(1, "John", "john@example.com");
        when(userDao.getByEmail("john@example.com")).thenReturn(user);

        assertEquals(user, userService.getUserByEmail("john@example.com"));
    }

    @Test
    void getUsersByNameDelegatesToDao() {
        List<User> users = List.of(new UserImpl(1, "John", "john@example.com"));
        when(userDao.getByName("John", 10, 1)).thenReturn(users);

        assertEquals(users, userService.getUsersByName("John", 10, 1));
    }

    @Test
    void createUserSavesNewUser() {
        User user = new UserImpl(0, "John", "john@example.com");
        when(userDao.getByEmail("john@example.com")).thenReturn(null);
        when(userDao.save(user)).thenReturn(new UserImpl(1, "John", "john@example.com"));

        User created = userService.createUser(user);

        assertEquals(1, created.getId());
        verify(userDao).save(user);
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userDao.getByEmail("john@example.com"))
                .thenReturn(new UserImpl(1, "Existing", "john@example.com"));

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(new UserImpl(0, "John", "john@example.com")));
        verify(userDao, never()).save(any());
    }

    @Test
    void createUserRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    void updateUserReturnsNullWhenNotFound() {
        User user = new UserImpl(42, "Ghost", "ghost@example.com");
        when(userDao.update(user)).thenReturn(null);

        assertNull(userService.updateUser(user));
    }

    @Test
    void deleteUserDelegatesToDao() {
        when(userDao.delete(1)).thenReturn(true);

        assertTrue(userService.deleteUser(1));
        verify(userDao).delete(1);
    }
}
