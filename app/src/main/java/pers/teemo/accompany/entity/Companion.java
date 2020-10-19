package pers.teemo.accompany.entity;

import java.time.LocalDate;

public class Companion {

    private LocalDate startDate;
    private LocalDate birthdayDate;

    public LocalDate getStartDate() {
        return startDate;
    }

    public Companion setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getBirthdayDate() {
        return birthdayDate;
    }

    public Companion setBirthdayDate(LocalDate birthdayDate) {
        this.birthdayDate = birthdayDate;
        return this;
    }
}
