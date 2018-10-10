import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by esalman17 on 10.10.2018.
 */

public class Follower {
    private static final java.io.File DESKTOP_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop");
    public static java.io.File LOCAL_DRIVE_FOLDER;
    public static long lastSync;

    public Follower(String localFolderName){
        LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, localFolderName);

        // 1: Create DriveCloud folder if it does not exist in local
        if (!LOCAL_DRIVE_FOLDER.exists()) {
            LOCAL_DRIVE_FOLDER.mkdirs();
            System.out.println("Created Sync Folder: " + LOCAL_DRIVE_FOLDER.getAbsolutePath());
        }

        // 2: Get the last sync time
        try {
            FileReader fr = new FileReader(localFolderName+".txt");
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();
            lastSync = Long.parseLong(s);
        }catch (IOException ex){
            lastSync = -1;
        }

        // 3: Connect to master
        try {
            Socket commandClientSocket = new Socket(DriveQuickstart.IP_ADDRESS, DriveQuickstart.COMMAND_PORT);
            DataOutputStream os_command = new DataOutputStream(commandClientSocket.getOutputStream());
            BufferedReader is_command = new BufferedReader(new InputStreamReader(commandClientSocket.getInputStream()));
            os_command.writeBytes("Hello, I am follower\n");
            String line = is_command.readLine();
            System.out.println("Master sent : " + line);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
