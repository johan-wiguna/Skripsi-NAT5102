import java.util.*;
import org.opencv.core.DMatch;
import org.opencv.core.MatOfDMatch;

public class WLISValidator extends Validator {
    double threshold = 0;
    ImageProcessor ip;
    ArrayList<ImageData>[] trainImages;
    ImageData testImage;
    ArrayList<ArrayList<MatOfDMatch>> matches;
    
    public WLISValidator(ArrayList<ImageData>[] trainImages, ImageData testImage, ArrayList<ArrayList<MatOfDMatch>> matches) {
        this.ip = new ImageProcessor();
        this.trainImages = trainImages;
        this.testImage = testImage;
        this.matches = matches;
    }
    
    public double calculateKeypointDistance(double[] kp1, double[] kp2) {
        double dist = 0.0;
        for (int i = 0; i < 128; i++) {
            dist += Math.pow((kp1[i] - kp2[i]), 2);
        }
        
        return Math.sqrt(dist);
    }
    
    public double[] calculateWeight() {
        int keypointCount = testImage.descriptor.height();
        double[] weights = new double[keypointCount];
        
        for (int i = 0; i < keypointCount; i++) {
            double d1 = Double.MAX_VALUE;
            double d2 = Double.MAX_VALUE;
            int d1ImageIdx = -1;
            
            for (int j = 0; j < matches.size(); j++) {
                DMatch[] dm = matches.get(j).get(i).toArray();
                double currDist = dm[0].distance;
                
                if (currDist < d1) {
                    d1 = currDist;
                    d1ImageIdx = j;
                }
            }
            
            for (int j = 0; j < matches.size(); j++) {
                if (j != d1ImageIdx) {
                    DMatch[] dm = matches.get(j).get(i).toArray();
                    double currDist = dm[0].distance;
                    
                    if (currDist < d2) {
                        d2 = currDist;
                    }
                }
            }
            
            weights[i] = 1.0 - (d1/d2);
        }
        return weights;
    }
    
    public void labelMatches() {
        // Keypoints are sorted based on its coordinate (x, y) by default
        int keypointCount = testImage.descriptor.height();
        int trainImageCount = matches.size();
        LabelledKeypoint[][] trainLabels = new LabelledKeypoint[trainImageCount][keypointCount];
        
        for (int i = 0; i < trainImageCount; i++) {
            for (int j = 0; j < matches.get(i).size(); j++) {
                DMatch[] dm = matches.get(i).get(j).toArray();
                int trainKeypointIdx = dm[0].trainIdx;
                LabelledKeypoint lk = new LabelledKeypoint(j, trainKeypointIdx);
                
                trainLabels[i][j] = lk;
            }
        }
        
        for (int i = 0; i < trainLabels.length; i++) {
            Arrays.sort(trainLabels[i]);
        }
        
        for (int i = 0; i < trainLabels.length; i++) {
            for (int j = 0; j < trainLabels[i].length; j++) {
                System.out.print(trainLabels[i][j].keypointIdx + ",");
            }
            System.out.println();
        }
    }
    
    public void simulateRotation() {
        
    }
    
    public void getLongestIncreasingSubsequence() {
        
    }
}
