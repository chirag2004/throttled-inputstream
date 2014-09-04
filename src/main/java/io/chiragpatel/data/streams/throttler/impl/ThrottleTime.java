package io.chiragpatel.data.streams.throttler.impl;

import org.joda.time.DateTime;

/**
 * Created by Software Psychology
 * User: softpsy
 * Date: 8/30/14
 * Time: 6:59 PM
 */
public class ThrottleTime {
    private int hour;
    private int minutes;
    private int seconds;


    public ThrottleTime(int hour, int minutes, int seconds) {
        this.seconds = seconds;
        this.minutes = minutes;
        this.hour = hour;

    }
    public boolean after(long timeMillis){
        return new DateTime().withTime(hour,minutes,seconds,0).isAfter(new DateTime(timeMillis));
    }
    public boolean before(long timeMillis){
        return new DateTime().withTime(hour,minutes,seconds,0).isBefore(new DateTime(timeMillis));
    }

}

