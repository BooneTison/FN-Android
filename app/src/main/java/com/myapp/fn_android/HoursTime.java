package com.myapp.fn_android;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class HoursTime implements Comparable<HoursTime>{
    private final int startHour;
    private final int startMin;
    private final int endHour;
    private final int endMin;
    private final int dayOrder;
    private final String day;
    private final String meal;

    public HoursTime(String start, String end, String dayOrder, String day) {
        if (start.equals("null")) {
            this.startHour = 0;
            this.startMin = 0;
            this.endHour = 0;
            this.endMin = 0;
        }
        else {
            this.startHour = Integer.parseInt(start.substring(0, 2));
            this.startMin = Integer.parseInt(start.substring(3, 5));
            this.endHour = Integer.parseInt(end.substring(0, 2));
            this.endMin = Integer.parseInt(end.substring(3, 5));
        }
        this.dayOrder = Integer.parseInt(dayOrder);
        this.day = day;
        meal = "null";
    }

    public HoursTime(String start, String end, String dayOrder, String day, String meal) {
        if (start.equals("null")) {
            this.startHour = 0;
            this.startMin = 0;
            this.endHour = 0;
            this.endMin = 0;
        }
        else {
            this.startHour = Integer.parseInt(start.substring(0, 2));
            this.startMin = Integer.parseInt(start.substring(3, 5));
            this.endHour = Integer.parseInt(end.substring(0, 2));
            this.endMin = Integer.parseInt(end.substring(3, 5));

        }
        this.dayOrder = Integer.parseInt(dayOrder);
        this.day = day;
        this.meal = meal;
    }

    public HoursTime(String start, String end) {
        this.startHour = Integer.parseInt(start.substring(0, 2));
        this.startMin = Integer.parseInt(start.substring(3, 5));
        this.endHour = Integer.parseInt(end.substring(0, 2));
        this.endMin = Integer.parseInt(end.substring(3, 5));
        this.dayOrder = 0;
        this.day = "null";
        this.meal = "null";
    }

    public String startAMPM() {
        if (startHour < 12) return "am";
        return "pm";
    }

    public String endAMPM() {
        if (endHour < 12) return "am";
        return "pm";
    }

    public int to12Hour(int time) {
        if (time > 12) return time-12;
        if (time == 0) return 12;
        return time;
    }

    public String minConvert(int time) {
        if (time == 0) return "OO";
        return String.valueOf(time);
    }

    @NonNull
    public String toString() {
        if (startHour != 0) {
            if (meal.equals("null")) {
                return day + " from " + to12Hour(startHour) + ":" + minConvert(startMin) + startAMPM()
                        + " to " + to12Hour(endHour) + ":" + minConvert(endMin) + endAMPM();
            }
            return meal + " from " + to12Hour(startHour) + ":" + minConvert(startMin) + startAMPM()
                    + " to " + to12Hour(endHour) + ":" + minConvert(endMin) + endAMPM();
        }
        return "Closed " + day;
    }

    @NonNull
    public String toStringHoursOnly() {
        return to12Hour(startHour) + ":" + minConvert(startMin) + startAMPM() + " - " +
                to12Hour(endHour) + ":" + minConvert(endMin) + endAMPM();
    }

    @Override
    public int compareTo(HoursTime o) {
        if (this.dayOrder < o.dayOrder) return -1; // Day less
        else if (this.dayOrder > o.dayOrder) return 1; // Day more
        else { // Same day
            if (this.startHour < o.startHour) return -1; // Time less
            else if (this.startHour > o.startHour) return 1; // Time greater
            else if (this.startMin < o.startMin) return -1; // Equal hours, less min
            else if (this.startMin > o.startMin) return 1; // Equal hours, greater min
            return 0; // Equal time
        }
    }

    public boolean isPlaceOpen(HoursTime restHours) {
        // The time of this should have the same start and end
        if (this.startHour != this.endHour || this.startMin != this.endMin)
            return false;
        if (restHours.meal.equals("null")) // Has days, check time and days
            return isInDayRange(this.day, restHours.day)
                    && isInTimeRange(restHours.startHour,restHours.startMin,restHours.endHour,restHours.endMin);
        else // Everyday, only check time
            return isInTimeRange(restHours.startHour,restHours.startMin,restHours.endHour,restHours.endMin);
    }

    private boolean isInDayRange(String day, String range) {
        if (!range.contains("-"))
            return day.equals(range);
        else {
            List<String> arr = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
            int dayIndex = arr.indexOf(day);
            int startIndex = arr.indexOf(range.substring(0,3));
            int endIndex = arr.indexOf(range.substring(4));
            return dayIndex >= startIndex && dayIndex <= endIndex;
        }
    }

    private boolean isInTimeRange(int sHour, int sMin, int eHour, int eMin) {
        if (this.startHour == sHour) return this.startMin >= sMin; // Same hour as open, check minutes
        if (this.startHour == eHour) return this.startMin <= eMin; // Same hour as close, check minutes
        return this.startHour > sHour && this.startHour < eHour; // Different hour, check hours
    }

    public int withinHour(HoursTime restHours) {
        // The time of this should have the same start and end
        if (this.startHour != this.endHour || this.startMin != this.endMin)
            return 0;
        if (!restHours.meal.equals("null")) { // Open everyday
            if (hourBeforeOpenClose(restHours.startHour,restHours.startMin)) { // Before opening
                double result = (1 - (calculateTimeLeft(restHours.startHour,restHours.startMin)/60.0)) * 60;
                return (int) result;
            }
            else if (hourBeforeOpenClose(restHours.endHour,restHours.endMin)) { // Before closing
                double result = (1 - (calculateTimeLeft(restHours.endHour,restHours.endMin)/60.0)) * 60;
                return (int) result;
            }
        }
        else {
            if (!isInDayRange(this.day, restHours.day)) return 0; //Not same day
            // Same day then
            if (hourBeforeOpenClose(restHours.startHour,restHours.startMin)) { // Before opening
                double result = (1 - (calculateTimeLeft(restHours.startHour,restHours.startMin)/60.0)) * 60;
                return (int) result;
            }
            else if (hourBeforeOpenClose(restHours.endHour,restHours.endMin)) { // Before closing
                double result = (1 - (calculateTimeLeft(restHours.endHour,restHours.endMin)/60.0)) * 60;
                return (int) result;
            }
        }
        return 0; // Not within an hour
    }

    private boolean hourBeforeOpenClose(int hour, int min) {
        if (this.startHour > hour) return false; // Already open or closed now
        double currTime = this.startHour + (this.startMin/60.0);
        double otherTime = hour + (min/60.0);
        return !(otherTime - currTime > 1) && !(otherTime - currTime < 0); // More than an hour or Less than an hour
    }

    private int calculateTimeLeft(int hour, int min) {
        double currTime = this.startHour + (this.startMin/60.0);
        double otherTime = hour + (min/60.0);
        return (int) ((otherTime - currTime) * 60);
    }

    public boolean isWithinHour(HoursTime restHours) {
        return hourBeforeOpenClose(restHours.startHour,restHours.startMin) || hourBeforeOpenClose(restHours.endHour,restHours.endMin);
    }
}
