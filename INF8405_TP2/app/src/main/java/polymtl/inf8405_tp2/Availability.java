package polymtl.inf8405_tp2;


import android.text.format.Time;

import java.util.Properties;

public class Availability {

    Time startTime;
    Time endTime;
    Boolean available;
    String conflitEventName;

    public Availability(Time startTime,Time endTime, Boolean available, String conflitEventName)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.conflitEventName = conflitEventName;
    }

    public Properties ToProperties()
    {
        Properties result = new Properties();
        result.setProperty("startTime", startTime.toString());
        result.setProperty("endTime", endTime.toString());
        result.setProperty("available", String.valueOf(available));
        result.setProperty("conflitEventName", conflitEventName);

        return result;
    }

}
