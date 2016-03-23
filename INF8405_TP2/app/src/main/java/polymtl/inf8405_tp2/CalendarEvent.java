package polymtl.inf8405_tp2;

//Permet de garder en mémoire les disponibilités des utilisateurs

import java.io.Serializable;

public class CalendarEvent implements Serializable {

    Long eventStart;
    Long eventEnd;
    String eventName; //On garde le nom de l'évènement dans le cas ou l'on voudrait permettre des conflits

    public CalendarEvent(Long eventStart,  Long eventEnd, String eventName)
    {
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventName = eventName;
    }

    public CalendarEvent(String data)
    {
        data = data.replace('[', ' ');
        data = data.replace(']',' ');

        String values[] = data.split("-");

        this.eventStart = Long.parseLong(values[0].trim());
        this.eventEnd = Long.parseLong(values[1].trim());
    }

    public String toString()
    {
        //On ne se sert pas du event name
        return eventStart.toString()+"-"+eventEnd.toString();
    }

}
