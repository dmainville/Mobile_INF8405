package polymtl.inf8405_tp2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.Time;
import android.util.Base64;

import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 * Created by David on 2016-03-15.
 */
public class UserProfile implements Serializable{

    //Consts
    static String PROFILE_FILE = "userProfile.txt";

    String groupName;
    String email;
    String preferences; //Comma separated format
    Boolean organizer;
    private String profilePictureBase64;
    List<CalendarEvent> events;
    //TODO add Some var type de save the current location

    public UserProfile()
    {

    }

    public String getSanitizedEmail()
    {
        return email.replaceAll("[\\.#$\\[\\]/]", "");
    }

    public Boolean SaveProfile(Context applicationContext)
    {
        String filename = PROFILE_FILE;
        Properties props = this.ToProperties();
        FileOutputStream outputStream;


        try {
            outputStream = applicationContext.openFileOutput(filename, Context.MODE_PRIVATE);
            props.store(outputStream, null);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace(); // if a key or value is not a string
            return false;
        }


        return true;
    }

    public Boolean SaveProfileRemotely(Context applicationContext)
    {
        // Nous n'avons pas vraiment besons d'associer le contexte à Firebase
        // parce qu'on ne lit jamais le résultat. Donc on peut le faire ici
        // au lieu de le mettre dans onCreate de l'applicationContext.
        // On peut changer ceci si nécessaire.
        Firebase.setAndroidContext(applicationContext);

        Firebase myFirebaseRef = new Firebase("https://sizzling-inferno-7505.firebaseio.com/");

        // Utiliser l'adresse courriel comme clé, en enlevant les caractères non acceptés de Firebase '.', '#', '$', '[', ']', '/'
        // TODO: On devrait échapper ASCII 0-31 + 127
        myFirebaseRef.child("UserProfiles").child(getSanitizedEmail()).setValue(this.ToProperties());

        return true;
    }

    public Boolean LoadProfile(InputStream in)
    {
        Properties props = new Properties();

        try {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Unable to read from stream");
            e.printStackTrace();
            return false;
        }

        groupName = props.getProperty("groupname");
        email = props.getProperty("email");
        organizer = Boolean.valueOf(props.getProperty("organizer"));
        preferences = props.getProperty("preferences");
        profilePictureBase64 = props.getProperty("profilePicture");

        return true;
    }

    public Properties ToProperties()
    {
        Properties result = new Properties();
        result.setProperty("groupname", groupName);
        result.setProperty("email", email);
        result.setProperty("organizer", String.valueOf(organizer));
        result.setProperty("preferences", preferences);
        result.setProperty("profilePicture", profilePictureBase64);

        return result;
    }


    /// Compresser le bitmap en PNG et retourner un string base64
    public final static String bitmapToString(Bitmap in){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, bytes); // lossless avec png
        String resultString = Base64.encodeToString(bytes.toByteArray(), Base64.DEFAULT);
        try
        {
            bytes.close();
        }
        catch (IOException e)
        {
            // close a échoué
            e.printStackTrace();
        }
        return resultString;
    }


    // Décoder le string base64 et décompresser le png
    // Returns: null si le string ne peut pas être décodé en bmp
    private Bitmap stringToBitmap(String in){
        if (in != null && ! in.isEmpty())
        {
            byte[] bytes = Base64.decode(in, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else
            return null;
    }

    //Récupère les évènements de l'utilisateur pour le nombre de jours passé en paramère à partir de la journée courrante.
    public void loadUserEvents(int dayCount, Context context)
    {
        Time dayStart = new Time();
        dayStart.setToNow();
        dayStart.hour=0;
        dayStart.minute=0;
        dayStart.second = 0;

        Time dayEnd = new Time();
        dayEnd.set(dayStart);
        dayEnd.yearDay=dayStart.yearDay+dayCount;
        dayEnd.hour=dayStart.hour+23;
        dayEnd.minute=dayStart.minute+59;
        dayEnd.second=dayStart.second+59;

        List<CalendarEvent> events = CalendarEventReader.GetCurrentDeviceCalendarEvents(context,dayStart.toMillis(false),dayEnd.toMillis(false));
        this.events = events;
    }

    public void setProfilePicture(Bitmap bitmap)
    {
        profilePictureBase64 = bitmapToString(bitmap);
    }

    public Bitmap getProfilePicture()
    {
        return stringToBitmap(profilePictureBase64);
    }
}
