
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    public static final HashMap<String, SyncedFile> syncedFiles = new HashMap<String, SyncedFile>(); //TODO may use syncronized Map

    public static void main(String... args) throws IOException, GeneralSecurityException {

        while(true) {
            System.out.println("Choose mode, (M) for master, (F) for follower");
            String in = input.nextLine();
            if(in.equals("M")|| in.equals("m")){
                System.out.println("Mode : MASTER");
                mode = Mode.MASTER;
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "MasterDriveCloud");
                try {
                    FileReader fr = new FileReader("master_data.txt");
                    BufferedReader br = new BufferedReader(fr);
                    String s = br.readLine();
                    lastSync = Long.parseLong(s);
                }catch (IOException ex){
                    lastSync = -1;
                }

                break;
            }
            else if(in.equals("F")|| in.equals("f")){
                System.out.println("Mode : FOLLOWER");
                mode = Mode.FOLLOWER;
                // TODO handle for multiple followers
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "FollowerDriveCloud");
                try {
                    FileReader fr = new FileReader("follower_data.txt");
                    BufferedReader br = new BufferedReader(fr);
                    String s = br.readLine();
                    lastSync = Long.parseLong(s);
                }catch (IOException ex){
                    lastSync = -1;
                }

                break;
            }
            System.out.println("Invalid input");
        }

        // 1: Create CREDENTIALS_FOLDER
        if (!CREDENTIALS_FOLDER.exists()) {
            CREDENTIALS_FOLDER.mkdirs();
            System.out.println("Created Folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
            System.out.println("Copy file " + CLIENT_SECRET_FILE_NAME + " into folder above.. and rerun this class!!");
            return;
        }

        // 2: Create DriveCloud folder if it does not exist in local
        if (!LOCAL_DRIVE_FOLDER.exists()) {
            LOCAL_DRIVE_FOLDER.mkdirs();
            System.out.println("Created Sync Folder: " + LOCAL_DRIVE_FOLDER.getAbsolutePath());
        }

        if(mode == Mode.MASTER) {
            driveSynchroniser.scheduleWithFixedDelay(new MasterCloudSynchronizer(), 0, 30, TimeUnit.SECONDS);
        }

        while(true){
            String s = input.nextLine();
            if(s.equals("quit")){
                System.out.println("Program will be closed");
                if(mode == Mode.MASTER) driveSynchroniser.shutdownNow();
                break;
            }
        }
        input.close();

        // Save the last sync time for further use of project
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
        fw.close();

        return;
    }
}



