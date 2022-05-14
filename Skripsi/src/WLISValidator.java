import java.util.*;
import org.opencv.core.*;

public class WLISValidator extends Validator {
    double threshold = 0;
    ImageProcessor ip;
    ArrayList<ImageData>[] trainImages;
    ImageData testImage;
    ArrayList<ArrayList<MatOfDMatch>> matches;
    int keypointCount;
    int trainImageCount;
    
    public WLISValidator(ArrayList<ImageData>[] trainImages, ImageData testImage, ArrayList<ArrayList<MatOfDMatch>> matches) {
        this.ip = new ImageProcessor();
        this.trainImages = trainImages;
        this.testImage = testImage;
        this.matches = matches;
        this.keypointCount = testImage.descriptor.height();
        this.trainImageCount = trainImageCount = matches.size();
    }
    
    public void validateImage() {
        // Assign weight for each test image keypoint
        double[] keypointsWeight = assignWeight();
        
        // Label every test image keypoint and its pair
        ArrayList<ArrayList<LabelledKeypoint>> labelledKeypoint = labelMatches();
//        for (ArrayList<LabelledKeypoint> arrayList : labelledKeypoint) {
//            for (LabelledKeypoint labelledKeypoint1 : arrayList) {
//                System.out.print(labelledKeypoint1.label + ", ");
//            }
//            System.out.println("");
//        }
        
        // Finding LIS with the most weight by rotating the train image
        
        // Calculate similarity between test image and the best matched train image
    }
    
    public double[] assignWeight() {
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
    
    public ArrayList<ArrayList<LabelledKeypoint>> labelMatches() {
        LabelledKeypoint[][] trainLabels = new LabelledKeypoint[trainImageCount][keypointCount];
        
        ImageData[] trainImagesMerged = new ImageData[trainImageCount];
        int idx = 0;
        for (int i = 0; i < trainImages.length; i++) {
            for (int j = 0; j < trainImages[i].size(); j++) {
                trainImagesMerged[idx] = trainImages[i].get(j);
                idx++;
            }
        }
        
        // matches.get(i).size() size = jumlah keypoint test
        // matches.get(i).get(j) size = byk pasangan per keypoint
        
        for (int i = 0; i < trainImageCount; i++) {
            boolean[] kpTaken = new boolean[trainImagesMerged[i].keypoints.toArray().length];
            
            for (int j = 0; j < matches.get(i).size(); j++) {
                int trainKeypointIdx = -1;
                DMatch[] dm = matches.get(i).get(j).toArray();
                
                for (int k = 0; k < matches.get(i).get(j).height(); k++) {
                    if (!kpTaken[dm[k].trainIdx]) {
                        kpTaken[dm[k].trainIdx] = true;
                        trainKeypointIdx = dm[k].trainIdx;
                        break;
                    }
                }
                
                KeyPoint[] kp = trainImagesMerged[i].keypoints.toArray();
                
                if (trainKeypointIdx != -1) {
                    double x = kp[trainKeypointIdx].pt.x;
                    double y = kp[trainKeypointIdx].pt.y;
                    LabelledKeypoint lk = new LabelledKeypoint(j, trainKeypointIdx, x, y);
                    trainLabels[i][j] = lk;
                } else {
                    LabelledKeypoint lk = new LabelledKeypoint(j, trainKeypointIdx, 0, 0);
                    trainLabels[i][j] = lk;
                }
            }
        }
        
        for (LabelledKeypoint[] trainLabel : trainLabels) {
            Arrays.sort(trainLabel);
        }
        
        // Get rid of keypoints that doesn't have any pair
        ArrayList<ArrayList<LabelledKeypoint>> lstFiltered = new ArrayList<>();
        for (ArrayList<LabelledKeypoint> list : lstFiltered) {
            list = new ArrayList<>();
        }
        
        for (int i = 0; i < trainLabels.length; i++) {
            ArrayList<LabelledKeypoint> temp = new ArrayList<>();
            for (int j = 0; j < trainLabels[i].length; j++) {
                if (trainLabels[i][j].keypointIdx != -1) {
                    temp.add(trainLabels[i][j]);
                }
            }
            lstFiltered.add(temp);
        }
        
        return lstFiltered;
    }
 
    public Stack getLongestIncreasingSubsequence(ArrayList<LabelledKeypoint> lk) {
        int n = lk.size();
        
        int[] labels = new int[n];
        int[] len = new int[n];
        int[] idx = new int[n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i-1; j++) {
                if (labels[i] > labels[j] && len[i] < len[j] + 1) {
                    len[i] = len[j] + 1;
                    idx[i] = j;
                }
            }
        }
        
        int maxLen = 0;
        for (int i = 1; i < n; i++) {
            if (len[i] > len[maxLen]) {
                maxLen = i;
            }
        }
        
        Stack lis = new Stack();
        int k = maxLen;
        while (idx[k] != 1) {
            lis.push(labels[idx[k]]);
            k = idx[k];
        }
        
        return lis;
    }
    
    public void rotateImage(double x, double y, double angle) {
        
    }
}