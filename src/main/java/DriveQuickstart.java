import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// code from https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;
    //TODO "\\" can not be working in Linux try with File constructor
    public static final java.io.File LOCAL_DRIVE_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop\\DriveCloud");

    private static final ScheduledExecutorService driveSyncer = Executors.newScheduledThreadPool(1);

    public static void main(String... args) throws IOException, GeneralSecurityException {
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

        driveSyncer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HashMap<String, File> cloudFiles = GoogleDriveUtils.getAppDataFileMap();
                java.io.File[] localFiles = LOCAL_DRIVE_FOLDER.listFiles();

                LinkedList<java.io.File> needUpload = new LinkedList<>();
                LinkedList<File> needDownload = new LinkedList<>();
                syncedFiles.clear();

                for(java.io.File localFile : localFiles){
                    // The file is only in LOCAL folder, send it to cloud
                    if(!cloudFiles.containsKey(localFile.getName())){
                        needUpload.add(localFile);
                    }else{
                        if(!syncedFiles.containsKey(localFile.getName())) {
                            File cloudFile = cloudFiles.get(localFile.getName());
                            SyncedFile sf = new SyncedFile(localFile, cloudFile, System.currentTimeMillis());
                            syncedFiles.put(localFile.getName(), sf);
                        }
                    }
                }

                for(String fileName: cloudFiles.keySet()){
                    // The file is only in CLOUD folder, download to local
                    if(!syncedFiles.containsKey(fileName)){
                        needDownload.add(cloudFiles.get(fileName));
                    }
                }

                if(needDownload.size() == 0 && needUpload.size()==0){
                    System.out.println("Current time: " + LocalDateTime.now() + ", no update is needed. Already synced!");
                }else{
                    System.out.println("Current time: " + LocalDateTime.now() + ", the following files are going to be synchronized");
                    for(java.io.File f : needUpload){
                        System.out.println(f.getName()+" \tgoing to be uploaded to cloud\t Size= " + f.length()+" bytes");
                    }
                    for(File f : needDownload){
                        System.out.println(f.getName()+" \tgoing to be downloaded from cloud\t Size= " + f.getSize()  +" bytes");
                    }

                    for(java.io.File localFile : needUpload){
                        File cloudFile = GoogleDriveUtils.createFileInAppData(localFile);
                        SyncedFile sf = new SyncedFile(localFile, cloudFile, System.currentTimeMillis());
                        syncedFiles.put(localFile.getName(), sf);
                    }
                    for(File cloudFile : needDownload){
                        java.io.File localFile = GoogleDriveUtils.downloadFile(cloudFile);
                        SyncedFile sf = new SyncedFile(localFile, cloudFile, System.currentTimeMillis());
                        syncedFiles.put(localFile.getName(), sf);
                    }
                    System.out.println("Synchronization done with Google Drive");

                }
            }
        }, 0, 30, TimeUnit.SECONDS);


    }
}