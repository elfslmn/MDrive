import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class DriveQuickstart {

    /**
     * Desktop folder of the current user
     */
    private static final java.io.File DESKTOP_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop");
    /**
     * Directory to store user credentials for this application
     */
    public static final java.io.File CREDENTIALS_FOLDER = new java.io.File("Drive Credentials");
    public static String CREDENTIAL_FILE_NAME = "credentials.json";

    /**
     * Local drive folder that going to be synced
     */
    public static java.io.File LOCAL_DRIVE_FOLDER;

    private static Scanner input = new Scanner(System.in);

    public static void main(String... args) throws IOException, GeneralSecurityException {
        while(true) {
            System.out.println("Choose mode, (M) for master, (F) for follower");
            String in = input.nextLine();

            if(in.equals("M")|| in.equals("m")){
                System.out.println("Mode : MASTER");
                input.close();

                //Create DriveCloud folder if it does not exist in local
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "MasterDriveCloud");
                if (!LOCAL_DRIVE_FOLDER.exists()) {
                    LOCAL_DRIVE_FOLDER.mkdirs();
                    System.out.println("Created Sync Folder: " + LOCAL_DRIVE_FOLDER.getAbsolutePath());
                }

                if (!CREDENTIALS_FOLDER.exists()) {
                    CREDENTIALS_FOLDER.mkdirs();
                    System.out.println("Created Folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
                    System.out.println("Copy " +  CREDENTIAL_FILE_NAME + " file into this folder above.. and rerun this class!!");
                    return;
                }

                if(!new java.io.File(CREDENTIALS_FOLDER, CREDENTIAL_FILE_NAME).exists()){
                    System.out.println("Copy " +  CREDENTIAL_FILE_NAME +" file into "+ CREDENTIALS_FOLDER.getAbsolutePath() + " folder and rerun this class!!");
                    return;
                }

                Master m = new Master();
                break;
            }
            else if(in.equals("F")|| in.equals("f")){
                System.out.println("Mode : FOLLOWER");
                System.out.print("Write a folder name for sync :");
                String folder = input.nextLine();
                input.close();

                //Create DriveCloud folder if it does not exist in local
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, folder);
                if (!LOCAL_DRIVE_FOLDER.exists()) {
                    LOCAL_DRIVE_FOLDER.mkdirs();
                    System.out.println("Created Sync Folder: " + LOCAL_DRIVE_FOLDER.getAbsolutePath());
                }

                Follower f = new Follower(in);
                break;
            }
            System.out.println("Invalid input");
        }

    }
}



