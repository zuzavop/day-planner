package com.myplanner.app;

import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * date structure for saving data about plan
 */
public class Plan {
    private int id;
    private String name, time;
    private int year, month, day;

    // empty constructor
    public Plan() {
    }

    /**
     * set new plan with parameters
     *
     * @param i id
     * @param n name, not null or empty
     * @param t time (in format hh:mm:ss)
     * @param y year
     * @param m month
     * @param d day
     */
    Plan(int i, @NotNull String n, @NotNull String t, int y, int m, int d) {
        id = i;
        setValuesIfValid(n, (Objects.equals(t, "") ? t : t.substring(0, t.length() - 3)), y, m, d);
    }

    /**
     * set new plan with parameters
     *
     * @param i id
     * @param n name, not null and empty
     * @param t time (in format hh:mm:ss)
     * @param y year
     * @param m month
     * @param d day
     */
    Plan(int i, @NotNull String n, Time t, int y, int m, int d) {
        id = i;
        setValuesIfValid(n, (t != null ? t.toString() : ""), y, m, d);
    }

    /**
     * set or change parameters of plan
     *
     * @param n name, not null or empty
     * @param t time (in format hh:mm)
     * @param y year
     * @param m month
     * @param d day
     * @return true if parameters is valid
     */
    public boolean setValuesIfValid(@NotNull String n, @NotNull String t, int y, int m, int d) {
        if (n.length() == 0) { // is mandatory
            return false;
        } else if (!isValidDate(y, m, d)) {
            return false;
        } else if (!isValidTime(t)) {
            return false;
        }
        // set values
        name = n;
        year = y;
        month = m;
        day = d;
        time = t;
        return true;
    }

    /**
     * control if date is valid
     *
     * @param y year
     * @param m month
     * @param d day
     */
    private boolean isValidDate(int y, int m, int d) {
        //control that date is in correct format
        StringBuilder date = new StringBuilder();
        date.append(m).append("/").append(d);
        date.append("/").append(y);

        SimpleDateFormat sdfrmt = new SimpleDateFormat("MM/dd/yyyy");
        sdfrmt.setLenient(false);
        try {
            sdfrmt.parse(String.valueOf(date));
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * @param t time (in format hh:mm)
     * @return true if valid date (in correct format)
     */
    private boolean isValidTime(String t) {
        if (t.length() == 0) {
            return true;
        }
        try {
            LocalTime.parse(t);
            return true;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    /**
     * @return time if set or "ALL DAY" if empty
     */
    public String getTimeText() {
        return (time.equals("") ? "ALL DAY" : time);
    }

    public int getId() {
        return id;
    }

    /**
     * @return date in format d.mm.yyyy
     */
    public String getDate() {
        return day + "." + month + "." + year;
    }
}
