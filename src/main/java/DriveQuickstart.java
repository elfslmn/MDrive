import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// code from https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;

    private static final java.io.File DESKTOP_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop");
    public static java.io.File LOCAL_DRIVE_FOLDER;

    private static final ScheduledExecutorService driveSyncer = Executors.newScheduledThreadPool(1);
    private static long lastSync;

    private static Scanner input = new Scanner(System.in);
    private static Mode mode = null;

    public static void main(String... args) throws IOException, GeneralSecurityException {

        while(true) {
            System.out.println("Choose mode, (1) for master, (0) for follower");
            int i = input.nextInt();
            if(i == 1){
                System.out.println("Mode : MASTER");
                mode = Mode.MASTER;
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "DriveCloudMaster");
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
            else if(i == 0){
                mode = Mode.FOLLOWER;
                LOCAL_DRIVE_FOLDER = new java.io.File(DESKTOP_FOLDER, "DriveCloudFollower");
                System.out.println("Mode : FOLLOWER");
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

        final HashMap<String, SyncedFile> syncedFiles = new HashMap<String, SyncedFile>(); //TODO may use syncronized Map

        driveSyncer.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                HashMap<String, File> cloudFiles = GoogleDriveUtils.getAppDataFileMap();
                java.io.File[] localFiles = LOCAL_DRIVE_FOLDER.listFiles();

                LinkedList<java.io.File> needUpload = new LinkedList<>();
                LinkedList<File> needDelete = new LinkedList<>();
                LinkedList<java.io.File> needUpdate = new LinkedList<>();
                syncedFiles.clear();

                for(java.io.File localFile : localFiles){
                    // The file is only in LOCAL folder, send it to cloud
                    if(!cloudFiles.containsKey(localFile.getName())){
                        needUpload.add(localFile);
                    }else{
                        // The file in the local is updated since last sync
                        if(localFile.lastModified() > lastSync){
                            needUpdate.add(localFile);
                            syncedFiles.put(localFile.getName(), null);
                        }else {
                            File cloudFile = cloudFiles.get(localFile.getName());
                            SyncedFile sf = new SyncedFile(localFile, cloudFile);
                            syncedFiles.put(localFile.getName(), sf);
                        }

                    }
                }

                for(String cloudFile: cloudFiles.keySet()){
                    // The file is only in CLOUD folder, delete from local
                    if(!syncedFiles.containsKey(cloudFile)){
                        needDelete.add(cloudFiles.get(cloudFile));
                    }
                }

                if(needDelete.size() == 0 && needUpload.size()==0 && needUpdate.size()==0){
                    System.out.println("Current time: " + LocalDateTime.now() + ",\tno update is needed. Already synced!");
                }else{
                    // Print planning updates
                    System.out.println("Current time: " + LocalDateTime.now() + ",\tthe following files are going to be synchronized");
                    for(java.io.File f : needUpload){
                        System.out.println(f.getName()+" \tgoing to be uploaded to cloud\t Size= " + f.length()+" bytes");
                    }
                    for(File f : needDelete){
                        System.out.println(f.getName()+" \tgoing to be deleted from cloud\t Size= " + f.getSize()  +" bytes");
                    }
                    for(java.io.File f : needUpdate){
                        System.out.println(f.getName()+" \tgoing to be updated in cloud\t Size= " + f.length()  +" bytes");
                    }

                    // Apply planning updates
                    for(java.io.File localFile : needUpload){
                        File cloudFile = GoogleDriveUtils.createFileInAppData(localFile);
                        SyncedFile sf = new SyncedFile(localFile, cloudFile);
                        syncedFiles.put(localFile.getName(), sf);
                    }
                    for(File cloudFile : needDelete){
                        GoogleDriveUtils.deleteFile(cloudFile);
                    }
                    for(java.io.File localFile : needUpdate){
                        File cloudFile = cloudFiles.get(localFile.getName());
                        GoogleDriveUtils.updateFile(cloudFile.getId(), localFile);
                        SyncedFile sf = new SyncedFile(localFile, cloudFile);
                        syncedFiles.put(localFile.getName(), sf);

                    }
                    System.out.println("Current time: "+LocalDateTime.now()+ ",\tSynchronization done with Google Drive");
                }
                lastSync = System.currentTimeMillis();
            }
        }, 0, 30, TimeUnit.SECONDS);


        while(true){
            String s = input.nextLine();
            if(s.equals("quit")){
                System.out.println("Program will be closed");
                driveSyncer.shutdownNow();
                break;
            }
        }
        input.close();
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