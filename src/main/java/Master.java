import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by esalman17 on 10.10.2018.
 */

public class Master {
    /**
     * Desktop folder of the current user
     */
    private static final java.io.File DESKTOP_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop");

    /**
     * Local master file
     */
    public static java.io.File LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "MasterDriveCloud");

    /**
     * The time of the last synchronization
     */
    public static long lastSync;

    /**
     * It schedule a synchronization job for every 30 seconds
     */
    private static final ScheduledExecutorService driveSynchroniser = Executors.newScheduledThreadPool(5);

    /**
     * Keeps track of synced files
     * key = file name, value = synced file that keeps drive-local file pair
     */
    public static final HashMap<String, SyncedFile> syncedFiles = new HashMap<String, SyncedFile>(); //TODO may use syncronized Map

    public Master(){};

    /**
     *  Creates a master object
     * @param CREDENTIALS_FOLDER : Folder that your credential file is in it.
     * @param CLIENT_SECRET_FILE_NAME : The name of your json file
     */
    public Master(java.io.File CREDENTIALS_FOLDER, String CLIENT_SECRET_FILE_NAME){
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

        // 3: Get the last sync time
        try {
            FileReader fr = new FileReader("master_data.txt");
            BufferedReader br = new BufferedReader(fr);
            String s = br.readLine();
            lastSync = Long.parseLong(s);
        }catch (IOException ex){
            lastSync = -1;
        }
        // 4: Schedule GoogleDrive - master sync jobs
        driveSynchroniser.scheduleWithFixedDelay(new MasterCloudSynchronizer(), 0, 30, TimeUnit.SECONDS);

        // 5: Open a server socket and wait for followers to connect
 /*       ServerSocket commandServerSocket = new ServerSocket(DriveQuickstart.COMMAND_PORT);
        System.out.println("Master waits for followers...");
        Socket commandSocket = commandServerSocket.accept();
        new ServerThread(commandSocket).start();*/

    }
}
