import com.google.api.services.drive.model.File;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by esalman17 on 9.10.2018.
 */

public class MasterCloudSynchronizer implements Runnable {
    @Override
    public void run() {
        HashMap<String, File> cloudFiles = GoogleDriveUtils.getAppDataFileMap();
        java.io.File[] localFiles = Master.LOCAL_DRIVE_FOLDER.listFiles();

        LinkedList<java.io.File> needUpload = new LinkedList<>();
        LinkedList<File> needDelete = new LinkedList<>();
        LinkedList<java.io.File> needUpdate = new LinkedList<>();
        Master.syncedFiles.clear();

        for (java.io.File localFile : localFiles) {
            // The file is only in LOCAL folder, send it to cloud
            if (!cloudFiles.containsKey(localFile.getName())) {
                needUpload.add(localFile);
            } else {
                // The file in the local is updated since last sync
                if (localFile.lastModified() > Master.lastSync) {
                    needUpdate.add(localFile);
                    Master.syncedFiles.put(localFile.getName(), null);
                } else {
                    File cloudFile = cloudFiles.get(localFile.getName());
                    SyncedFile sf = new SyncedFile(localFile, cloudFile);
                    Master.syncedFiles.put(localFile.getName(), sf);
                }

            }
        }

        for (String cloudFile : cloudFiles.keySet()) {
            // The file is only in CLOUD folder, delete from local
            if (!Master.syncedFiles.containsKey(cloudFile)) {
                needDelete.add(cloudFiles.get(cloudFile));
            }
        }

        if (needDelete.size() == 0 && needUpload.size() == 0 && needUpdate.size() == 0) {
            System.out.println("Current time: " + LocalDateTime.now() + ",\tno update is needed. Already synced!");
        } else {
            // Print planning updates
            System.out.println("Current time: " + LocalDateTime.now() + ",\tthe following files are going to be synchronized");
            for (java.io.File f : needUpload) {
                System.out.println(f.getName() + " \tgoing to be uploaded to cloud\t Size= " + f.length() + " bytes");
            }
            for (File f : needDelete) {
                System.out.println(f.getName() + " \tgoing to be deleted from cloud\t Size= " + f.getSize() + " bytes");
            }
            for (java.io.File f : needUpdate) {
                System.out.println(f.getName() + " \tgoing to be updated in cloud\t Size= " + f.length() + " bytes");
            }

            // Apply planning updates
            for (java.io.File localFile : needUpload) {
                File cloudFile = GoogleDriveUtils.createFileInAppData(localFile);
                SyncedFile sf = new SyncedFile(localFile, cloudFile);
                Master.syncedFiles.put(localFile.getName(), sf);
            }
            for (File cloudFile : needDelete) {
                GoogleDriveUtils.deleteFile(cloudFile);
            }
            for (java.io.File localFile : needUpdate) {
                File cloudFile = cloudFiles.get(localFile.getName());
                GoogleDriveUtils.updateFile(cloudFile.getId(), localFile);
                SyncedFile sf = new SyncedFile(localFile, cloudFile);
                Master.syncedFiles.put(localFile.getName(), sf);

            }
            System.out.println("Current time: " + LocalDateTime.now() + ",\tSynchronization done with Google Drive");
        }
        Master.lastSync = System.currentTimeMillis();
    }
}
