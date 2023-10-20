package com.pro4d.quickmc.util;

public class TimeFormat {

    private static final int DAYS_IN_SECOND = 86400;

    public static String getTime(long remaining){
        int days = toDays(remaining);
        int hours = toHours(remaining);
        int minutes = toMinutes(remaining);
        int seconds = toSeconds(remaining);
        StringBuilder builder = new StringBuilder();

        // Add days if it's not 0
        if(days != 0) builder.append(days).append("d ");

        // Add hours if it's not 0
        if(hours != 0) builder.append(hours).append("h ");

        // Add minutes if it's not 0
        if(minutes != 0) builder.append(minutes).append("m ");

        // Add seconds if it's not 0
        if(seconds != 0) builder.append(seconds).append("s");

        // If the string is empty, add 'Ready' text
        if(builder.isEmpty()) builder.append("Ready");

        return builder.toString();
    }

    private static int toDays(long remaining){
        return (int) (remaining / DAYS_IN_SECOND);
    }

    private static int toHours(long remaining){
        return (int) ((remaining % DAYS_IN_SECOND) / 3600);
    }

    private static int toMinutes(long remaining){
        return (int) (((remaining % DAYS_IN_SECOND) % 3600) / 60);
    }

    private static int toSeconds(long remaining){
        return (int) (((remaining % DAYS_IN_SECOND) % 3600) % 60);
    }

}
