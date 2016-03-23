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
import java.util.Calendar;
import java.util.Date;
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
    Double longitude;
    Double latitude;
    private String profilePictureBase64 = "";
    List<CalendarEvent> events;
    //TODO add Some var type de save the current location

    public UserProfile()
    {
        organizer = false;
        //TODO: put better default coords, like Poly?
        longitude = 0.0;
        latitude = 0.0;
        profilePictureBase64 = "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";

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
            System.out.println("Unable to read from stream");
            return false;
        }



        groupName = props.getProperty("groupname", "");
        email = props.getProperty("email", "");
        organizer = Boolean.valueOf(props.getProperty("organizer", "false"));
        preferences = props.getProperty("preferences", "");
        //TODO: put better default coords, like Poly?
        longitude = Double.parseDouble(props.getProperty("longitude", "0.0"));
        latitude = Double.parseDouble(props.getProperty("latitude", "0.0"));
        profilePictureBase64 = props.getProperty("profilePicture", "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");

        return true;
    }

    public Properties ToProperties()
    {
        Properties result = new Properties();
        if (groupName != null) result.setProperty("groupname", groupName);
        if (email != null) result.setProperty("email", email);
        if (organizer != null) result.setProperty("organizer", String.valueOf(organizer));
        if (preferences != null) result.setProperty("preferences", preferences);
        if (longitude != null) result.setProperty("longitude", String.valueOf(longitude));
        if (latitude != null) result.setProperty("latitude", String.valueOf(latitude));
        if (profilePictureBase64 != null) result.setProperty("profilePicture", profilePictureBase64);
        if(events!=null)
        {
            /*String strEvents = "";
            for(CalendarEvent event : events)
            {
                if(strEvents!="")
                    strEvents+=";";
                strEvents+= events.toString();
            }
            result.setProperty("events", strEvents);*/
            result.setProperty("events", events.toString());
        }

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
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(new Date());

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(new Date());
        endDate.add(Calendar.DATE,dayCount);

        List<CalendarEvent> events = CalendarEventReader.GetCurrentDeviceCalendarEvents(context,startDate.getTimeInMillis(),endDate.getTimeInMillis());
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
