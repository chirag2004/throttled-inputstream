package io.chiragpatel.data.streams;

import io.chiragpatel.data.streams.throttler.InputStreamThrottler;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Chirag Patel on 8/26/14.
 */
public class ThrottledInputStreamFactory {

    private InputStreamThrottler inputStreamThrottler;

    public ThrottledInputStreamFactory(InputStreamThrottler inputStreamThrottler){
        if(inputStreamThrottler == null){
            throw new IllegalArgumentException("InputStreamThrotller cannot be null");

        }
        this.inputStreamThrottler = inputStreamThrottler;
    }

    public ThrottledInputStream create(InputStream inputStream, int maxWaitTime){
        ThrottledInputStream throttledInputStream = new ThrottledInputStream(inputStream, inputStreamThrottler, maxWaitTime);
        return throttledInputStream;
    }

    public ThrottledInputStream create(InputStream inputStream){
        ThrottledInputStream throttledInputStream = new ThrottledInputStream(inputStream, inputStreamThrottler);
        return throttledInputStream;
    }

}
