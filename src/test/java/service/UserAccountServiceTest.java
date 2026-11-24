package service;

import dao.UserAccountDao;
import dao.UserDao;
import model.UserAccount;
import model.impl.UserAccountImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountDao userAccountDao;
    @Mock
    private UserDao userDao;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountService();
        userAccountService.setUserAccountDao(userAccountDao);
        userAccountService.setUserDao(userDao);
    }

    @Test
    void getAccountByUserIdReturnsAccountOrNull() {
        UserAccountImpl account = new UserAccountImpl(1, 1, BigDecimal.TEN);
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.of(account));
        when(userAccountDao.findByUserId(2)).thenReturn(Optional.empty());

        assertEquals(account, userAccountService.getAccountByUserId(1));
        assertNull(userAccountService.getAccountByUserId(2));
    }

    @Test
    void refillAccountAddsMoneyToExistingAccount() {
        UserAccountImpl account = new UserAccountImpl(1, 1, new BigDecimal("10.00"));
        when(userDao.existsById(1L)).thenReturn(true);
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.of(account));
        when(userAccountDao.save(account)).thenReturn(account);

        UserAccount refilled = userAccountService.refillAccount(1, new BigDecimal("15.50"));

        assertEquals(new BigDecimal("25.50"), refilled.getAmount());
    }

    @Test
    void refillAccountCreatesAccountWhenMissing() {
        when(userDao.existsById(1L)).thenReturn(true);
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.empty());
        when(userAccountDao.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount created = userAccountService.refillAccount(1, new BigDecimal("30.00"));

        ArgumentCaptor<UserAccountImpl> captor = ArgumentCaptor.forClass(UserAccountImpl.class);
        verify(userAccountDao).save(captor.capture());
        assertEquals(1, captor.getValue().getUserId());
        assertEquals(new BigDecimal("30.00"), created.getAmount());
    }

    @Test
    void refillAccountRejectsNonPositiveAmountAndUnknownUser() {
        assertThrows(IllegalArgumentException.class, () -> userAccountService.refillAccount(1, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> userAccountService.refillAccount(1, null));

        when(userDao.existsById(99L)).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> userAccountService.refillAccount(99, BigDecimal.TEN));
    }

    @Test
    void withdrawSubtractsMoney() {
        UserAccountImpl account = new UserAccountImpl(1, 1, new BigDecimal("50.00"));
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.of(account));

        userAccountService.withdrawFromAccount(1, new BigDecimal("20.00"));

        assertEquals(new BigDecimal("30.00"), account.getAmount());
        verify(userAccountDao).save(account);
    }

    @Test
    void withdrawThrowsOnInsufficientFunds() {
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.of(new UserAccountImpl(1, 1, new BigDecimal("5.00"))));

        assertThrows(IllegalStateException.class,
                () -> userAccountService.withdrawFromAccount(1, new BigDecimal("20.00")));
        verify(userAccountDao, never()).save(any());
    }

    @Test
    void withdrawThrowsWhenAccountMissing() {
        when(userAccountDao.findByUserId(1)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> userAccountService.withdrawFromAccount(1, BigDecimal.TEN));
    }

    @Test
    void withdrawIsNoOpForZeroOrNullAmount() {
        userAccountService.withdrawFromAccount(1, BigDecimal.ZERO);
        userAccountService.withdrawFromAccount(1, null);

        verify(userAccountDao, never()).findByUserId(1);
        verify(userAccountDao, never()).save(any());
    }
}
