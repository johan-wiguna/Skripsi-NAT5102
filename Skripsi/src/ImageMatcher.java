import java.util.*;
import org.opencv.core.*;
import org.opencv.features2d.*;

public class ImageMatcher {
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
    
    public ArrayList<ArrayList<MatOfDMatch>> matchKeypoints(ImageData testImage, ArrayList<ImageData> trainImages, int k) {
        ArrayList<ArrayList<MatOfDMatch>> matches = new ArrayList<>();
        for (ArrayList<MatOfDMatch> match : matches) {
            match = new ArrayList<>();
        }
        
        BFMatcher bf = new BFMatcher();
        
        for (ImageData trainImage : trainImages) {
            ArrayList<MatOfDMatch> temp = new ArrayList<>();
            bf.knnMatch(testImage.descriptor, trainImage.descriptor, temp, k);
            matches.add(temp);
        }
        
        return matches;
    }
    
    public ArrayList<MatOfDMatch> matchKeypoints(ImageData testImage, ArrayList<ImageData>[] trainImages) {
        ArrayList<MatOfDMatch> matches = new ArrayList<>();
        
        BFMatcher bf = new BFMatcher();
        
        for (ArrayList<ImageData> trainImageFolders : trainImages) {
            for (ImageData trainImage : trainImageFolders) {
                MatOfDMatch temp = new MatOfDMatch();
                bf.match(testImage.descriptor, trainImage.descriptor, temp);
                matches.add(temp);
            }
        }
        
        return matches;
    }
    
    public ArrayList<MatOfDMatch> matchKeypoints(ImageData testImage, ArrayList<ImageData> trainImages) {
        ArrayList<MatOfDMatch> matches = new ArrayList<>();
        
        BFMatcher bf = new BFMatcher();
        
        for (ImageData trainImage : trainImages) {
            MatOfDMatch temp = new MatOfDMatch();
            bf.match(testImage.descriptor, trainImage.descriptor, temp);
            matches.add(temp);
        }
        
        return matches;
    }
}
