package pers.teemo.accompany.entity;

import java.time.LocalDate;

public class Event {
    private LocalDate startDate;
    private String eventName;

    public LocalDate getStartDate() {
        return startDate;
    }

    public Event setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEventName() {
        return eventName;
    }

    public Event setEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }
}
