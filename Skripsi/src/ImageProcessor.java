import java.util.*;
import org.opencv.core.*;
import org.opencv.features2d.*;

public class ImageProcessor {
    MatOfKeyPoint[] segmentedKeypoint;
    
    public List<MatOfDMatch> matchKeypoints(ImageData testData, ImageData trainImage, int k) {
        List<MatOfDMatch> lstMatches = new ArrayList<>();
        BFMatcher bf = new BFMatcher();
        
        bf.knnMatch(testData.descriptor, trainImage.descriptor, lstMatches, k);
        
        return lstMatches;
    }
    
    public List<List<MatOfDMatch>> matchKeypoints(ImageData testImage, ImageData[] trainImages, int k) {
        List<List<MatOfDMatch>> lst = new ArrayList<>();
        List<MatOfDMatch> lstMatches = new ArrayList<>();
        BFMatcher bf = new BFMatcher();
        
        System.out.println("len = " + trainImages.length);
        
        for (ImageData trainImage : trainImages) {
            bf.knnMatch(testImage.descriptor, trainImage.descriptor, lstMatches, k);
            lst.add(lstMatches);
        }
        
        return lst;
    }
    
    public ArrayList<ArrayList<MatOfDMatch>> matchKeypoints(ImageData testImage, ArrayList<ImageData>[] trainImages, int k) {
        ArrayList<ArrayList<MatOfDMatch>> matches = new ArrayList<>();
        for (ArrayList<MatOfDMatch> match : matches) {
            match = new ArrayList<>();
        }
        
        BFMatcher bf = new BFMatcher();
        
        for (ArrayList<ImageData> trainImageFolders : trainImages) {
            for (ImageData trainImage : trainImageFolders) {
                ArrayList<MatOfDMatch> temp = new ArrayList<>();
                bf.knnMatch(testImage.descriptor, trainImage.descriptor, temp, k);
                matches.add(temp);
            }
        }
        
        return matches;
    }
    
    public void matchAllTrainImages() {
        
    }
    
    public void segmentImage() {
        
    }
    
    
}
