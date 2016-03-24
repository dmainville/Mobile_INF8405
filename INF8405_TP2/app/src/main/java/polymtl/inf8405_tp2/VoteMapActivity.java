package polymtl.inf8405_tp2;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
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
import java.util.HashMap;

public class VoteMapActivity extends FragmentActivity implements OnMapReadyCallback {

    final private int REQUEST_CODE_VOTE_DATE_ACTIVITY = 1;

    private GoogleMap mMap;
    private UserProfile mCurrentProfile;

    private int InitialBatterieLevel;
    Marker meetingMarker;
    Button mBtnSkip;
    private Firebase mFirebaseMemberRef;
    private Firebase mFirebaseUserProfiles;
    private Firebase mFirebaseMeetingLocations;


    private HashMap<String, LatLng> mMemberCoordinates = new HashMap<>();
    private ArrayList<String> mTeammates = new ArrayList<>();
    private LatLng[] mVoteOptions = new LatLng[3];
    private LatLng averageCoordinates;

    private Integer numberOfMembers = 0;
    private HashMap<String, Integer> locationsVote;

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
        InitialBatterieLevel = (int) getIntent().getExtras().get("batterie");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this);

        mFirebaseMemberRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("members");

        mFirebaseUserProfiles = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("UserProfiles");

        mFirebaseMeetingLocations = new Firebase("https://sizzling-inferno-7505.firebaseio.com/")
                .child("readyGroups")
                .child(mCurrentProfile.groupName)
                .child("meetingLocations");
    }

    private void startVoteDateActivity()
    {
        Intent intent = new Intent(this, VoteDateActivity.class);
        intent.putExtra("profile", mCurrentProfile);
        intent.putExtra("memberCount", numberOfMembers);
		intent.putExtra("batterie", InitialBatterieLevel);
        startActivityForResult(intent, REQUEST_CODE_VOTE_DATE_ACTIVITY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        centerOnSelf();

        locateTeammates();


        //TODO: everyone listens for meeting points change and recuperate points
        mFirebaseMeetingLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        //TODO: evertone shows vote buttons
        //TODO: everyone votes
        //TODO: everyone uploads data
        //TODO: everyone unregisters listeners and moves on to next page


    }




    private void locateTeammates()
    {
        mFirebaseMemberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user : dataSnapshot.getChildren())
                {
                    if (((Boolean)user.getValue()).equals(Boolean.TRUE))
                    {
                        mTeammates.add(user.getKey());
                    }
                }
                mFirebaseUserProfiles.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshotUsers) {
                        double longitudeSum = 0.0;
                        double latitudeSum  = 0.0;
                        for (String teammate : mTeammates)
                        {
                            Double longi = Double.parseDouble( (String) dataSnapshotUsers.child(teammate).child("longitude").getValue());
                            Double lati = Double.parseDouble( (String) dataSnapshotUsers.child(teammate).child("latitude").getValue());

                            if (longi == null)
                                longi = 0.0;
                            if (lati == null)
                                lati = 0.0;

                            longitudeSum += longi;
                            latitudeSum += lati;
                            mMemberCoordinates.put(teammate, new LatLng(lati, longi));
                            numberOfMembers++;
                        }
                        addTeammatesToMap();
                        //on fait la moyenne pour tout le monde car c'est simple à calculer
                        averageCoordinates = new LatLng(longitudeSum/numberOfMembers, latitudeSum/numberOfMembers);
                        if (mCurrentProfile.organizer)
                        {
                            // TODO: remplacer ces valeurs par les lieux calculés
                            // TODO: admin calculates favourite spots and puts them in a list
                            // TODO: admin querries google for favourite spots closest to coord.
                            mFirebaseMeetingLocations.child("location1").child("longitude").setValue(averageCoordinates.longitude - 0.05);
                            mFirebaseMeetingLocations.child("location1").child("latitude").setValue(averageCoordinates.latitude - 0.05);

                            mFirebaseMeetingLocations.child("location2").child("longitude").setValue(averageCoordinates.longitude - 0.1);
                            mFirebaseMeetingLocations.child("location2").child("latitude").setValue(averageCoordinates.latitude + 0.1);

                            mFirebaseMeetingLocations.child("location3").child("longitude").setValue(averageCoordinates.longitude + 0.1);
                            mFirebaseMeetingLocations.child("location3").child("latitude").setValue(averageCoordinates.latitude - 0.1);
                        }

                        // permet d'ajouter les pins sur la map
                        mFirebaseMeetingLocations.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int i = 0;
                                for (DataSnapshot coord : dataSnapshot.getChildren()) {
                                    // Chercher les valeurs
                                    LatLng pos = new LatLng((Double) coord.child("longitude").getValue(), (Double) coord.child("latitude").getValue());

                                    // Afficher les valeurs
                                    mMap.addMarker(new MarkerOptions()
                                            .position(pos)
                                            .title("Option " + (i+1)));

                                    // Inscrire les valeurs dans le tables
                                    mVoteOptions[i] = pos;
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void addTeammatesToMap()
    {
        for (HashMap.Entry<String, LatLng> teammate : mMemberCoordinates.entrySet()) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(teammate.getValue())
                    .title(teammate.getKey()));
            marker.setAlpha((float)0.5);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(meetingMarker.getPosition()));
        }

    }

    private void centerOnSelf()
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentProfile.latitude, mCurrentProfile.longitude)));
    }
}
