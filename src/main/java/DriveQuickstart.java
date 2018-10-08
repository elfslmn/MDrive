import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

// code from https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;
    //TODO "\\" can not be working in Linux try with File constructor
    public static final java.io.File LOCAL_DRIVE_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop\\DriveCloud");

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

        HashMap<String, File> cloudFiles = GoogleDriveUtils.getAppDataFileMap();

        HashMap<String, SyncedFile> mFiles = new HashMap<String, SyncedFile>(); //TODO may use syncronized Map

        java.io.File[] localFiles = LOCAL_DRIVE_FOLDER.listFiles();
        for(java.io.File localFile : localFiles){
            File cloudFile;
            // The file is only in LOCAL folder, send it to cloud
            if(!cloudFiles.containsKey(localFile.getName())){
                cloudFile = GoogleDriveUtils.createFileInAppData(localFile);
            }else{
                cloudFile = cloudFiles.get(localFile.getName());
            }
            SyncedFile sf = new SyncedFile(localFile, cloudFile, System.currentTimeMillis());
            mFiles.put(localFile.getName(), sf);
        }

        for(String fileName: cloudFiles.keySet()){
            // The file is only in CLOUD folder, download to local
            if(!mFiles.containsKey(fileName)){
                File cloudFile = cloudFiles.get(fileName);
                java.io.File localFile = GoogleDriveUtils.downloadFile(cloudFile);
                SyncedFile sf = new SyncedFile(localFile, cloudFile, System.currentTimeMillis());
                mFiles.put(localFile.getName(), sf);
            }
        }

        GoogleDriveUtils.getAppDataFileMap();

    }
}