package polymtl.inf8405_tp2;
import android.content.Context;
import java.io.FileOutputStream;

/**
 * Created by David on 2016-03-15.
 */
public class UserProfile {

    //Consts
    static String PROFILE_FILE = "userProfile.txt";

    String groupName;
    String email;
    String preferences; //Commat separated format
    Boolean organizer;
    //TODO add Some var type de save the current location

    public UserProfile()
    {

    }

    public Boolean SaveProfile(Context applicationContext)
    {
        String filename = PROFILE_FILE;
        String string = this.ToString();
        FileOutputStream outputStream;


        try {
            outputStream = applicationContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public Boolean LoadProfile(String data)
    {
        String[] values = data.split(";");

        System.out.println("lenght : "+values.length);
        if(values.length != 4)
            return false;

        this.groupName = values[0];
        this.email = values[1];

        this.organizer = false;
        if(values[2].equals("1"))
            this.organizer = true;

        this.preferences = values[3];

        return true;
    }

    public String ToString()
    {
        String strProfile = "";
        String strOrganizer = "0";
        if(organizer)
            strOrganizer = "1";


        strProfile = groupName+";"+email+";"+strOrganizer+";"+preferences;

        return strProfile;
    }
}
