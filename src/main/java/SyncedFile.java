import com.google.api.services.drive.model.File;

/**
 * Created by esalman17 on 8.10.2018.
 */

public class SyncedFile {
    public java.io.File localFile;
    public File cloudFile;

    public SyncedFile(java.io.File localFile, File cloudFile) {
        this.localFile = localFile;
        this.cloudFile = cloudFile;
    }
}
