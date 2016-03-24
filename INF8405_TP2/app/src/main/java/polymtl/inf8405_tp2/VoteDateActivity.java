package polymtl.inf8405_tp2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Switch;
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

    //Référence de base de données
    Firebase mFirebaseRef;
    Firebase mFirebaseGroupRef;
    Firebase mFirebaseVoteRef;

    ArrayList<String> users;
    ArrayList<CalendarEvent> events;
    private UserProfile mCurrentProfile;
    private int InitialBatterieLevel;
    Calendar[] availabilities;

    //Ui
    private Button mBtnVote;
    private TextView mLblTime1;
    private TextView mLblTime2;
    private TextView mLblTime3;
    private TextView mLblCount1;
    private TextView mLblCount2;
    private TextView mLblCount3;
    private RadioGroup mRadioGroup;

    //Application date
    private int[] voteCount;
    private int memberCount = Integer.MAX_VALUE; //Utilisé pour déterminer lorsque le vote prend fin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_date);

        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");
        InitialBatterieLevel = (int) getIntent().getExtras().get("batterie");
        memberCount = (int)getIntent().getExtras().get("memberCount");

        Firebase.setAndroidContext(this);
        mFirebaseGroupRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("members");

        mFirebaseVoteRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("VotesDate");

        mRadioGroup = (RadioGroup)findViewById(R.id.rbGroupDate);
        mLblTime1 = (TextView) findViewById(R.id.lblTime1);
        mLblTime2 = (TextView) findViewById(R.id.lblTime2);
        mLblTime3 = (TextView) findViewById(R.id.lblTime3);

        mLblCount1 = (TextView) findViewById(R.id.lblVoteCount1);
        mLblCount2 = (TextView) findViewById(R.id.lblVoteCount2);
        mLblCount3 = (TextView) findViewById(R.id.lblVoteCount3);

        mBtnVote = (Button) findViewById(R.id.btnVote);
        mBtnVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int radioButtonID = mRadioGroup.getCheckedRadioButtonId();
                View radioButton = mRadioGroup.findViewById(radioButtonID);
                int idx = mRadioGroup.indexOfChild(radioButton);
                System.out.println("INDEX : " + idx);

                //Les indexes des radio button sont 1, 4 et 7. -1 Veux dire que rien  n'est sélectionné

                int idVote = -1;
                switch (idx) {
                    case 1:
                        idVote = 0;
                        break;

                    case 4:
                        idVote = 1;
                        break;

                    case 7:
                        idVote = 2;
                        break;
                }

                if (idVote != -1) {
                    System.out.println("VOTE COUNT : " + idVote + " - " + (voteCount[idVote] + 1));
                    mFirebaseVoteRef.child("votes" + (idVote+1)).setValue((voteCount[idVote] + 1));

                    mBtnVote.setEnabled(false);
                }

            }
        });

        GetGroupUsers();
        LoadVotes();

        mFirebaseVoteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Ajouter un listener pour savoir si l'admin a décidé de passer au vote.
                if (snapshot.hasChild("votes1")) {
                    voteCount[0] = Integer.parseInt(snapshot.child("votes1").getValue().toString());
                    mLblCount1.setText("Votes : "+ voteCount[0]);

                }

                if (snapshot.hasChild("votes2")) {
                    voteCount[1] = Integer.parseInt(snapshot.child("votes2").getValue().toString());
                    mLblCount2.setText("Votes : "+ voteCount[1]);
                }

                if (snapshot.hasChild("votes3")) {
                    voteCount[2] = Integer.parseInt(snapshot.child("votes3").getValue().toString());
                    mLblCount3.setText("Votes : "+ voteCount[2]);
                }

                System.out.println("DATA CHANGED!");
                int voteCountTotal = 0;
                for(int i=0; i<3; i++)
                {
                    voteCountTotal+=voteCount[i];
                }

                if(voteCountTotal>=memberCount)
                {
                    System.out.println("VOTE DATE COMPLETED!");
                    if(mCurrentProfile.organizer)
                    {
                        //Tous les membres ont voté, on peut storé dans la base de donnée le meilleur résultat
                        int bestId = 0;
                        for(int i=1; i<3; i++)
                        {
                            //On trouve le choix avec le plus de vote
                            if(voteCount[i] > voteCount[bestId])
                                bestId = i;
                        }

                        String voteResult = "";
                        if(bestId == 0)
                        {
                            voteResult = mLblTime1.getText().toString();
                        }
                        else if(bestId == 1)
                        {
                            voteResult = mLblTime2.getText().toString();
                        }
                        else
                        {
                            voteResult = mLblTime3.getText().toString();
                        }

                        mFirebaseVoteRef.child("voteResult").setValue(voteResult);
                        GotoAdminAfterVote();
                    }
                    else
                    {
                        GotoFinalResultActivity();
                    }

                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void GotoFinalResultActivity()
    {
        Intent intent = new Intent(this, FinalResultActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        intent.putExtra("batterie", InitialBatterieLevel);
        startActivity(intent);

        // On arrête cette activité lorsqu'on passe au prochain parce qu'on n'en a plus besoin
        finish();
    }

    public void GotoAdminAfterVote()
    {
        Intent intent = new Intent(this, AdminAfterVoteActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        intent.putExtra("batterie", InitialBatterieLevel);
        startActivity(intent);

        // On arrête cette activité lorsqu'on passe au prochain parce qu'on n'en a plus besoin
        finish();
    }

    public void LoadVotes()
    {
        voteCount = new int[3];
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

    }

    public void GetUsersEvents()
    {
        events = new ArrayList<CalendarEvent>();
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

        //Récupéré les users du groupe
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
