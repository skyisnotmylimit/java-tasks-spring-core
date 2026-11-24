package model.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import model.Event;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * Default implementation of {@link Event} mapped with Hibernate ORM.
 */
@Entity
@Table(name = "events")
public class EventImpl implements Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_date", nullable = false)
    private Date date;

    @Column(name = "ticket_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal ticketPrice = BigDecimal.ZERO;

    public EventImpl() {
    }

    public EventImpl(long id, String title, Date date) {
        this(id, title, date, BigDecimal.ZERO);
    }

    public EventImpl(long id, String title, Date date, BigDecimal ticketPrice) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.ticketPrice = ticketPrice;
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
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    @Override
    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventImpl event)) {
            return false;
        }
        return id == event.id && Objects.equals(title, event.title) && Objects.equals(date, event.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, date);
    }

    @Override
    public String toString() {
        return "Event{id=" + id + ", title='" + title + "', date=" + date + ", ticketPrice=" + ticketPrice + "}";
    }
}
