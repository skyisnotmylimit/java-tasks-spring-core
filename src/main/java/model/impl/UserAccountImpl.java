package model.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import model.UserAccount;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Default implementation of {@link UserAccount} mapped with Hibernate ORM.
 */
@Entity
@Table(name = "user_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_accounts_user", columnNames = "user_id"))
public class UserAccountImpl implements UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    public UserAccountImpl() {
    }

    public UserAccountImpl(long id, long userId, BigDecimal amount) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserAccountImpl account)) {
            return false;
        }
        return id == account.id && userId == account.userId
                && (amount == null ? account.amount == null
                        : account.amount != null && amount.compareTo(account.amount) == 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId);
    }

    @Override
    public String toString() {
        return "UserAccount{id=" + id + ", userId=" + userId + ", amount=" + amount + "}";
    }
}
