package model;

import java.math.BigDecimal;

/**
 * Stores the amount of prepaid money a user has in the system,
 * which is used during the booking procedure.
 */
public interface UserAccount {
    /**
     * Account Id. UNIQUE.
     * @return Account Id.
     */
    long getId();
    void setId(long id);

    /**
     * Id of the user owning the account. UNIQUE.
     * @return User Id.
     */
    long getUserId();
    void setUserId(long userId);

    /**
     * Amount of prepaid money.
     * @return Prepaid amount.
     */
    BigDecimal getAmount();
    void setAmount(BigDecimal amount);
}
