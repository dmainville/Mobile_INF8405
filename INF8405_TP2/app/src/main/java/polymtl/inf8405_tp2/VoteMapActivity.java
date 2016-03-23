package polymtl.inf8405_tp2;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    final private int REQUEST_CODE_VOTE_DATE_ACTIVITY = 1;

    private GoogleMap mMap;
    private UserProfile mCurrentProfile;
    Marker meetingMarker;
    Button mBtnSkip;
    private int memberCount = Integer.MAX_VALUE; //Utilisé pour déterminer lorsque le vote prend fin
    private Firebase mFirebaseMemberRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_map);

        mBtnSkip = (Button) findViewById(R.id.btnSkip);
        mBtnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoteDateActivity();
            }
        });

        mCurrentProfile = (UserProfile) getIntent().getExtras().get("profile");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this);

        mFirebaseMemberRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("members");

        //Récupèrer le nombre de user du groupe. À partir de ce moment cette valeur ne devrait plus changer.
        //On la passera dans les intents aux prochaines activités.
        mFirebaseMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int count = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    count++;
                }
                memberCount = count;
                System.out.println("MEMBER COUNT : "+memberCount);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

    }

    private void startVoteDateActivity()
    {
        Intent intent = new Intent(this, VoteDateActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        intent.putExtra("memberCount", memberCount);
        startActivityForResult(intent, REQUEST_CODE_VOTE_DATE_ACTIVITY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        System.out.println("ON_MAP_READY");
        mMap = googleMap;
        System.out.println("lat : "+mCurrentProfile.meetingLatitude+" long : "+mCurrentProfile.meetingLongitude);
        // Add a marker in Sydney and move the camera
        LatLng latlng = new LatLng(mCurrentProfile.meetingLatitude, mCurrentProfile.meetingLongitude);
        meetingMarker = mMap.addMarker(new MarkerOptions()
                .position(latlng)
                .title("Meeting Area"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(meetingMarker.getPosition()));

    }
}
