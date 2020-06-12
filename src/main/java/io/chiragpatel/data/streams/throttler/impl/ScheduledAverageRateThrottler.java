package io.chiragpatel.data.streams.throttler.impl;

import io.chiragpatel.data.streams.throttler.InputStreamThrottler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

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
    private volatile boolean usable = false;


    private List<ThrottleSchedule> throttleSpecs =  new CopyOnWriteArrayList<>();

    public ScheduledAverageRateThrottler(List<ThrottleSchedule> throttleSpecs){
        if(throttleSpecs != null && throttleSpecs.size() > 0){
            this.throttleSpecs.addAll(throttleSpecs);
        } else {
            throw new IllegalArgumentException("throttling specs are required");
        }

    }

    @Override
    public void notifyRead(int size){

        firstReadTime.compareAndSet(-1,getCurrentTimeMillis());
        if(totalBytesRead.addAndGet(size) < 0) {
            //we have exceed the positive range of long which in zettabytes!
            //This can be handled by capturing previousAverage and including it
            //in the sleep time calculations. However, this results in more synchronized blocks and contention.
            usable = false;
        }
    }

    @Override
    /**
     * This method (and the method it calls) is not synchronized because we only care about averages. Due to race conditions this method may not
     * work correctly for a small time window but eventually will average out. For a large number of threads, synchronized blocks
     * can cause contention and we are trading off accuracy in favor of less contention and settling for eventual average case accuracy
     */
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

        if( targetThrottleRate == -1){
            //no throttling
            return 0;
        }

        // this inequality must always hold! (totalBytesRead/((finalTime - firstReadTime)/1000)) < targetThrottleRate
        long finalTime =  ((totalBytesRead.get())/ getTargetThrottleRate()) * 1000 + firstReadTime.get();
        long sleepTime =  finalTime - getCurrentTimeMillis();

        if(sleepTime > 0){
            return sleepTime;
        }
        return 0;
    }

    int getTargetThrottleRate() {

        long now = getCurrentTimeMillis();

        for (ThrottleSchedule throttleSpec : throttleSpecs) {

            if(throttleSpec != null && throttleSpec.matches(now)){
                return throttleSpec.getRateBytesPerSecond();
            }
        }
        return -1;
    }

    long getCurrentTimeMillis(){
        return System.currentTimeMillis();
    }
}
