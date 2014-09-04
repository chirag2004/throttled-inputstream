package io.chiragpatel.data.streams;

import java.io.IOException;
import java.io.InputStream;

import io.chiragpatel.data.streams.throttler.InputStreamThrottler;

/**
 * Created by Chirag Patel on 8/25/14.
 */
public class ThrottledInputStream extends InputStream {

    private InputStream inputStream;
    private InputStreamThrottler throttler;
    private int maxWaitTimeMilliSeconds = -1;

    public ThrottledInputStream(InputStream inputStream, InputStreamThrottler throttler, int maxWaitTimeMilliSeconds){
        if(inputStream == null){
            throw new IllegalArgumentException("inputStream cannot be null");
        }
        if(throttler == null){
            throw new IllegalArgumentException("throttler cannot be null");
        }

        this.inputStream = inputStream;
        this.throttler = throttler;
        this.maxWaitTimeMilliSeconds = maxWaitTimeMilliSeconds;
    }
    public ThrottledInputStream(InputStream inputStream, InputStreamThrottler throttler){
        this(inputStream, throttler, -1);
    }

    @Override
    public int read() throws IOException {
        try{
            throttler.throttle(maxWaitTimeMilliSeconds);
        } catch (IllegalStateException e){
            throw new IOException(e);
        }
        int returnVal = inputStream.read();
        throttler.notifyRead(1);
        return returnVal;
    }

    @Override
    public int read(byte b[]) throws IOException {
        try{
            throttler.throttle(maxWaitTimeMilliSeconds);
        } catch (IllegalStateException e){
            throw new IOException(e);
        }
        int size = inputStream.read(b);
        throttler.notifyRead(size);
        return size;
    }

    @Override
    public int read(byte b[], int offset, int len) throws IOException {
        try{
            throttler.throttle(maxWaitTimeMilliSeconds);

        } catch (IllegalStateException e){
            throw new IOException(e);
        }
        int size = inputStream.read(b, offset, len);
        throttler.notifyRead(size);
        return size;
    }


    @Override
    public void close() throws IOException {

        inputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

}
