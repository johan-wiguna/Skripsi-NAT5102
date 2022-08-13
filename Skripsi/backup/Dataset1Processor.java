import java.io.File;
import java.util.ArrayList;

public class Dataset1Processor {
    String basePath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Dataset_temp\\";
    File testPath = new File(basePath + "test\\");
    File trainPath = new File(basePath + "train\\");
    int datasetSize = 70;
    ArrayList<String>[] realTestPaths;
    ArrayList<String>[] forgedTestPaths;
    ArrayList<String>[] realTrainPaths;
    ArrayList<String>[] forgedTrainPaths;

    public ArrayList<String>[] getRealTestPaths() {
        return realTestPaths;
    }

    public ArrayList<String>[] getForgedTestPaths() {
        return forgedTestPaths;
    }

    public ArrayList<String>[] getRealTrainPaths() {
        return realTrainPaths;
    }

    public ArrayList<String>[] getForgedTrainPaths() {
        return forgedTrainPaths;
    }

    public int getDatasetSize() {
        return datasetSize;
    }
    
    public void storeTestImagePaths() {
        File[] testFolders = testPath.listFiles();
        
        realTestPaths = new ArrayList[datasetSize];
        forgedTestPaths = new ArrayList[datasetSize];
        
        for (int i = 0; i < datasetSize; i++) {
            realTestPaths[i] = new ArrayList<>();
            forgedTestPaths[i] = new ArrayList<>();
        }
        
        for (File testFolder : testFolders) {
            File[] testFiles = testFolder.listFiles();
            
            String fileIndex = testFolder.getName().substring(0, 3);
            int index = Integer.parseInt(fileIndex);

            for (File testFile : testFiles) {
                if (testFolder.toString().contains("forg")) {
                    forgedTestPaths[index].add(testFile.toString());
                } else {
                    realTestPaths[index].add(testFile.toString());
                }
            }
        }
    }
    
    public void storeTrainImagePaths() {
        File[] trainFolders = trainPath.listFiles();
        
        realTrainPaths = new ArrayList[datasetSize];
        forgedTrainPaths = new ArrayList[datasetSize];
        
        for (int i = 0; i < datasetSize; i++) {
            realTrainPaths[i] = new ArrayList<>();
            forgedTrainPaths[i] = new ArrayList<>();
        }
        
        for (File trainFolder : trainFolders) {
            File[] trainFiles = trainFolder.listFiles();
            
            String fileIndex = trainFolder.getName().substring(0, 3);
            int index = Integer.parseInt(fileIndex);

            for (File trainFile : trainFiles) {
                if (trainFolder.toString().contains("forg")) {
                    forgedTrainPaths[index].add(trainFile.toString());
                } else {
                    realTrainPaths[index].add(trainFile.toString());
                }
            }
        }
    }
}
