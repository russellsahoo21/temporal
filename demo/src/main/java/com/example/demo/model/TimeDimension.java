package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "dim_time")
public class TimeDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "calendar_date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "year_val")
    private Integer year;

    @Column(name = "quarter_val")
    private Integer quarter;

    @Column(name = "month_val")
    private Integer month;

    @Column(name = "month_name")
    private String monthName;

    @Column(name = "day_val")
    private Integer day;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "is_weekend")
    private Boolean isWeekend;

    @Column(name = "season")
    private String season;

    public TimeDimension() {}

    public TimeDimension(LocalDate date) {
        this.date = date;
        this.year = date.getYear();
        this.month = date.getMonthValue();
        this.monthName = date.getMonth().name();
        this.day = date.getDayOfMonth();
        this.dayOfWeek = date.getDayOfWeek().name();
        this.isWeekend = date.getDayOfWeek().getValue() >= 6;
        this.quarter = (date.getMonthValue() - 1) / 3 + 1;
        
        switch (this.quarter) {
            case 1 -> this.season = "Winter";
            case 2 -> this.season = "Spring";
            case 3 -> this.season = "Summer";
            default -> this.season = "Fall";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Boolean getIsWeekend() {
        return isWeekend;
    }

    public void setIsWeekend(Boolean weekend) {
        isWeekend = weekend;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }
}
