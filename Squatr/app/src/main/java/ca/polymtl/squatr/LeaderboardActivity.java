package ca.polymtl.squatr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LeaderboardActivity extends AppCompatActivity {

    private ArrayList<Flag> mFlagList;

    private HashMap<String, Integer> mLeaderboardHashMap;
    private ArrayList<Pair> mLeaderboardArray;
    private ArrayAdapter mLeaderboardListAdapter;
    private ListView mLeaderboardListView;

    private HashMap<String, Integer> mLeaderboardMazeHashMap;
    private ArrayList<Pair> mLeaderboardMazeArray;
    private ArrayAdapter mLeaderboardMazeListAdapter;
    private ListView mLeaderboardMazeListView;

    private HashMap<String, Integer> mLeaderboardLightHashMap;
    private ArrayList<Pair> mLeaderboardLightArray;
    private ArrayAdapter mLeaderboardLightListAdapter;
    private ListView mLeaderboardLightListView;

    private String mUsername;
    private Firebase mFirebaseRef;
    private TextView mTbBatterie;
    private int initialBatterieLevel = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Bundle data = getIntent().getExtras();
        mUsername = data.getString("Username");
        initialBatterieLevel = data.getInt("Battery");

        mFlagList = new ArrayList<>();
        SetListViews();

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://projetinf8405.firebaseio.com/");
        // request update from db for flag locations and add change listener

        mFirebaseRef.child("flags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LoadAllFlag(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

        //Écouté le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onStop() {
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch(Exception ignored){ }
        super.onStop();
    }

    @Override
    protected void onStart() {
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        super.onStart();
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {

            //Récupérer le niveau actuel de la batterie
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            if(initialBatterieLevel == -1)
                initialBatterieLevel = level;

            int consommation  = initialBatterieLevel-level;
            //Consommation de batterie : 0%

            //Afficher la différence avec le niveau initial
            mTbBatterie.setText(getString(R.string.batteryLabel)+consommation+"%");
        }
    };

    private void SetListViews(){
        mLeaderboardArray = new ArrayList<>();
        mLeaderboardListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mLeaderboardArray);
        mLeaderboardListView = (ListView) findViewById(R.id.leaderboardListView);
        mLeaderboardListView.setAdapter(mLeaderboardListAdapter);

        mLeaderboardMazeArray = new ArrayList<>();
        mLeaderboardMazeListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mLeaderboardMazeArray);
        mLeaderboardMazeListView = (ListView) findViewById(R.id.leaderboardMazeListView);
        mLeaderboardMazeListView.setAdapter(mLeaderboardMazeListAdapter);

        mLeaderboardLightArray = new ArrayList<>();
        mLeaderboardLightListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mLeaderboardLightArray);
        mLeaderboardLightListView = (ListView) findViewById(R.id.leaderboardLightListView);
        mLeaderboardLightListView.setAdapter(mLeaderboardLightListAdapter);
    }

    private void LoadAllFlag(DataSnapshot snapshot) {
        mFlagList.clear();

        // Add each child to our local array
        for (DataSnapshot flag : snapshot.getChildren())
        {
            String name = flag.getKey();
            String owner = flag.child("owner").getValue(String.class);
            Double latitude = flag.child("coords").child("latitude").getValue(Double.class);
            Double longitude = flag.child("coords").child("longitude").getValue(Double.class);
            Integer highscore = flag.child("highscore").getValue(Integer.class);
            String game = flag.child("game").getValue(String.class);

            if (highscore == null)
            {
                highscore = 0;
            }
            if (name != null && latitude != null && longitude != null)
            {
                Flag newFlag = new Flag(latitude, longitude, name, owner, highscore, game);
                mFlagList.add(newFlag);
            }
        }
        SortLeaderboard();
    }

    private void SortLeaderboard() {
        if(mLeaderboardHashMap == null)
            mLeaderboardHashMap = new HashMap<>();
        else {
            mLeaderboardArray.clear();
            mLeaderboardHashMap.clear();
        }

        if(mLeaderboardMazeHashMap == null)
            mLeaderboardMazeHashMap = new HashMap<>();
        else {
            mLeaderboardMazeArray.clear();
            mLeaderboardMazeHashMap.clear();
        }

        if(mLeaderboardLightHashMap == null)
            mLeaderboardLightHashMap = new HashMap<>();
        else {
            mLeaderboardLightArray.clear();
            mLeaderboardLightHashMap.clear();
        }

        for(Flag flag : mFlagList){
            if (mLeaderboardHashMap.containsKey(flag.owner))
                mLeaderboardHashMap.put(flag.owner, mLeaderboardHashMap.get(flag.owner)+1);
            else
                mLeaderboardHashMap.put(flag.owner,1);

            if(flag.game.equals("maze")) {
                if (mLeaderboardMazeHashMap.containsKey(flag.owner))
                    mLeaderboardMazeHashMap.put(flag.owner, mLeaderboardMazeHashMap.get(flag.owner) + 1);
                else
                    mLeaderboardMazeHashMap.put(flag.owner, 1);
            }

            if(flag.game.equals("light")){
                if (mLeaderboardLightHashMap.containsKey(flag.owner))
                    mLeaderboardLightHashMap.put(flag.owner, mLeaderboardLightHashMap.get(flag.owner) + 1);
                else
                    mLeaderboardLightHashMap.put(flag.owner, 1);
            }
        }

        for(String name : mLeaderboardHashMap.keySet())
        {
            if(mLeaderboardArray.isEmpty())
                mLeaderboardArray.add(new Pair(name, mLeaderboardHashMap.get(name)));
            else{
                boolean insert = false;
                for(int i = 0; i < mLeaderboardArray.size() && !insert; i++){
                    if(mLeaderboardHashMap.get(name) > mLeaderboardArray.get(i).second) {
                        mLeaderboardArray.add(i, new Pair(name, mLeaderboardHashMap.get(name)));
                        insert = true;
                    }
                }
                if(!insert)
                    mLeaderboardArray.add(new Pair(name,mLeaderboardHashMap.get(name)));
            }
        }

        for(String name : mLeaderboardMazeHashMap.keySet())
        {
            if(mLeaderboardMazeArray.isEmpty())
                mLeaderboardMazeArray.add(new Pair(name, mLeaderboardMazeHashMap.get(name)));
            else{
                boolean insert = false;
                for(int i = 0; i < mLeaderboardMazeArray.size() && !insert; i++){
                    if(mLeaderboardMazeHashMap.get(name) > mLeaderboardMazeArray.get(i).second) {
                        mLeaderboardMazeArray.add(i, new Pair(name, mLeaderboardMazeHashMap.get(name)));
                        insert = true;
                    }
                }
                if(!insert)
                    mLeaderboardMazeArray.add(new Pair(name,mLeaderboardMazeHashMap.get(name)));
            }
        }

        for(String name : mLeaderboardLightHashMap.keySet())
        {
            if(mLeaderboardLightArray.isEmpty())
                mLeaderboardLightArray.add(new Pair(name, mLeaderboardLightHashMap.get(name)));
            else{
                boolean insert = false;
                for(int i = 0; i < mLeaderboardLightArray.size() && !insert; i++){
                    if(mLeaderboardLightHashMap.get(name) > mLeaderboardLightArray.get(i).second) {
                        mLeaderboardLightArray.add(i, new Pair(name, mLeaderboardLightHashMap.get(name)));
                        insert = true;
                    }
                }
                if(!insert)
                    mLeaderboardLightArray.add(new Pair(name,mLeaderboardLightHashMap.get(name)));
            }
        }

        mLeaderboardListAdapter.notifyDataSetChanged();
        mLeaderboardMazeListAdapter.notifyDataSetChanged();
        mLeaderboardLightListAdapter.notifyDataSetChanged();
    }

    public class Pair{
        String first;
        Integer second;

        Pair(String s, Integer i){
            first = s;
            second = i;
        }

        public String toString(){
            return first + " - " + second.toString();
        }
    }
}
