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
        double finalMaxWeight = Double.MIN_VALUE;
        int finalImageIdx = -1;
        
        for (int i = 0; i < labelledKeypoint.size(); i++) {
            int[] lisInit = getLIS(labelledKeypoint.get(i));
            double maxWeight = getTotalWeight(lisInit, keypointsWeight);
            int angle = 10;
            System.out.println("i = " + i);
            for (int j = 0; j < 360/angle; j++) {
                for (int k = 0; k < labelledKeypoint.get(i).size(); k++) {
                    double x = labelledKeypoint.get(i).get(k).x;
                    double y = labelledKeypoint.get(i).get(k).y;
                    double newX = x * Math.cos(angle) - y * Math.sin(angle);
                    double newY = x * Math.sin(angle) + y * Math.cos(angle);
                    
                    labelledKeypoint.get(i).get(k).setX(newX);
                    labelledKeypoint.get(i).get(k).setY(newY);
                    
//                    if (k == 1) {
//                        System.out.println("x = " + x + "; y = " + y);
//                        System.out.println("newX = " + labelledKeypoint.get(i).get(k).x + "; newY = " + labelledKeypoint.get(i).get(k).y);
//                    }
                }
                
                Collections.sort(labelledKeypoint.get(i));
                int[] currLIS = getLIS(labelledKeypoint.get(i));
                double currWeight = getTotalWeight(currLIS, keypointsWeight);
                
                if (currWeight > maxWeight) {
                    maxWeight = currWeight;
                }
                
                System.out.println("cw = " + currWeight);
            }
            
            if (maxWeight > finalMaxWeight) {
                finalMaxWeight = maxWeight;
                finalImageIdx = i;
            }
        }
        System.out.println("----------------");
        
        // Calculate similarity between test image and the best matched train image
        System.out.println("Most similar image: " + finalImageIdx);
        System.out.println("Weight: " + finalMaxWeight);
        System.out.println("testImg: " + testImage.path);
        int currIdx = 0;
        loop:
        for (ArrayList<ImageData> trainImage : trainImages) {
            for (ImageData imageData : trainImage) {
                if (currIdx == finalImageIdx) {
                    System.out.println("trainImg: " + imageData.path);
                    break loop;
                }
                
                currIdx++;
            }
        }
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
                
                if (currDist < d1 && currDist != 0.0) {
                    d1 = currDist;
                    d1ImageIdx = j;
                }
            }
            
            for (int j = 0; j < matches.size(); j++) {
                if (j != d1ImageIdx) {
                    DMatch[] dm = matches.get(j).get(i).toArray();
                    double currDist = dm[0].distance;
                    
                    if (currDist < d2 && currDist != 0.0) {
                        d2 = currDist;
                    }
                }
            }
//            System.out.println("d1 = " + d1 + "; d2 = " + d2);
            weights[i] = 1.0 - (d1 / d2);
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
 
    public int[] getLIS(ArrayList<LabelledKeypoint> lk) {
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
    
    public double getTotalWeight(int[] lis, double[] keypointsWeight) {
        double weight = 0.0;
        
        for (int i = 0; i < lis.length; i++) {
            weight += keypointsWeight[lis[i]];
        }
        
        return weight;
    }
    
    public void rotateImage(double x, double y, double angle) {
        
    }
}