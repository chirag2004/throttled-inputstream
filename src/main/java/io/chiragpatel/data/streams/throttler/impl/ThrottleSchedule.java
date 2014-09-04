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
    private int rateInBytes;


    public ThrottleSchedule(ThrottleTime start, ThrottleTime end, int rateInBytes) {
        if(start == null){
            throw new IllegalArgumentException("start date cannot be null");
        }
        if(end == null) {
            throw new IllegalArgumentException("start date cannot be null");
        }
        if(rateInBytes <= 0){
            throw new IllegalArgumentException("rate in bytes cannot be null");
        }
        this.start = start;
        this.end = end;
        this.rateInBytes = rateInBytes;
    }

    public int getRateInBytes() {
        return rateInBytes;
    }

    public boolean matches(long time){
        return start.before(time) &&  end.after(time);

    }
}
