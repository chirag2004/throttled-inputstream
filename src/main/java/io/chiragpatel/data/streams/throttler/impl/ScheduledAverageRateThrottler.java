package io.chiragpatel.data.streams.throttler.impl;

import io.chiragpatel.data.streams.throttler.InputStreamThrottler;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Chirag Patel
 * User: softpsy
 * Date: 8/29/14
 * Time: 12:54 PM
 * A throttler that throttles reads such the the average read rate is
 * maintained as per schedule interval specification.
 * Note: There may be higher than average reads between a given interval
 * but subsequent reads will be delayed so that average is maintained.
 */
public class ScheduledAverageRateThrottler implements InputStreamThrottler {

    private AtomicLong totalBytesRead = new AtomicLong(0);
    private AtomicLong firstReadTime = new AtomicLong(-1);
    private volatile boolean usable = true;


    private List<ThrottleSchedule> throttleSpecs =  new CopyOnWriteArrayList<>();

    public ScheduledAverageRateThrottler(){
    }

    public ScheduledAverageRateThrottler(List<ThrottleSchedule> throttleSpecs){
        if(throttleSpecs != null){
            this.throttleSpecs.addAll(throttleSpecs);
        }
    }


    @Override
    public void notifyRead(int size){

        firstReadTime.compareAndSet(-1,getCurrentTimeMillis());
        try{
            totalBytesRead.addAndGet(size);
            //catch overflows
        } catch (ArithmeticException ae){
            usable = false;
        }

    }

    @Override
    public void throttle(long maxWaitTimeMillis){

        if(!usable){
            throw new IllegalStateException("cannot throttle anymore. max throttled bytes limit reached");
        }
        long sleepTime = getSleepTime();

        if(sleepTime > maxWaitTimeMillis && maxWaitTimeMillis > 0) {
            throw new IllegalStateException("available bandwidth too low");
        }

        if(sleepTime > 0){
            sleepFor(sleepTime);
        }
    }

    protected void sleepFor(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new IllegalStateException("failed to throttle", e);
        }
    }

    long getSleepTime() {

        if(totalBytesRead.get() <= 0){
            //nothing read yet (no read notification)... don't sleep
            return 0;
        }
        //if totalBytesRead > 0 we are guaranteed that the first read has occurred..
        int targetThrottleRate = getTargetThrottleRate();

        if(Integer.MAX_VALUE == targetThrottleRate){
            return 0;
        }

        // (totalBytesRead/((finalTime - firstReadTime)/1000)) < targetThrottleRate
        long finalTime =  ((totalBytesRead.get())/ getTargetThrottleRate()) * 1000 + firstReadTime.get();
        long sleepTime =  finalTime - getCurrentTimeMillis();

        if(sleepTime > 0){
            return sleepTime;
        }
        return 0;
    }


    public ScheduledAverageRateThrottler addThrottleTimeRange(ThrottleTime start, ThrottleTime end, int maxBytes){
        this.throttleSpecs.add(new ThrottleSchedule(start, end, maxBytes));
        return this;
    }

    int getTargetThrottleRate() {

        long now = getCurrentTimeMillis();

        for (ThrottleSchedule throttleSpec : throttleSpecs) {

            if(throttleSpec != null && throttleSpec.matches(now)){
                return throttleSpec.getRateInBytes();
            }
        }
        return Integer.MAX_VALUE;
    }

    long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }


}
