## throttled-inputstream
This is an implementation of input streams that share bandwidth amongst one another. If the average number of bytes exceed 
threshold, the reads get throttled to maintain an average read rate. The throttling is specified on a daily schedule providing start and end UTC times and the target rate during those times.

#### Limitations/Tradeoffs
* There may be temporary spikes in the read rates but the average is maintained over time. We trade off temporary accuracy in order reduce thread contentions caused by synchronized blocks.
* The shared upper limit for data consumed by the throttled input streams is 2^63-1 bytes (which is the maximum value for the long type in Java). This is approximately 8-9 zettabytes.

#### Sample Usage 
```java

    // ThrottleTime represents hour, minute, second UTC time
    // ThrottleSchedule takes ThrottleSchedule and a rate in bytesPerSecond
    ThrottleSchedule schedule1 = new ThrottleSchedule(new ThrottleTime(12,0,0), new ThrottleTime(18,0,0),64 * 1024);
    ThrottleSchedule schedule2 = new ThrottleSchedule(new ThrottleTime(0,0,0), new ThrottleTime(6,0,0), 64);

    ScheduledAverageRateThrottler throttler = new ScheduledAverageRateThrottler(Arrays.asList(schedule1, schedule2));
    ThrottledInputStreamFactory throttledStreamFactory = new ThrottledInputStreamFactory(throttler);
    
    //Not the typical server code, but just an example
    ServerSocket server = new ServerSocket(8080);
    Socket socket1 = server.accept();
    Socket socket2 = server.accept();

    InputStream throttledStream1 = throttledStreamFactory.create(socket1.getInputStream());
    InputStream throttledStream2 = throttledStreamFactory.create(socket2.getInputStream());

     //Now throttledStream1 and throttledStream2 can be read by 2 separate threads but share the specified bandwidth


