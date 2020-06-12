package io.chiragpatel.data.streams.throttler.impl;

/**
 * Created by Software Psychology
 * User: softpsy
 * Date: 8/30/14
 * Time: 6:58 PM
 */
public class ThrottleSchedule {
    private ThrottleTime start;
    private ThrottleTime end;
    private int rateInBytesPerSecond;


    public ThrottleSchedule(ThrottleTime start, ThrottleTime end, int rateInBytesPerSecond) {
        if(start == null){
            throw new IllegalArgumentException("start date cannot be null");
        }
        if(end == null) {
            throw new IllegalArgumentException("start date cannot be null");
        }
        if(rateInBytesPerSecond <= 0){
            throw new IllegalArgumentException("rate must be greater than 0");
        }
        this.start = start;
        this.end = end;
        this.rateInBytesPerSecond = rateInBytesPerSecond;
    }

    public int getRateBytesPerSecond() {
        return rateInBytesPerSecond;
    }

    public boolean matches(long time){
        return start.before(time) &&  end.after(time);

    }
}
