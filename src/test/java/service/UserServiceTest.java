package service;

import dao.UserDao;
import model.User;
import model.impl.UserImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void getUserByIdReturnsUserOrNull() {
        UserImpl user = new UserImpl(1, "John", "john@example.com");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.findById(2L)).thenReturn(Optional.empty());

        assertEquals(user, userService.getUserById(1));
        assertNull(userService.getUserById(2));
    }

    @Test
    void getUserByEmailDelegatesToDao() {
        UserImpl user = new UserImpl(1, "John", "john@example.com");
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertEquals(user, userService.getUserByEmail("john@example.com"));
    }

    @Test
    void getUsersByNameUsesPagination() {
        List<UserImpl> users = List.of(new UserImpl(1, "John", "john@example.com"));
        when(userDao.findByNameContainingOrderByNameAsc("John", PageRequest.of(1, 5))).thenReturn(users);

        List<User> result = userService.getUsersByName("John", 5, 2);

        assertEquals(1, result.size());
        assertEquals(users.get(0), result.get(0));
    }

    @Test
    void createUserSavesNewUser() {
        UserImpl user = new UserImpl(0, "John", "john@example.com");
        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userDao.save(user)).thenReturn(new UserImpl(1, "John", "john@example.com"));

        User created = userService.createUser(user);

        assertEquals(1, created.getId());
        verify(userDao).save(user);
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        when(userDao.findByEmail("john@example.com"))
                .thenReturn(Optional.of(new UserImpl(1, "Existing", "john@example.com")));

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
        when(userDao.existsById(42L)).thenReturn(false);

        assertNull(userService.updateUser(new UserImpl(42, "Ghost", "ghost@example.com")));
        verify(userDao, never()).save(any());
    }

    @Test
    void deleteUserReturnsFalseWhenMissing() {
        when(userDao.existsById(1L)).thenReturn(true);
        when(userDao.existsById(2L)).thenReturn(false);

        assertTrue(userService.deleteUser(1));
        verify(userDao).deleteById(1L);
        assertFalse(userService.deleteUser(2));
    }
}
