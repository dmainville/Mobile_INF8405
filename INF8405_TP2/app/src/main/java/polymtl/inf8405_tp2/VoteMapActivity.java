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
        //À partir de la date actuel, itérer de 2h et trouver 3 zones d'ici à trois jours qui contiennet le moins de conflit
        
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
                        if(properties.getKey() == "events"){
                            System.out.println("EVENTS : " + properties.getValue());
                            String strEvents[] = properties.getValue().toString().split(",");
                            for(String strEvent : strEvents)
                            {
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
