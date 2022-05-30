import java.io.File;
import java.util.ArrayList;

public class FolderProcessor {
    File basePath;
    File[] folderPaths;
    ArrayList<String>[] filePaths;

    public FolderProcessor(String basePath) {
        this.basePath = new File(basePath);
        storeImagesPath();
    }
    
    public File[] getFolderPaths() {
        return folderPaths;
    }

    public ArrayList<String>[] getFilePaths() {
        return filePaths;
    }
    
    public void storeImagesPath() {
        folderPaths = basePath.listFiles();
        
        filePaths = new ArrayList[folderPaths.length];
        for (int i = 0; i < folderPaths.length; i++) {
            filePaths[i] = new ArrayList<>();
        }
        
        for (int i = 0; i < folderPaths.length; i++) {
            File[] files = folderPaths[i].listFiles();
            
            for (int j = 0; j < files.length; j++) {
                filePaths[i].add(files[j].toString());
            }
        }
    }
}
