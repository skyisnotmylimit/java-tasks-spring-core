package model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import model.Event;

import java.util.Date;
import java.util.Objects;

/**
 * Default implementation of {@link Event}.
 */
public class EventImpl implements Event {

    private long id;
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "UTC")
    private Date date;

    public EventImpl() {
    }

    public EventImpl(long id, String title, Date date) {
        this.id = id;
        this.title = title;
        this.date = date;
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
        return "Event{id=" + id + ", title='" + title + "', date=" + date + "}";
    }
}
