import java.io.File;
import java.util.ArrayList;

public class FolderProcessor {
    File folderPath;
    ArrayList<String> strFilePaths;

    public FolderProcessor(String basePath) {
        this.folderPath = new File(basePath);
        this.strFilePaths = new ArrayList<>();
        storeImagesPath();
    }

    public ArrayList<String> getStrFilePaths() {
        return strFilePaths;
    }
    
    public void storeImagesPath() {
        File[] filePaths = folderPath.listFiles();
        
        for (int i = 0; i < filePaths.length; i++) {
            String fileType = getFileType(filePaths[i].toString());
            if (fileType.equalsIgnoreCase("png") || fileType.equalsIgnoreCase("jpg") || fileType.equalsIgnoreCase("jpeg")) {
                strFilePaths.add(filePaths[i].toString());
            }
        }
    }
    
    public static String getFileType(String path) {
        String fileType = "";
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx != -1) {
            fileType = path.substring(dotIdx + 1);
        }
        
        return fileType;
    }
}
