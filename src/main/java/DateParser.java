import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by esalman17 on 16.10.2018.
 */

public class DateParser {
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);


    public static Date parse(String s){
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date  = format.parse(s);
        }
        catch (Exception e) {
            System.err.println("Data cannot be parsed.");
        }
        return date;
    }




}
