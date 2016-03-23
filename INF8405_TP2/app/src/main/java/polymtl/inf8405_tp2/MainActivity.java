package polymtl.inf8405_tp2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final private int REQUEST_CODE_PICTURE = 1;
    final private int REQUEST_CODE_IMAGE = 2;
    final private int REQUEST_CODE_WAITING_ROOM_ACTIVITY = 3;

    final private int MAX_DAYS_BEFORE_EVENT = 3;

    // UI references.
    private ViewSwitcher mViewSwitcher;
    private EditText mGroupName;
    private EditText mEmail;
    private ImageView mPhoto;
    private CheckBox mOrganisateur;
    private Button mBtnPreferences;
    private Button mBtnLogin;
    private ListView mListViewPreferences;
    private Button mBtnConfirmPreferences;
    private EditText mNewPreference;
    private Button mBtnAddPreference;
    private TextView mOrderedList;
    private TextView mLoginPreferences;

    // Acutal application data
    private ArrayList<String> ordrePreferences;
    private ArrayList<String> values = new  ArrayList<>(Arrays.asList("Restaurant", "Café", "Parc", "Pizzéria", "Cafétéria", "École", "Maison"));
    private ArrayAdapter<String> adapter;
    private LocationService locations;
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        mGroupName = (EditText) findViewById(R.id.group_name);
        mEmail = (EditText) findViewById(R.id.email);
        mPhoto = (ImageView) findViewById(R.id.photo);
        mOrganisateur = (CheckBox) findViewById(R.id.organisateur);
        mBtnPreferences = (Button) findViewById(R.id.btn_preferences);
        mBtnLogin = (Button) findViewById(R.id.btn_login);
        mListViewPreferences = (ListView) findViewById(R.id.listView_preferences);
        mBtnConfirmPreferences = (Button) findViewById(R.id.btn_list_confirm);
        mNewPreference = (EditText) findViewById(R.id.new_preference);
        mBtnAddPreference = (Button) findViewById(R.id.btn_add_preference);
        mOrderedList = (TextView) findViewById(R.id.ordered_list);
        mLoginPreferences = (TextView) findViewById(R.id.login_preferences);

        ordrePreferences = new ArrayList<>();

        initializeListener(); ///< Ajouter les listeners aux boutons
        locations = new LocationService(this); ///< commencer à chercher la position
        LoadInitialProfile();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_CODE_IMAGE:
                case REQUEST_CODE_PICTURE:
                    // Transformer l'image retournée en un bitmap, en la downsizant au préalable
                    Uri selectedImageURL = data.getData();
                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImageURL); //throws FileNotFoundException

                        // Chercher la taille de l'image sans la décoder
                        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
                        bfOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(imageStream, null, bfOptions);

                        //réduire la taille
                        final int TARGET_SIZE = 400;

                        // On cherche l'échelle de réduction pour que le width et le heigh soient plus petit que TARGET_SIZE
                        int scale = 1;
                        System.out.println(bfOptions.outHeight + " =========== " + bfOptions.outWidth);
                        for ( ; bfOptions.outWidth / scale > TARGET_SIZE || bfOptions.outWidth / scale > TARGET_SIZE; scale*=2);

                        // On ouvre l'image
                        BitmapFactory.Options bfOptions2 = new BitmapFactory.Options();
                        bfOptions2.inSampleSize = scale;
                        Bitmap imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageURL), null, bfOptions2);

                        // Associer ce bitmap au user
                        currentProfile.setProfilePicture(imageBitmap);

                        // Modifier l'image de profil affichée
                        mPhoto.setImageBitmap(imageBitmap);

                        // Fermer le stream
                        imageStream.close(); //trhows IOException
                    }
                    catch (FileNotFoundException e)
                    {
                        System.err.println("Image File not found");
                        Toast.makeText(this, "Unable to load image",Toast.LENGTH_SHORT).show();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace(); //close stream failed
                    }
                    break;
                case REQUEST_CODE_WAITING_ROOM_ACTIVITY:
                    break;
                default:
                    break;
            }

        }
    }
    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast t = Toast.makeText(this, "Location Services must be active to use the app", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 2000, locations);
                locations.currentLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lm.removeUpdates(locations);

                Toast t = Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT);
                t.show();
            }
            else {
                Toast t = Toast.makeText(this, "Location Services must be active to use the app", Toast.LENGTH_LONG);
                t.show();
            }
        }
    }
*/
    public void LoadInitialProfile()
    {
        currentProfile = new UserProfile();
        FileInputStream file;

        try {
            file = openFileInput(UserProfile.PROFILE_FILE);
        } catch (FileNotFoundException e) {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Could not find a profile to load!", Toast.LENGTH_SHORT);
            toast.show();

            AjustFieldsToProfile();
            return;
        }

        if(currentProfile.LoadProfile(file))
        {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Profile loaded successfully!", Toast.LENGTH_SHORT);
            toast.show();

            AjustFieldsToProfile();
        } else
        {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Failed loading previous profile! ErrCode : 2", Toast.LENGTH_SHORT);
            toast.show();
            AjustFieldsToProfile();
        }
    }

    /// Met les champs à jour avec les valeurs de currentUser
    public void AjustFieldsToProfile()
    {
        mGroupName.setText(currentProfile.groupName);
        mEmail.setText(currentProfile.email);
        mOrganisateur.setChecked(currentProfile.organizer);
        LoadTextPreferences();
        mLoginPreferences.setText(CreateTextPreferences());
        mPhoto.setImageBitmap(currentProfile.getProfilePicture());
    }

    public void SaveCurrentProfile()
    {
        if(currentProfile == null)
            return;

        currentProfile.loadUserEvents(MAX_DAYS_BEFORE_EVENT, this.getApplicationContext());

        if(!currentProfile.SaveProfile(this.getApplicationContext()))
        {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Failed saving user profile locally!", Toast.LENGTH_SHORT);
            toast.show();
        }

        Toast toast = Toast.makeText(this.getApplicationContext(), "Profile saved locally!", Toast.LENGTH_SHORT);
        toast.show();

        if(!currentProfile.SaveProfileRemotely(this.getApplicationContext()))
        {
            Toast toast2 = Toast.makeText(this.getApplicationContext(), "Failed saving user profile remotely!", Toast.LENGTH_SHORT);
            toast.show();
        }

        Toast toast2 = Toast.makeText(this.getApplicationContext(), "Profile saved remotely!", Toast.LENGTH_SHORT);
        toast2.show();

    }

    private void startWaitingRoomActivity()
    {
        //TODO autoriser seulement un admin par room

        Intent intent = new Intent(this, WaitingRoomActivity.class);
        intent.putExtra("profile", currentProfile);
        startActivityForResult(intent, REQUEST_CODE_WAITING_ROOM_ACTIVITY);
    }

    public void initializeListener() {

        mBtnPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewSwitcher.showNext();
            }
        });

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VerifyInformation())
                {
                    //locations.UpdateLocation(MainActivity.this);
                    Register();
                    // Procéder à la prochaine activité
                    startWaitingRoomActivity();
                }
            }
        });



        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, values);
        mListViewPreferences.setAdapter(adapter);

        mListViewPreferences.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String object = mListViewPreferences.getItemAtPosition(position).toString();
                if (!ordrePreferences.contains(object)) {
                    ordrePreferences.add(object);
                } else {
                    ordrePreferences.remove(object);
                }

                mOrderedList.setText(CreateTextPreferences());
            }
        });

        mBtnAddPreference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNewPreference.getText().length() > 0 && !values.contains(mNewPreference.getText().toString())) {
                    values.add(mNewPreference.getText().toString());
                    adapter.notifyDataSetChanged();
                }
            }
        });

        mBtnConfirmPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewSwitcher.showPrevious();
                mLoginPreferences.setText(CreateTextPreferences());
            }
        });
    }

    public void selectPicture(){
        final CharSequence[] items = { "Prendre une photo", "Choisir une image", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Prendre une photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CODE_PICTURE);
                } else if (items[item].equals("Choisir une image")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            REQUEST_CODE_IMAGE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public boolean VerifyInformation() {
        boolean returnValue = true;

        mGroupName.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (mGroupName.getText().length() == 0) {
            mGroupName.setBackgroundColor(Color.parseColor("#fd8282"));
            returnValue = false;
        }

        mEmail.setBackgroundColor(Color.parseColor("#FFFFFF"));
        if (!mEmail.getText().toString().contains("@")) {
            mEmail.setBackgroundColor(Color.parseColor("#fd8282"));
            returnValue = false;
        }

        if(ordrePreferences.size() < 3){
            Toast t = Toast.makeText(this,"Vous devez choisir au moins 3 preferences!",Toast.LENGTH_SHORT);
            t.show();
            returnValue = false;
        }

        if(locations.currentLocation == null)
        {
            Toast t = Toast.makeText(this,"Vous devez:\n- Activer la geolocalisation\n- Attendre pour avoir plus de précision.\nActivez le Wifi pour améliorer la précision.",Toast.LENGTH_LONG);
            t.show();
            returnValue = false;
        }

        return returnValue;
    }

    public void Register() {

        currentProfile.groupName = mGroupName.getText().toString();
        currentProfile.email = mEmail.getText().toString();
        currentProfile.preferences = CreateTextPreferences();
        currentProfile.organizer = mOrganisateur.isChecked();
        currentProfile.latitude = locations.currentLocation.getLatitude();
        currentProfile.longitude = locations.currentLocation.getLongitude();

        SaveCurrentProfile();
    }

    public void TestCalendar()
    {
        System.out.println("Testing Calendar");

        Time dayStart = new Time();
        dayStart.setToNow();
        dayStart.hour=0;
        dayStart.minute=0;
        dayStart.second = 0;

        Time dayEnd = new Time();
        dayEnd.set(dayStart);
        dayEnd.hour=dayStart.hour+23;
        dayEnd.minute=dayStart.minute+59;
        dayEnd.second=dayStart.second+59;

        List<CalendarEvent> events = CalendarEventReader.GetCurrentDeviceCalendarEvents(this.getApplicationContext(),dayStart.toMillis(false),dayEnd.toMillis(false));
    }

    public String CreateTextPreferences(){
        String text = "";
        for (int i = 0; i < ordrePreferences.size(); i++) {
            if (i > 0)
                text += ", ";
            text += i + 1 + ": " + ordrePreferences.get(i);
        }
        return text;
    }

    public void LoadTextPreferences()
    {
        if (currentProfile.preferences != null && currentProfile.preferences != "")
        {
            String[] prefs = currentProfile.preferences.split(",");

            for (int i = 0; i < prefs.length; i++) {
                String currentPref = prefs[i].split(":")[1].trim();
                ordrePreferences.add(currentPref);

                int index = FindPreferenceItem(currentPref);
                if (index == -1) {
                    values.add(currentPref);
                    adapter.notifyDataSetChanged();
                    mListViewPreferences.setItemChecked(values.size() - 1, true);
                } else {
                    mListViewPreferences.setItemChecked(index, true);
                }
            }
        }
    }

    public int FindPreferenceItem(String itemName)
    {
        for(int i=0; i<mListViewPreferences.getCount(); i++)
        {
            if(itemName.equals( mListViewPreferences.getItemAtPosition(i).toString()))
                return i;
        }

        return -1;
    }
}