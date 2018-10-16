import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;

    private static Scanner input = new Scanner(System.in);

    public static final int COMMAND_PORT = 4444;
    public static final int DATA_PORT = 4445;
    public static final String IP_ADDRESS = "127.0.0.1"; // local host
    public static Mode mode;

    public static void main(String... args) throws IOException, GeneralSecurityException {
        while(true) {
            System.out.println("Choose mode, (M) for master, (F) for follower");
            String in = input.nextLine();
            if(in.equals("M")|| in.equals("m")){
                System.out.println("Mode : MASTER");
                input.close();
                mode = Mode.MASTER;
                Master m = new Master(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
                break;
            }
            else if(in.equals("F")|| in.equals("f")){
                System.out.println("Mode : FOLLOWER");
                System.out.print("Write a folder name for sync :");
                in = input.nextLine();
                input.close();
                mode = Mode.FOLLOWER;
                Follower f = new Follower(in);
                break;
            }
            System.out.println("Invalid input");
        }

    }
}



