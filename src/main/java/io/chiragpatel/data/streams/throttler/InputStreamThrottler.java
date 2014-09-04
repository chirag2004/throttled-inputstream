package io.chiragpatel.data.streams.throttler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Chirag Patel
 * User: softpsy
 * Date: 8/29/14
 * Time: 12:53 PM
 */
public interface InputStreamThrottler {

    void notifyRead(int size);
    void throttle(long maxWaitTime);
}
