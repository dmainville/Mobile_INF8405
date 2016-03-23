package polymtl.inf8405_tp2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class VoteDateActivity extends AppCompatActivity {

    Firebase mFirebaseRef;
    Firebase mFirebaseGroupRef;
    ArrayList<String> users;
    ArrayList<CalendarEvent> events;
    private UserProfile mCurrentProfile;
    Calendar[] availabilities;
    boolean readyToVote = false;

    private Button mBtnVote;
    private TextView mLblTime1;
    private TextView mLblTime2;
    private TextView mLblTime3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_date);

        mLblTime1 = (TextView) findViewById(R.id.lblTime1);
        mLblTime2 = (TextView) findViewById(R.id.lblTime2);
        mLblTime3 = (TextView) findViewById(R.id.lblTime3);
        mBtnVote = (Button) findViewById(R.id.btnVote);
        mBtnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do something
            }
        });

        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");
        GetGroupUsers();
    }

    public void ChooseBestAvailabilities()
    {

        int[][] conflicts = new int[3][7];

        Calendar currentTime = Calendar.getInstance(); //Heure actuelle
        currentTime.setTime(new Date());
        System.out.println("CURRENT TIME : "+currentTime.getTime().toString());

        Calendar checkTime = Calendar.getInstance(); //Heure sur laquelle on itère
        Calendar checkTimeEnd = Calendar.getInstance(); //Heure de fin de la période sur laquelle on itère
        checkTime.setTime(new Date());
        checkTime.set(Calendar.MINUTE, 0);
        checkTime.set(Calendar.SECOND, 0);

        checkTimeEnd.setTime(new Date());
        checkTimeEnd.set(Calendar.MINUTE, 0);
        checkTimeEnd.set(Calendar.SECOND, 0);

        //À partir de la date actuel, itérer de 2h et trouver 3 zones d'ici à trois jours qui contiennet le moins de conflit
        //On itère sur trois jours à chaque 2h de 8h à 20h
        for(int dayCount=0; dayCount<=2; dayCount++)
        {
            checkTime.add(Calendar.DATE,dayCount);
            checkTimeEnd.add(Calendar.DATE,dayCount);

            for(int hourCount=8; hourCount<20; hourCount+=2)
            {
                checkTime.set(Calendar.HOUR_OF_DAY, hourCount);
                checkTimeEnd.set(Calendar.HOUR_OF_DAY, hourCount+2);

                //Considèrer les heures passé comme des conflits
                System.out.println("CHECK TIME : "+checkTime.getTime().toString());
                System.out.println("CHECK TIME END : "+checkTimeEnd.getTime().toString());
                /*System.out.println("CHECK TIME MILI: "+checkTime.getTimeInMillis());
                System.out.println("CURRENT TIME MILI : "+currentTime.getTimeInMillis());*/
                if(checkTime.getTimeInMillis() <  currentTime.getTimeInMillis())
                {
                    //System.out.println("TIME IS SMALLER");
                    conflicts[dayCount][(hourCount/2)-4] = Integer.MAX_VALUE;
                }

                //Vérifier la présence de conflit pour l'heure sur laquelle on itère (checkTime)
                for(CalendarEvent event : events)
                {
                    /*System.out.println("EVENT START : "+new Date(event.eventStart).getTime());
                    System.out.println("EVENT END : " + new Date(event.eventEnd).getTime());*/
                    //Détection d'un conflit
                    if(isOverlapping(new Date(event.eventStart),new Date(event.eventEnd),new Date(checkTime.getTimeInMillis()),new Date(checkTimeEnd.getTimeInMillis())))
                    {
                    /*if(event.eventStart <= checkTime.getTimeInMillis() && checkTime.getTimeInMillis() <= event.eventEnd)
                    {*/
                        System.out.println("CONFLICT DETECTED : "+checkTime.getTime().toString());

                        conflicts[dayCount][(hourCount/2)-4] ++;
                    }
                }
            }
        }

        //Une fois qu'on a déterminer le nombre de conflit pour chaque case on choisit les 3 première cases sans conflit. Si ce n'est pas possible on choisit les 3 cases avec le moins de conflits
        int conflitTolerance = 0;
        int foundAvailabilities = 0;
        availabilities = new Calendar[3];

        while(foundAvailabilities <3)
        {
            for(int i=0; i<3; i++)
            {
                if(foundAvailabilities ==3)
                    break;

                for(int j=0; j<7; j++)
                {
                    //On trouve une date dans la zone de confort du nombre de conflit
                    if(conflicts[i][j] == conflitTolerance)
                    {
                        Calendar foundTime = Calendar.getInstance(); //Heure actuelle
                        foundTime.setTime(new Date());
                        foundTime.add(Calendar.DATE, i);
                        foundTime.set(Calendar.MINUTE, 0);
                        foundTime.set(Calendar.SECOND, 0);
                        foundTime.set(Calendar.HOUR_OF_DAY, j * 2 + 8);

                        availabilities[foundAvailabilities] = foundTime;
                        foundAvailabilities++;

                        if(foundAvailabilities ==3)
                            break;
                    }
                }
            }

            conflitTolerance++;
        }

        //On a trouvé 3 dates on les affiches pour que les users puissent voter
        AjustLabels();
    }

    public static boolean isOverlapping(Date start1, Date end1, Date start2, Date end2) {
        return start1.before(end2) && start2.before(end1);
    }


    public void AjustLabels()
    {
        mLblTime1.setText(availabilities[0].getTime().toString());
        mLblTime2.setText(availabilities[1].getTime().toString());
        mLblTime3.setText(availabilities[2].getTime().toString());

        readyToVote = true;
    }

    public void GetUsersEvents()
    {
        events = new ArrayList<CalendarEvent>();
        Firebase.setAndroidContext(this);

        System.out.println("GET USERS EVENT");

        //Récupéré les event des users
        mFirebaseRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("UserProfiles");

        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {

                for (DataSnapshot child : snapshot.getChildren())
                {
                    if(!users.contains(child.getKey()))
                        continue;

                    for (DataSnapshot properties : child.getChildren())
                    {
                        if(properties.getKey() != "events")
                            continue;

                        System.out.println("EVENTS : " + properties.getValue());

                        if(properties.getValue().equals("[]"))
                            break;

                        String strEvents[] = properties.getValue().toString().split(",");
                        for(String strEvent : strEvents)
                        {
                            events.add(new CalendarEvent(strEvent));
                        }
                    }
                }

                for(CalendarEvent event : events)
                {
                    Calendar eventTimeStart = Calendar.getInstance();
                    eventTimeStart.setTimeInMillis(event.eventStart);
                    Calendar eventTimeEnd = Calendar.getInstance();
                    eventTimeEnd.setTimeInMillis(event.eventEnd);
                    System.out.println("EVENT LIST START : " + eventTimeStart.getTime());
                    System.out.println("EVENT LIST END : "+eventTimeEnd.getTime());
                }

                ChooseBestAvailabilities();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });

    }

    public void GetGroupUsers()
    {
        users = new ArrayList<String>();

        Firebase.setAndroidContext(this);

        //Récupéré les users du groupe
        mFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("members");

        mFirebaseGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    System.out.println("CHILD!!" + child.getKey());
                    users.add(child.getKey());
                }

                GetUsersEvents();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

}
