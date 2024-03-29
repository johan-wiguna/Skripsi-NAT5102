import java.util.*;
import org.opencv.core.*;

public class WLISValidator {
    ArrayList<ImageData>[] trainImages;
    ImageData testImage;
    ArrayList<ArrayList<MatOfDMatch>> matches;
    int keypointCount;
    int trainImageCount;
    double maxSimilarity;
    
    public WLISValidator(ArrayList<ImageData>[] trainImages, ImageData testImage, ArrayList<ArrayList<MatOfDMatch>> matches) {
        this.trainImages = trainImages;
        this.testImage = testImage;
        this.matches = matches;
        this.keypointCount = testImage.descriptor.height();
        this.trainImageCount = matches.size();
    }
    
    public double getMaxSimilarity() {
        return maxSimilarity;
    }
    
    public void validateImage() {
        // Assign weight for each test image keypoint
        double[] keypointsWeight = assignWeight();
        
        // Label every test image keypoint and its pair
        ArrayList<ArrayList<LabelledKeypoint>> labelledKeypoint = labelMatches();
        
        // Finding LIS with the most weight by rotating the train image
        maxSimilarity = -1;
        
        for (int i = 0; i < labelledKeypoint.size(); i++) {
            int[] lisInit = getLIS(labelledKeypoint.get(i));
            double maxWeight = getSimilarity(lisInit, keypointsWeight);
            int angle = 10;
            
            for (int j = 0; j < 360/angle; j++) {
                for (int k = 0; k < labelledKeypoint.get(i).size(); k++) {
                    double x = labelledKeypoint.get(i).get(k).getX();
                    double y = labelledKeypoint.get(i).get(k).getY();
                    
                    double newX = x * Math.cos(angle) - y * Math.sin(angle);
                    double newY = x * Math.sin(angle) + y * Math.cos(angle);
                    
                    labelledKeypoint.get(i).get(k).setX(newX);
                    labelledKeypoint.get(i).get(k).setY(newY);
                }
                
                Collections.sort(labelledKeypoint.get(i));
                int[] currLIS = getLIS(labelledKeypoint.get(i));
                double currWeight = getSimilarity(currLIS, keypointsWeight);
                
                if (currWeight > maxWeight) {
                    maxWeight = currWeight;
                }
            }
            
            if (maxWeight > maxSimilarity) {
                maxSimilarity = maxWeight;
            }
        }
    }
    
    private double[] assignWeight() {
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
            
            if (d2 == 0) {
                weights[i] = 0.0;
            } else {
//                System.out.println("d1 = " + d1);
//                System.out.println("d2 = " + d2);
                weights[i] = 1.0 - (d1 / d2);
//                System.out.println("weight = " + weights[i]);
            }
        }
        
        return weights;
    }
    
    private ArrayList<ArrayList<LabelledKeypoint>> labelMatches() {
        LabelledKeypoint[][] trainLabels = new LabelledKeypoint[trainImageCount][keypointCount];
        
        ImageData[] trainImagesMerged = new ImageData[trainImageCount];
        int idx = 0;
        for (int i = 0; i < trainImages.length; i++) {
            for (int j = 0; j < trainImages[i].size(); j++) {
                trainImagesMerged[idx] = trainImages[i].get(j);
                idx++;
            }
        }
        
        // matches.get(i) size = jumlah keypoint test
        // matches.get(i).get(j) size = byk pasangan per keypoint
        
        for (int i = 0; i < trainImageCount; i++) {
            boolean[] kpTaken = new boolean[trainImagesMerged[i].keypoint.toArray().length];
            
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
                
                KeyPoint[] kp = trainImagesMerged[i].keypoint.toArray();
                
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
        lstFiltered.forEach(list -> {
            list = new ArrayList<>();
        });
        
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
 
    private int[] getLIS(ArrayList<LabelledKeypoint> lk) {
        int n = lk.size();
        
        int[] arr = new int[n];
        int[] len = new int[n];
        int[] idx = new int[n];
        
        for (int i = 0; i < n; i++) {
            arr[i] = lk.get(i).label;
            len[i] = 1;
            idx[i] = -1;
        }
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (arr[i] > arr[j] && len[i] < len[j] + 1) {
                    len[i] = len[j] + 1;
                    idx[i] = j;
                }
            }
        }
        
        int maxIdx = 0;
        for (int i = 1; i < n; i++) {
            if (len[i] > len[maxIdx]) {
                maxIdx = i;
            }
        }
        
        int[] lis = new int[len[maxIdx]];
        int k = maxIdx;
        int currIdx = lis.length - 1;
        
        while (idx[k] != -1) {
            lis[currIdx] = arr[k];
            k = idx[k];
            currIdx--;
        }
        
        lis[0] = arr[k];
        
        return lis;
    }
    
    private double getSimilarity(int[] lis, double[] keypointsWeight) {
        double weight = 0.0;
        
        for (int i = 0; i < lis.length; i++) {
            weight += keypointsWeight[lis[i]];
        }
        
        return weight;
    }
}