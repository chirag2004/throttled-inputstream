package io.chiragpatel.data.streams.throttler.impl;

import io.chiragpatel.data.streams.throttler.impl.ScheduledAverageRateThrottler;
import org.joda.time.DateTime;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by Chirag Patel
 * User: softpsy
 * Date: 8/29/14
 * Time: 2:51 PM
 */
public class TestScheduledAverageRateThrottler {

    ScheduledAverageRateThrottler throttler;

    @Mock
    InputStream inputStream;

    @BeforeMethod
    public void init(){
        throttler = spy(new ScheduledAverageRateThrottler());

        throttler.addThrottleTimeRange( new ThrottleTime(12,0,0), new ThrottleTime(18,0,0), 64 * 1024);
        throttler.addThrottleTimeRange(new ThrottleTime(0,0,0), new ThrottleTime(6,0,0), 64);
        MockitoAnnotations.initMocks(this);
    }



    @Test
    public void shouldReturnTheRightTargetThrottleRateBetweenTimeRange(){

        setCurrentTime(15,0,0);

        assertEquals(throttler.getTargetThrottleRate(), 64*1024);

        setCurrentTime(4,0,0);

        assertEquals(throttler.getTargetThrottleRate(), 64);

    }

    @Test
    public void shouldReturnMaxRateWhenNoTimeIsScheduled(){

        setCurrentTime(9,0,0);

        assertEquals(throttler.getTargetThrottleRate(), Integer.MAX_VALUE);


    }

    @Test
    public void shouldNotSleepWhenNoBytesAreRead(){

        setCurrentTime(15,0,0);

        assertEquals(throttler.getSleepTime(), 0);

    }

    public void shouldSleepBasedOnSleepTimeReturnedWhenNoMaxWaitTime(){

        setCurrentTime(15,0,0);

        doReturn(5555L).when(throttler).getSleepTime();

        throttler.throttle(-1);

        verify(throttler).sleepFor(eq(5555L));

    }

    public void shouldSleepBasedOnSleepTimeReturnedWhenLessThanMaxWaitTime(){

        setCurrentTime(15,0,0);

        doReturn(5555L).when(throttler).getSleepTime();

        throttler.throttle(10000);

        verify(throttler).sleepFor(eq(5555L));

    }


    @Test
    public void sleepTimeShouldResultInTargetThrottleRateAfterHighReadRate(){


        setCurrentTime(3, 0, 0);

        throttler.notifyRead(102400);
        setCurrentTime(3,0,15);
        throttler.notifyRead(204800);

        setCurrentTime(4,0,0);

        long sleepTimeSec = throttler.getSleepTime()/1000;

        assertTrue(sleepTimeSec > 0);

        long rateAfterSleep = (102400+204800)/(3600+sleepTimeSec);

        assertEquals(64, rateAfterSleep,"Rate after sleepTime should be 64");

    }

    @Test
    public void lowerReadRateShouldResultInZeroSleepTime(){

        setCurrentTime(3, 0, 0);

        throttler.notifyRead(102400);
        throttler.notifyRead(102400);

        setCurrentTime(4,0,0);

        long sleepTime = throttler.getSleepTime();

        assertEquals(0, sleepTime,"sleepTime should be 0");

    }

    @Test
    public void unlimitedTargetRateShouldResultInZeroSleepTime(){

        setCurrentTime(9, 0, 0);

        throttler.notifyRead(102400);
        throttler.notifyRead(Integer.MAX_VALUE);

        setCurrentTime(9,1,0);

        long sleepTime = throttler.getSleepTime();

        assertEquals(0, sleepTime,"sleepTime should be 0");

    }


    @Test(expectedExceptions = IllegalStateException.class)
    public void throwsIOExceptionWhenSleepTimeExceedsMaxWaitTime(){

        setCurrentTime(3, 0, 0);

        throttler.notifyRead(Integer.MAX_VALUE);

        setCurrentTime(4, 0, 0);

        throttler.throttle(4);

    }



    @Test
    public void maximumWaitTimeWithNegativeValueShouldNotThrowAnError(){

        setCurrentTime(3, 0, 0);

        throttler.notifyRead(Integer.MAX_VALUE);

        setCurrentTime(3, 0, 1);
        setCurrentTime(3, 0, 1);

        doReturn(1L).when(throttler).getSleepTime();

        throttler.throttle(-1);

    }


    private void setCurrentTime(int hour, int minutes, int seconds) {
        doReturn(DateTime.now().withTime(hour, minutes, seconds, 0).getMillis()).when(throttler).getCurrentTimeMillis();
    }


}
