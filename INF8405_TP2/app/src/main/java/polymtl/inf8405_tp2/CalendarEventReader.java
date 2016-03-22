package polymtl.inf8405_tp2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.content.pm.PackageManager;
import android.Manifest;
import android.support.v4.content.ContextCompat;

//Inspiré de http://developer.android.com/guide/topics/providers/calendar-provider.html

public class CalendarEventReader {
    public static final String[] FIELDS = {
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.AVAILABILITY};

    public static final Uri CALENDAR_URI = Uri.parse("content://com.android.calendar/calendars");

    ContentResolver contentResolver;
    Set<String> calendars = new HashSet<String>();

    public  CalendarEventReader(){
    }

    //Retourne les évènements du calendrier courrant entre les tenmps de début et de fin
    public static List<CalendarEvent> GetCurrentDeviceCalendarEvents(Context context, long startTime, long endTime)
    {
        System.out.println("READING CALENDAR 1");

        List<CalendarEvent> events = new ArrayList<CalendarEvent>();
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return events;

        System.out.println("READING CALENDAR 2");

        Cursor cursor = CalendarContract.Instances.query(context.getContentResolver(), FIELDS, startTime, endTime);
        while (cursor.moveToNext())
        {
            System.out.println("READING CALENDAR 3");

            if(Integer.parseInt(cursor.getString(4)) != CalendarContract.Instances.AVAILABILITY_BUSY)
                continue;

            Long eventStart = (Long.parseLong(cursor.getString(2)));
            Long eventEnd = (Long.parseLong(cursor.getString(3)));
            String eventName = cursor.getString(1);

            CalendarEvent event = new CalendarEvent(eventStart,eventEnd,eventName);
            events.add(event);

            System.out.println("EVENT : " + eventName);
        }

        return events;
    }
}