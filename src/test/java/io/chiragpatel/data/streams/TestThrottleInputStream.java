package io.chiragpatel.data.streams;

import io.chiragpatel.data.streams.throttler.impl.ScheduledAverageRateThrottler;
import org.joda.time.DateTime;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Chirag Patel
 * User: softpsy
 * Date: 8/30/14
 * Time: 5:03 PM
 */
public class TestThrottleInputStream {

    ThrottledInputStream throttledInputStream;
    ScheduledAverageRateThrottler throttler;

    @Mock
    InputStream inputStream;

    @BeforeMethod
    public void init(){
        MockitoAnnotations.initMocks(this);
        throttler  = spy(new ScheduledAverageRateThrottler());
        throttledInputStream = new ThrottledInputStreamFactory(throttler).create(inputStream);

    }

    @Test
    public void shouldCallInputStreamReadMethodsOnRead1() throws IOException{

        when(inputStream.read()).thenReturn(1);
        int returnVal =  throttledInputStream.read();

        InOrder inOrder = inOrder(throttler,inputStream,throttler);

        inOrder.verify(throttler).throttle(-1L);
        inOrder.verify(inputStream).read();
        inOrder.verify(throttler).notifyRead(1);

        assertEquals(returnVal, 1,"read should return value 1");

    }


    @Test
    public void shouldCallInputStreamReadMethodsOnRead2() throws IOException{

        byte[] bytes = new byte[10];
        when(inputStream.read(eq(bytes))).thenReturn(5);
        int returnVal = throttledInputStream.read(bytes);

        InOrder inOrder = inOrder(throttler,inputStream,throttler);

        inOrder.verify(throttler).throttle(-1L);
        inOrder.verify(inputStream).read(eq(bytes));
        inOrder.verify(throttler).notifyRead(5);

        assertEquals(returnVal,5 ,"read should return value 5");
    }


    @Test
    public void shouldCallInputStreamReadMethodsOnRead3() throws IOException{

        byte[] bytes = new byte[10];
        when(inputStream.read(eq(bytes), eq(5), eq(10))).thenReturn(10);
        int returnVal = throttledInputStream.read(bytes, 5, 10);

        InOrder inOrder = inOrder(throttler,inputStream,throttler);

        inOrder.verify(throttler).throttle(-1L);
        inOrder.verify(inputStream).read(eq(bytes), eq(5), eq(10));
        inOrder.verify(throttler).notifyRead(10);

        assertEquals(returnVal,10,"read should return value 10");

    }

    @Test(expectedExceptions = IOException.class)
    public void shouldThrowIOExceptionWhenThrottleThrowsException1() throws IOException{

        doThrow(new IllegalStateException()).when(throttler).throttle(eq(-1L));
        throttledInputStream.read();

    }


    @Test(expectedExceptions = IOException.class)
    public void shouldThrowIOExceptionWhenThrottleThrowsException2() throws IOException{
        byte[] bytes = new byte[10];
        doThrow(new IllegalStateException()).when(throttler).throttle(eq(-1L));
        throttledInputStream.read(bytes);

    }


    @Test(expectedExceptions = IOException.class)
    public void shouldThrowIOExceptionWhenThrottleThrowsException3() throws IOException{
        byte[] bytes = new byte[10];
        doThrow(new IllegalStateException()).when(throttler).throttle(eq(-1L));
        throttledInputStream.read(bytes, 6, 10);

    }

    @Test
    public void shouldCallThrottlerWithMaxWaitTime() throws IOException{

        ThrottledInputStream throttledInputStream1 = new ThrottledInputStream(inputStream, throttler, 50);
        byte[] bytes = new byte[10];

        throttledInputStream1.read(bytes, 6, 10);
        verify(throttler).throttle(eq(50L));

    }

}
