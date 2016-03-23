package polymtl.inf8405_tp2;

import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class VoteMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UserProfile mCurrentProfile;
    Firebase mFirebaseRef;
    Firebase mFirebaseGroupRef;
    ArrayList<String> users;
    ArrayList<CalendarEvent> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_map);
        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GetGroupUsers();

    }

    public void GetUsersEvents(ArrayList<String> users)
    {
        events = new ArrayList<CalendarEvent>();
        Firebase.setAndroidContext(this);

        System.out.println("GET USERS EVENT");

        for (String user : users)
        {
            System.out.println("LOOKING FOR USER :"+user);

            //Récupéré les event du user
            mFirebaseRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                    .child("UserProfiles")
                    .child(user);

            mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if(child.getKey()!="events")
                            continue;

                        System.out.println("EVENTS : "+child.getValue());

                    }



                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {}
            });
        }
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

                GetUsersEvents(users);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        System.out.println("ON_MAP_READY");
        mMap = googleMap;

        Location loc = new Location("Meeting Area");
        loc.setLongitude(mCurrentProfile.meetingLongitude);
        loc.setLatitude(mCurrentProfile.meetingLatitude);
        // Add a marker in Sydney and move the camera
        LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latlng).title(loc.getProvider()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

    }
}
