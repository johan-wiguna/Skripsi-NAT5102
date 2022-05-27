import java.util.*;
import org.opencv.core.*;

public class HMM {
    ImageData image;
    ArrayList<ArrayList<MatOfDMatch>> matches;
    int clusterCount;
    int[] keypointCluster;
    double[] initial = {1, 0, 0, 0};
    double[][] transition;
    double[][] emission;
    double[][] alpha;
    double[][] beta;
    double[][][] xi;
    double[][] gamma;
    
    public HMM(ImageData image, int clusterCount) {
        this.image = image;
        this.clusterCount = clusterCount;
    }
    
    public void buildImageModel() {
        transition = new double[4][4]; // transition[i][j] = P(i|j) = j -> i
        emission = new double[clusterCount][4]; // emission[i][j] = P(i|j)
        
        // Initialize A and B randomly
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                transition[i][j] = random.nextDouble();
            }
        }
        
        for (int i = 0; i < clusterCount; i++) {
            for (int j = 0; j < 4; j++) {
                emission[i][j] = random.nextDouble();
            }
        }
        
        // Make the random values sum up to 1 (normalization)
        normalizeProbabilities(transition);
        normalizeProbabilities(emission);
        
        System.out.printf("OLD%n");
        for (double[] trs : transition) {
            for (double tr : trs) {
                System.out.printf("%f ", tr);
            }
            System.out.println("");
        }
        System.out.println("");
        
        // Determine bounds for this image
        double[] imageBounds = segmentImage(image);
        
        // Cluster keypoints
        KMeans km = new KMeans(clusterCount, imageBounds);
        keypointCluster = km.clusterKeypoints(image);
        
        // Re-estimate parameter with Baum-Welch algorithm
        reestimateParameters();
    }
    
    public void reestimateParameters() {
        double prevProb = 0;
        
        double[][] tempTransition = transition;
        double[][] tempEmission = emission;
        double[][] tempAlpha = alpha;
        double[][] tempBeta = beta;
        double[][][] tempXi = xi;
        double[][] tempGamma = gamma;
        
        boolean isFirst = true;
        while (true) {
            trainImageModel();
            
            double currProb = 0;
            for (int t = 0; t < keypointCluster.length; t++) {
                for (int i = 0; i < 4; i++) {
                    currProb += alpha[t][i] * beta[t][i];
                }
            }
            
            if (!isFirst) {
                if (currProb < prevProb) {
                    break;
                }
            } else {
                isFirst = false;
            }
            
            tempTransition = transition;
            tempEmission = emission;
            tempAlpha = alpha;
            tempBeta = beta;
            tempXi = xi;
            tempGamma = gamma;
            prevProb = currProb;
        }
        
        transition = tempTransition;
        emission = tempEmission;
        alpha = tempAlpha;
        beta = tempBeta;
        xi = tempXi;
        gamma = tempGamma;
    }
    
    public void trainImageModel() {
        // Alpha (Forward)
        alpha = new double[keypointCluster.length][4];
        for (int j = 0; j < 4; j++) {
            alpha[0][j] = initial[j] * emission[keypointCluster[0]][j];
        }
        
        for (int t = 1; t < keypointCluster.length; t++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    alpha[t][j] += alpha[t-1][i] * transition[j][i] * emission[keypointCluster[t]][j];
                }
            }
        }
        
        // Beta (Backward)
        beta = new double[keypointCluster.length][4];
        for (int i = 0; i < 4; i++) {
            beta[keypointCluster.length-1][i] = 1;
        }
        
        for (int t = keypointCluster.length - 2; t >= 0; t--) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    beta[t][i] += beta[t+1][j] * transition[j][i] * emission[keypointCluster[t+1]][j];
                }
            }
        }
        
        // Xi
        xi = new double[keypointCluster.length-1][4][4];
        for (int t = 0; t < keypointCluster.length - 1; t++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double numerator = alpha[t][i] * transition[j][i] * emission[keypointCluster[t+1]][j] * beta[t+1][j];
                    double enumerator = 0;
                    
                    for (int k = 0; k < 4; k++) {
                        for (int w = 0; w < 4; w++) {
                            enumerator += alpha[t][k] * beta[t+1][j] * transition[w][k] * emission[keypointCluster[t+1]][w];
                        }
                    }
                    
                    xi[t][i][j] = numerator / enumerator;
                }
            }
        }
        
        // Gamma
        gamma = new double[keypointCluster.length][4];
        for (int t = 0; t < keypointCluster.length; t++) {
            for (int j = 0; j < 4; j++) {
                double numerator = alpha[t][j] * beta[t][j];
                double enumerator = 0;
                
                for (int i = 0; i < 4; i++) {
                    enumerator += alpha[t][i] * beta[t][i];
                }
                
                gamma[t][j] = numerator / enumerator;
            }
        }
        
        // Re-estimate transition probabilities
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double numerator = 0;
                double enumerator = 0;
                
                for (int t = 0; t < keypointCluster.length - 1; t++) {
                    numerator += xi[t][i][j];
                    for (int k = 0; k < 4; k++) {
                        enumerator += xi[t][i][k];
                    }
                }
                
                transition[i][j] = numerator / enumerator;
            }
        }
        
        // Re-estimate emission probabilities
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < clusterCount; k++) {
                double numerator = 0;
                double enumerator = 0;
                
                for (int t = 0; t < keypointCluster.length; t++) {
                    if (keypointCluster[t] == k) {
                        numerator += gamma[t][j];
                    }
                    
                    enumerator += gamma[t][j];
                }
                
                emission[k][j] = numerator / enumerator;
            }
        }
        
        // Normalize new transition and emission probabilities
        normalizeProbabilities(transition);
        normalizeProbabilities(emission);
    }
    
    public void normalizeProbabilities(double[][] arr) {
        double[] sum = new double[arr.length];
        
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                sum[i] += arr[i][j];
            }
        }
        
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                arr[i][j] /= sum[i];
            }
        }
    }
    
    public double[] segmentImage(ImageData img) {
        KeyPoint[] keypoints = img.keypoints.toArray();
        double[] imageBounds = new double[4];
        double topBound = Double.MIN_VALUE;
        double bottomBound = Double.MAX_VALUE;
        double leftBound = Double.MAX_VALUE;
        double rightBound = Double.MIN_VALUE;
        
        for (KeyPoint keypoint : keypoints) {
            if (keypoint.pt.y > topBound) {
                topBound = keypoint.pt.y;
            }
            if (keypoint.pt.y < bottomBound) {
                bottomBound = keypoint.pt.y;
            }
            if (keypoint.pt.x < leftBound) {
                leftBound = keypoint.pt.x;
            }
            if (keypoint.pt.x > rightBound) {
                rightBound = keypoint.pt.x;
            }
        }
        
        imageBounds[0] = topBound;
        imageBounds[1] = bottomBound;
        imageBounds[2] = leftBound;
        imageBounds[3] = rightBound;
        
        double newWidth = rightBound - leftBound;
        double segmentWidth = newWidth / 4;
        
        double[][] segmentRange = new double[4][2];
        
        for (int i = 0; i < 4; i++) {
            segmentRange[i][0] = segmentWidth * i + leftBound;
            segmentRange[i][1] = segmentWidth * (i + 1) + leftBound;
        }
        
        return imageBounds;
    }
}
