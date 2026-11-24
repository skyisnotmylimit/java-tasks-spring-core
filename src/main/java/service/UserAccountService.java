package service;

import dao.UserAccountDao;
import dao.UserDao;
import model.UserAccount;
import model.impl.UserAccountImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service containing user account (prepaid money) related functionality.
 */
@Transactional
public class UserAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccountService.class);

    private UserAccountDao userAccountDao;
    private UserDao userDao;

    /**
     * DAOs are injected via autowiring (setter-based).
     */
    public void setUserAccountDao(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional(readOnly = true)
    public UserAccount getAccountByUserId(long userId) {
        LOGGER.debug("Getting account for user {}", userId);
        return userAccountDao.findByUserId(userId).orElse(null);
    }

    /**
     * Refills the account of the given user, creating the account if it does not exist yet.
     */
    public UserAccount refillAccount(long userId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Refill amount must be positive, got " + amount);
        }
        if (!userDao.existsById(userId)) {
            throw new IllegalArgumentException("User with id " + userId + " does not exist");
        }
        UserAccountImpl account = userAccountDao.findByUserId(userId)
                .orElseGet(() -> new UserAccountImpl(0, userId, BigDecimal.ZERO));
        account.setAmount(account.getAmount().add(amount));
        UserAccount refilled = userAccountDao.save(account);
        LOGGER.info("Refilled account of user {} by {}, new balance: {}", userId, amount, refilled.getAmount());
        return refilled;
    }

    /**
     * Withdraws the given amount from the user account.
     *
     * @throws IllegalStateException if the user has no account or not enough money.
     */
    public void withdrawFromAccount(long userId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            LOGGER.debug("Nothing to withdraw from account of user {} (amount={})", userId, amount);
            return;
        }
        UserAccountImpl account = userAccountDao.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "User " + userId + " has no prepaid account to pay " + amount));
        if (account.getAmount().compareTo(amount) < 0) {
            throw new IllegalStateException("User " + userId + " has insufficient funds: balance "
                    + account.getAmount() + ", required " + amount);
        }
        account.setAmount(account.getAmount().subtract(amount));
        userAccountDao.save(account);
        LOGGER.info("Withdrew {} from account of user {}, new balance: {}", amount, userId, account.getAmount());
    }
}
