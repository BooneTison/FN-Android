package com.example.fn_android;

import androidx.annotation.NonNull;

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
            this.dayOrder = Integer.parseInt(dayOrder);
            this.day = day;
            this.startHour = 0;
            this.startMin = 0;
            this.endHour = 0;
            this.endMin = 0;
            meal = "null";
        }
        else {
            this.startHour = Integer.parseInt(start.substring(0, 2));
            this.startMin = Integer.parseInt(start.substring(3, 5));
            this.endHour = Integer.parseInt(end.substring(0, 2));
            this.endMin = Integer.parseInt(end.substring(3, 5));
            this.dayOrder = Integer.parseInt(dayOrder);
            this.day = day;
            meal = "null";
        }
    }

    public HoursTime(String start, String end, String dayOrder, String day, String meal) {
        if (start.equals("null")) {
            this.dayOrder = Integer.parseInt(dayOrder);
            this.day = day;
            this.startHour = 0;
            this.startMin = 0;
            this.endHour = 0;
            this.endMin = 0;
            this.meal = meal;
        }
        else {
            this.startHour = Integer.parseInt(start.substring(0, 2));
            this.startMin = Integer.parseInt(start.substring(3, 5));
            this.endHour = Integer.parseInt(end.substring(0, 2));
            this.endMin = Integer.parseInt(end.substring(3, 5));
            this.dayOrder = Integer.parseInt(dayOrder);
            this.day = day;
            this.meal = meal;
        }
    }

    public String startM() {
        if (startHour < 12) return "am";
        return "pm";
    }

    public String endM() {
        if (endHour < 12) return "am";
        return "pm";
    }

    public int to12Hour(int time) {
        if (time > 12) return time-12;
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
                return day + " from " + to12Hour(startHour) + ":" + minConvert(startMin) + startM()
                        + " to " + to12Hour(endHour) + ":" + minConvert(endMin) + endM();
            }
            return meal + " from " + to12Hour(startHour) + ":" + minConvert(startMin) + startM()
                    + " to " + to12Hour(endHour) + ":" + minConvert(endMin) + endM();
        }
        return "Closed " + day;
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

        return false;
    }
}
