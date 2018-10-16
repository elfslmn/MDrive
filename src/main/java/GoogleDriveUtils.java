/**
 * Created by esalman17 on 7.10.2018.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

// The class contains code snippets from the websites below
// https://developers.google.com/drive/api/v3/quickstart/java
// https://o7planning.org/en/11889/manipulating-files-and-folders-on-google-drive-using-java#
public class GoogleDriveUtils {
    private static final String APPLICATION_NAME = "MDrive";

    /**
     * Global instance of the {@link JsonFactory}.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Directory to store user credentials for this application
     */
    public static final java.io.File CREDENTIALS_FOLDER = DriveQuickstart.CREDENTIALS_FOLDER;
    public static String CREDENTIAL_FILE_NAME = DriveQuickstart.CREDENTIAL_FILE_NAME;

    /**
     * Global instance of the scopes required.
     * If modifying these scopes, delete the StoredCredential file.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_APPDATA);

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    private static Drive _driveService;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(CREDENTIALS_FOLDER);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static Credential getCredentials() throws FileNotFoundException {

        java.io.File credentialFilePath = new java.io.File(CREDENTIALS_FOLDER, CREDENTIAL_FILE_NAME);

        if (!credentialFilePath.exists()) {
            throw new FileNotFoundException("Please copy " + CREDENTIAL_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        InputStream in = null;
        try {
            in = new FileInputStream(credentialFilePath);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Please copy " + CREDENTIAL_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        Credential credential = null;
        try {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                                                                              .setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
            credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return credential;
    }

    /**
     *  Build a new drive object for the application if it has not been already built.
     * @return the drive service
     */
    public static Drive getDriveService() {
        if (_driveService != null) {
            return _driveService;
        }
        Credential credential = null;
        try {
            credential = getCredentials();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        _driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
                .setApplicationName(APPLICATION_NAME).build();
        return _driveService;
    }

    /**
     * Creates a file in the app data of the drive by using the content and name of the local file
     * @param localFile file that be uploaded
     * @return drive file that newly created
     */
    public static final File createFileInAppData(java.io.File localFile){
        System.out.println("Uploading "+ localFile.getName());
        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        fileMetadata.setParents(Collections.singletonList("appDataFolder"));


        //FileContent mediaContent = new FileContent("text/plane", localFile);
        File file = null;
        try {
            String mimeType = Files.probeContentType(localFile.toPath());
            FileContent mediaContent = new FileContent(mimeType, localFile);
            file = getDriveService().files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
        } catch (IOException e) {
            System.err.println("The file cannot be created.");
        }
        System.out.println("Upload completed for "+ localFile.getName());
        return file;
    }

    /**
     * Gets list of the file in app data folder
     * @return map of the files whose keys are file names and values are drive file objects
     */
    public static final HashMap<String, File> getAppDataFileMap() {
        HashMap<String, File> map = new HashMap<String, File>();

        FileList files = null;
        try {
            files = getDriveService().files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name,size,modifiedTime)")
                    .execute();
        } catch (IOException e) {
            System.err.println("File list cannot be obtained. Check your internet connection.");
        }
        //System.out.println("DEBUG: Files in drive:");
        for (File file : files.getFiles()) {
            //System.out.printf("%s \t (%s)\n", file.getName(), file.getId());
            map.put(file.getName(), file);
        }
        return  map;
    }

    //TODO giving error for empty files

    /**
     *  Download the file from the drive into specified folder
     * @param cloudFile file that will be downloaded from the cloud
     * @param folder folder that the file is downloaded into
     * @return local instance of the newly downloaded file
     */
    public static final java.io.File downloadFile(File cloudFile, java.io.File folder){
        System.out.println("Downloading "+cloudFile.getName());
        java.io.File localFile = new java.io.File(folder, cloudFile.getName());
        try {
            OutputStream outputStream = new FileOutputStream(localFile);
            getDriveService().files().get(cloudFile.getId())
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            System.err.println("The file cannot be donwloaded.");
        }
        System.out.println("Download completed for "+cloudFile.getName());
        return localFile;
    }

    /**
     *  Updates the file in the drive by using content of the local file
     * @param fileId  The id of the file in the cloud
     * @param localFile The file whose content will be uploaded
     */
    public static final void updateFile(String fileId, java.io.File localFile){
        System.out.println("Updating "+localFile.getName());
        try {
            String mimeType = Files.probeContentType(localFile.toPath());
            FileContent mediaContent = new FileContent(mimeType, localFile);
            getDriveService().files().update(fileId, new File(), mediaContent).execute();
        } catch (IOException e) {
            System.err.println("The file cannot be updated.");
        }

        System.out.println("Update completed for "+localFile.getName());
        return;
    }

    /**
     * Deletes the file from the cloud
     * @param cloudFile the file that will be deleted
     */
    public static final void deleteFile(File cloudFile){
        System.out.println("Deleting "+cloudFile.getName());
        try {
            getDriveService().files().delete(cloudFile.getId()).execute();
        } catch (IOException e) {
            System.err.println("The file cannot be deleted.");
        }
        System.out.println("Deleting from cloud completed for "+cloudFile.getName());
    }


}
