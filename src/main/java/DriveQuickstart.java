import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

// code from https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class DriveQuickstart {

    private static final java.io.File CREDENTIALS_FOLDER =  GoogleDriveUtils.CREDENTIALS_FOLDER;
    private static final String CLIENT_SECRET_FILE_NAME = GoogleDriveUtils.CLIENT_SECRET_FILE_NAME;

    private static final java.io.File LOCAL_DRIVE_FOLDER = new java.io.File(System.getProperty("user.home"), "Desktop\\DriveCloud");

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

        java.io.File localFile = new java.io.File(LOCAL_DRIVE_FOLDER, "deneme.txt");
        System.out.println("Upload file: "+localFile.getAbsolutePath());
        File cloudFile = GoogleDriveUtils.createFileInAppData(localFile);

        GoogleDriveUtils.getAppDataFileList();

        java.io.File downloadedFile = new java.io.File(LOCAL_DRIVE_FOLDER, "deneme2.txt");
        GoogleDriveUtils.downloadFile(cloudFile.getId(),downloadedFile);

        java.io.File updatedFile = new java.io.File(LOCAL_DRIVE_FOLDER, "deneme_new.txt");
        GoogleDriveUtils.updateFile(cloudFile.getId(),updatedFile );

        java.io.File downloaded2File = new java.io.File(LOCAL_DRIVE_FOLDER, "deneme4.txt");
        GoogleDriveUtils.downloadFile(cloudFile.getId(),downloaded2File);

    }
}