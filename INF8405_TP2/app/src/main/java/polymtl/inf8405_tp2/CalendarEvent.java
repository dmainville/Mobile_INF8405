package polymtl.inf8405_tp2;

//Permet de garder en mémoire les disponibilités des utilisateurs

public class CalendarEvent {

    Long eventStart;
    Long eventEnd;
    String eventName; //On garde le nom de l'évènement dans le cas ou l'on voudrait permettre des conflits

    public CalendarEvent(Long eventStart,  Long eventEnd, String eventName)
    {
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
    }

}
