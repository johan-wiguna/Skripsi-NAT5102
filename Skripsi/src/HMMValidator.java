import java.util.ArrayList;
import java.util.Arrays;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfDMatch;

public class HMMValidator {
    ArrayList<ImageData> trainImages;
    ImageData testImage;
    ArrayList<MatOfDMatch> matches;
    HMM[] trainHMM;
    int keypointCount;
    int trainImageCount;
    double maxProbability;
    double avgTrainProb;

    public HMMValidator(ArrayList<ImageData> trainImages, ImageData testImage, ArrayList<MatOfDMatch> matches, HMM[] trainHMM) {
        this.trainImages = trainImages;
        this.testImage = testImage;
        this.matches = matches;
        this.trainHMM = trainHMM;
        this.keypointCount = testImage.descriptor.height();
        this.trainImageCount = matches.size();
    }
    
    public double getMaxProbability() {
        return maxProbability;
    }

    public double getAvgTrainProb() {
        return avgTrainProb;
    }
    
    public void validateImage() {
        testImage.segmentImage();
        double[][] segmentRange = testImage.getSegmentRange();
        KeyPoint[] kp = testImage.keypoint.toArray();
        LabelledKeypoint[] testLK = new LabelledKeypoint[keypointCount];
        
        // Sort keypoints by its X value
        for (int i = 0; i < keypointCount; i++) {
            testLK[i] = new LabelledKeypoint(i, kp[i].pt.x, kp[i].pt.y);
        }
        
        Arrays.sort(testLK);
        
        maxProbability = -1;
        double totalTrainProb = 0;
        
        for (int i = 0; i < trainImageCount; i++) {
            double currProb = 0;
            DMatch[] dm = matches.get(i).toArray();
            HMM currHMM = trainHMM[i];
            int[] testCluster = new int[keypointCount];
            int[] testState = new int[keypointCount];
            
            for (int j = 0; j < keypointCount; j++) {
                testCluster[testLK[j].getKeypointIdx()] = currHMM.keypointCluster[dm[testLK[j].getKeypointIdx()].trainIdx];
                
                for (int k = 0; k < 4; k++) {
                    if (testLK[j].getX() >= segmentRange[k][0] && testLK[j].getX() < segmentRange[k][1]) {
                        testState[testLK[j].getKeypointIdx()] = k;
                    }
                }
            }
            
            int lastIdx = -1;
            for (int j = 0; j < keypointCount; j++) {
                int sortedIdx = testLK[j].getKeypointIdx();
                
                if (j == 0) {
                    currProb = currHMM.initial[testState[sortedIdx]] * currHMM.emission[testCluster[sortedIdx]][testState[sortedIdx]];
                } else {
                    currProb *= currHMM.transition[testState[sortedIdx]][testState[lastIdx]] * currHMM.emission[testCluster[sortedIdx]][testState[sortedIdx]];
                }
                
                lastIdx = sortedIdx;
            }
            
            if (currProb > maxProbability) {
                maxProbability = currProb;
            }
            
            totalTrainProb += currHMM.getProbability();
        }
        
        avgTrainProb = totalTrainProb / trainImageCount;
    }
}
