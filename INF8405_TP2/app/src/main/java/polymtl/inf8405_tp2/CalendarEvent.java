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

    public String toString()
    {
        return eventStart.toString()+"-"+eventEnd+"-"+eventName;
    }

}
