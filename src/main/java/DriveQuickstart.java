
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// code from https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;

    private static final java.io.File DESKTOP_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop");
    public static java.io.File LOCAL_DRIVE_FOLDER;

    private static final ScheduledExecutorService driveSynchroniser = Executors.newScheduledThreadPool(1);
    public static long lastSync;

    private static Scanner input = new Scanner(System.in);
    private static Mode mode = null;


    public static final int COMMAND_PORT = 4444;
    public static final int DATA_PORT = 4445;
    public static final String IP_ADDRESS = "127.0.0.1"; // local host

    public static void main(String... args) throws IOException, GeneralSecurityException {
        while(true) {
            System.out.println("Choose mode, (M) for master, (F) for follower");
            String in = input.nextLine();
            if(in.equals("M")|| in.equals("m")){
                System.out.println("Mode : MASTER");
                mode = Mode.MASTER;
                Master m = new Master(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
                break;
            }
            else if(in.equals("F")|| in.equals("f")){
                System.out.println("Mode : FOLLOWER");
                System.out.print("Write a folder name for sync :");
                in = input.nextLine();
                input.close();
                Follower f = new Follower(in);
                break;
            }
            System.out.println("Invalid input");
        }

 /*       // Save the last sync time for further use of project
        FileWriter fw;
        if(mode == Mode.MASTER) {
            fw = new FileWriter("master_data.txt");
        }
        else{
            fw = new FileWriter("follower_data.txt");
        }
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(""+lastSync);

        bw.close();
        fw.close(); */
    }
}



