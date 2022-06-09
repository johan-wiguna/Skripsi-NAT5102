import java.util.*;
import org.opencv.core.*;

public class HMM {
    ImageData image;
    int clusterCount;
    int[] keypointCluster;
    double[] initial = {1, 0, 0, 0};
    double[][] transition;
    double[][] emission;
    double[][] alpha;
    double[][] beta;
    double[][][] xi;
    double[][] gamma;
    double probability;
    
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
        
        // Determine bounds for this image
        image.segmentImage();
        double[] imageBounds = image.bounds;
        
        // Cluster keypoints
        KMeans km = new KMeans(clusterCount, imageBounds);
        keypointCluster = km.clusterKeypoints(image);
        
        // Re-estimate parameter with Baum-Welch algorithm
        reestimateParameters();
        
        // Calculate probability for the image
        calculateProbability();
    }
    
    private void calculateProbability() {
        image.segmentImage();
        double[][] segmentRange = image.getSegmentRange();
        KeyPoint[] kp = image.keypoint.toArray();
        int keypointCount = kp.length;
        LabelledKeypoint[] trainLK = new LabelledKeypoint[keypointCount];
        
        for (int i = 0; i < keypointCount; i++) {
            trainLK[i] = new LabelledKeypoint(i, kp[i].pt.x, kp[i].pt.y);
        }
        
        Arrays.sort(trainLK);
        
        int[] sortedState = new int[keypointCount];

        for (int j = 0; j < keypointCount; j++) {
            for (int k = 0; k < 4; k++) {
                if (trainLK[j].getX() >= segmentRange[k][0] && trainLK[j].getX() < segmentRange[k][1]) {
                    sortedState[trainLK[j].getKeypointIdx()] = k;
                }
            }
        }
        
        int lastIdx = -1;
        for (int j = 0; j < keypointCount; j++) {
            int sortedIdx = trainLK[j].getKeypointIdx();
            
            if (j == 0) {
                probability = initial[sortedState[sortedIdx]] * emission[keypointCluster[sortedIdx]][sortedState[sortedIdx]];
            } else {
                probability *= transition[sortedState[sortedIdx]][sortedState[lastIdx]] * emission[keypointCluster[sortedIdx]][sortedState[sortedIdx]];
            }
            
            lastIdx = sortedIdx;
        }
    }

    public double getProbability() {
        return probability;
    }
    
    private void reestimateParameters() {
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
    
    private void trainImageModel() {
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
    
    private void normalizeProbabilities(double[][] arr) {
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
}
