/**
 * Created by esalman17 on 7.10.2018.
 */
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

public class GoogleDriveUtils {
    private static final String APPLICATION_NAME = "MDrive";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Directory to store user credentials for this application.
    public static final java.io.File CREDENTIALS_FOLDER = new java.io.File(System.getProperty("user.home"), "credentials");
    public static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved structured credentials.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_APPDATA);
    //private static final List<String> SCOPES = Arrays.asList(AppsactivityScopes.ACTIVITY);

    // Global instance of the {@link FileDataStoreFactory}.
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    // Global instance of the HTTP transport.
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
    public static Credential getCredentials() throws IOException {

        java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

        if (!clientSecretFilePath.exists()) {
            throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

        return credential;
    }

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

    public static final File createDriveFolder(String folderIdParent, String folderName) {

        File fileMetadata = new File();

        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (folderIdParent != null) {
            List<String> parents = Arrays.asList(folderIdParent);
            fileMetadata.setParents(parents);
        }

        // Create a Folder.
        // Returns File object with id & name fields will be assigned values
        File file = null;
        try {
            file = getDriveService().files().create(fileMetadata).setFields("id, name").execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    // com.google.api.services.drive.model.File
    public static final List<File> getGoogleSubFolderByName(String googleFolderIdParent, String subFolderName) {

        String pageToken = null;
        List<File> list = new ArrayList<File>();

        String query = null;
        if (googleFolderIdParent == null) {
            query = " name = '" + subFolderName + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' " //
                    + " and 'root' in parents";
        } else {
            query = " name = '" + subFolderName + "' " //
                    + " and mimeType = 'application/vnd.google-apps.folder' " //
                    + " and '" + googleFolderIdParent + "' in parents";
        }

        do {
            FileList result = null;
            try {
                result = getDriveService().files().list().setQ(query).setSpaces("drive") //
                        .setFields("nextPageToken, files(id, name, createdTime)")//
                        .setPageToken(pageToken).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (File file : result.getFiles()) {
                list.add(file);
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        //
        return list;
    }

    // com.google.api.services.drive.model.File
    public static final List<File> getGoogleRootFoldersByName(String subFolderName){
        return getGoogleSubFolderByName(null,subFolderName);
    }

    public static final File createFileInAppData(java.io.File localFile){
        System.out.println("Uploading "+ localFile.getName());
        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        fileMetadata.setParents(Collections.singletonList("appDataFolder"));

        FileContent mediaContent = new FileContent("text/plane", localFile);
        File file = null;
        try {
            file = getDriveService().files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Upload completed for "+ localFile.getName());
        return file;
    }

    public static final HashMap<String, File> getAppDataFileMap() {
        HashMap<String, File> map = new HashMap<String, File>();

        FileList files = null;
        try {
            files = getDriveService().files().list()
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name,size)")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("DEBUG: Files in drive:");
        for (File file : files.getFiles()) {
            //System.out.printf("%s \t (%s)\n", file.getName(), file.getId());
            map.put(file.getName(), file);
        }
        return  map;
    }

    public static final java.io.File downloadFile(File cloudFile){
        System.out.println("Downloading "+cloudFile.getName());
        java.io.File localFile = new java.io.File(DriveQuickstart.LOCAL_DRIVE_FOLDER, cloudFile.getName());
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(localFile);
            getDriveService().files().get(cloudFile.getId())
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Download completed for "+cloudFile.getName());
        return localFile;
    }

    public static final void updateFile(String fileId, java.io.File localFile){
        FileContent mediaContent = new FileContent("text/plane", localFile);
        try {
            getDriveService().files().update(fileId, new File(), mediaContent).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("UPDATE: LOCAL(path= "+localFile.getAbsolutePath()+") \t->\t DRIVE(id= "+fileId);
        return;
    }

    public static final void deleteFile(File cloudFile){
        System.out.println("Deleting "+cloudFile.getName());
        try {
            getDriveService().files().delete(cloudFile.getId()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Deleting from cloud completed for "+cloudFile.getName());
    }


}
