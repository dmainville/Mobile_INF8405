package polymtl.inf8405_tp2;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class VoteMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UserProfile mCurrentProfile;
    Firebase mFirebaseRef;
    Firebase mFirebaseGroupRef;
    ArrayList<String> users;
    ArrayList<CalendarEvent> events;
    Double mLongitude;
    Double mLatitude;
    Marker meetingMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_map);
        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");

        mLatitude = 0.0;
        mLongitude = 0.0;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GetGroupUsers();

    }

    public void ChooseBestAvailabilities()
    {

        int[][] conflicts = new int[3][7];

        Calendar currentTime = Calendar.getInstance(); //Heure actuelle
        currentTime.setTime(new Date());

        Calendar checkTime = Calendar.getInstance(); //Heure sur laquelle on itère
        currentTime.setTime(new Date());

        //À partir de la date actuel, itérer de 2h et trouver 3 zones d'ici à trois jours qui contiennet le moins de conflit
        //On itère sur trois jours à chaque 2h de 8h à 20h
        for(int dayCount=1; dayCount<=3; dayCount++)
        {
            checkTime.add(Calendar.DATE,dayCount);

            for(int hourCount=8; hourCount<20; hourCount+=2)
            {
                checkTime.set(Calendar.HOUR,hourCount);

                //Considèrer les heures passé comme des conflits
                if(checkTime.getTimeInMillis() <  currentTime.getTimeInMillis())
                {
                    conflicts[dayCount-1][(hourCount/2)-4] = Integer.MAX_VALUE;
                }

                //Vérifier la présence de conflit pour l'heure sur laquelle on itère (checkTime)
                for(CalendarEvent event : events)
                {
                    //Détection d'un conflit
                    if(event.eventStart < checkTime.getTimeInMillis() && checkTime.getTimeInMillis() < event.eventEnd)
                    {
                        conflicts[dayCount-1][(hourCount/2)-4] ++;
                    }
                }
            }
        }

        //Une fois qu'on a déterminer le nombre de conflit pour chaque case on choisit les 3 première cases sans conflit. Si ce n'est pas possible on choisit les 3 cases avec le moins de conflits
        int conflitTolerance = 0;
        int foundAvailabilities = 0;
        Calendar[] availabilities = new Calendar[3];

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
                        foundTime.add(Calendar.DATE, j);
                        foundTime.set(Calendar.HOUR, j * 2 + 8);

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
                        if(properties.getKey() == "events") {
                            System.out.println("EVENTS : " + properties.getValue());

                            if (properties.getValue().equals("[]"))
                                break;

                            String strEvents[] = properties.getValue().toString().split(",");
                            for (String strEvent : strEvents) {
                                events.add(new CalendarEvent(strEvent));
                            }
                        }

                        if(properties.getKey() == "latitude")
                            mLatitude += (Double)properties.getValue();
                        if(properties.getKey() == "longitude")
                            mLongitude += (Double)properties.getValue();

                    }
                }

                for(CalendarEvent event : events)
                {
                    System.out.println("EVENT COMPLETE LIST : "+event);
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
                    System.out.println("CHILD!!"+child.getKey());
                    users.add(child.getKey());
                }

                GetUsersEvents();
                UpdateMap();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        System.out.println("ON_MAP_READY");
        mMap = googleMap;

        // Add a marker and move the camera
        LatLng latlng = new LatLng(mLatitude / users.size(), mLongitude/users.size());
        meetingMarker = mMap.addMarker(new MarkerOptions()
            .position(latlng)
            .title("Meeting Area"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(meetingMarker.getPosition()));
    }

    public void UpdateMap(){
        meetingMarker.remove();
        LatLng latlng = new LatLng(mLatitude / users.size(), mLongitude/users.size());
        meetingMarker = mMap.addMarker(new MarkerOptions()
        .position(latlng)
        .title("Meeting Area"));
    }
}
