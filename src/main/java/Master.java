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
     * Local master file
     */
    public static java.io.File LOCAL_DRIVE_FOLDER = DriveQuickstart.LOCAL_DRIVE_FOLDER;

    /**
     * It schedule a synchronization job for every 30 seconds
     */
    private static final ScheduledExecutorService driveSynchroniser = Executors.newScheduledThreadPool(5);

    /**
     * Keeps track of synced files
     * key = file name, value = synced file that keeps drive-local file pair
     */
    public static final HashMap<String, SyncedFile> syncedFiles = new HashMap<String, SyncedFile>(); //TODO may use syncronized Map

    /**
     *  Creates a master object
     */
    public Master(){

        // Schedule GoogleDrive - master sync jobs
        driveSynchroniser.scheduleWithFixedDelay(new MasterCloudSynchronizer(), 0, 30, TimeUnit.SECONDS);

        // 5: Open a server socket and wait for followers to connect
 /*       ServerSocket commandServerSocket = new ServerSocket(DriveQuickstart.COMMAND_PORT);
        System.out.println("Master waits for followers...");
        Socket commandSocket = commandServerSocket.accept();
        new ServerThread(commandSocket).start();*/

    }
}
