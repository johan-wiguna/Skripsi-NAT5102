import java.util.*;
import org.opencv.core.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author johan
 */
public class HMMValidator extends Validator {
    ArrayList<ImageData> trainImages;
    ImageData testImage;
    ArrayList<ArrayList<MatOfDMatch>> matches;
    int testKeypointCount;
    int trainImageCount;
    int clusterCount = 2;
    
    public HMMValidator(ArrayList<ImageData> trainImages, ImageData testImage, ArrayList<ArrayList<MatOfDMatch>> matches) {
        this.trainImages = trainImages;
        this.testImage = testImage;
        this.matches = matches;
        this.testKeypointCount = testImage.descriptor.height();
        this.trainImageCount = trainImageCount = matches.size();
    }
    
    public void validateImage() {
        buildImageModel(trainImages.get(0));
    }
    
    public void buildImageModel(ImageData image) {
        double[] initial = {1, 0, 0, 0};
        double[][] transition = new double[4][4]; // transition[i][j] = P(i|j)
        double[][] emission = new double[clusterCount][4]; // emission[i][j] = P(i|j)
        
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
        
        // Make the random values sum up to 1
        double[] sumTransition = new double[transition.length];
        double[] sumEmission = new double[emission.length];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sumTransition[i] += transition[i][j];
            }
        }
        
        for (int i = 0; i < clusterCount; i++) {
            for (int j = 0; j < 4; j++) {
                sumEmission[i] += emission[i][j];
            }
        }
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                transition[i][j] /= sumTransition[i];
            }
        }
        
        for (int i = 0; i < clusterCount; i++) {
            for (int j = 0; j < 4; j++) {
                emission[i][j] /= sumEmission[i];
            }
        }
        
        System.out.printf("%nOLD EMISSION%n");
        for (double[] trs : emission) {
            for (double tr : trs) {
                System.out.printf("%f ", tr);
            }
            System.out.println("");
        }
        
        // Determine bounds for this image
        double[] imageBounds = segmentImage(image);
        // Cluster keypoints
        KMeans km = new KMeans(clusterCount, imageBounds);
        int[] keypointCluster = km.clusterKeypoints(image);
        // Re-estimate parameter with Baum-Welch algorithm
        trainImageModel(keypointCluster, initial, transition, emission, clusterCount);
    }
    
    public void trainImageModel(int[] obsSequence, double[] initial, double[][] transition, double[][] emission, int clusterCount) {
        
        // Alpha
        double[][] forward = new double[obsSequence.length][4];
        for (int j = 0; j < 4; j++) {
            forward[0][j] = initial[j] * emission[obsSequence[0]][j];
        }
        
        for (int t = 1; t < obsSequence.length; t++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    forward[t][j] += forward[t-1][i] * transition[j][i] * emission[obsSequence[t]][j];
                }
            }
        }
        
        for (double[] fwds : forward) {
            for (double fwd : fwds) {
                System.out.print(fwd + " ");
            }
            System.out.println("");
        }
        
        // Beta
        double[][] backward = new double[obsSequence.length][4];
        for (int t = obsSequence.length - 1; t >= 0; t--) {
            for (int i = 0; i < 4; i++) {
                if (t == obsSequence.length - 1) {
                    backward[t][i] = 1;
                } else {
                    for (int j = 0; j < 4; j++) {
                        backward[t][i] += backward[t+1][j] * transition[i][j] * emission[obsSequence[t+1]][i];
                    }
                }
            }
        }
        
        // Xi
        double[][][] xi = new double[obsSequence.length][4][4];
        for (int t = 0; t < obsSequence.length - 1; t++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    double numerator = forward[t][i] * transition[i][j] * emission[obsSequence[t+1]][j] * backward[t+1][j];
                    double enumerator = 0;
                    
                    for (int k = 0; k < 4; k++) {
                        enumerator += forward[t][j] * backward[t][j];
                    }
//                    System.out.printf("%f, %f%n", numerator, enumerator);
                    xi[t][i][j] = numerator / enumerator;
                }
            }
        }
        
        // Gamma
        double[][] gamma = new double[obsSequence.length][4];
        for (int t = 0; t < obsSequence.length; t++) {
            for (int j = 0; j < 4; j++) {
                double numerator = forward[t][j] * backward[t][j];
                double enumerator = 0;
                for (int i = 0; i < 4; i++) {
                    enumerator += forward[t][i] * backward[t][i];
                }
                
                gamma[t][j] = numerator / enumerator;
            }
        }
        
        // Re-estimate transition probabilities
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double numerator = 0;
                double enumerator = 0;
                for (int t = 0; t < obsSequence.length - 1; t++) {
                    numerator += xi[t][i][j];
                    for (int k = 0; k < 4; k++) {
                        enumerator += xi[t][i][k];
                    }
                }
                
                transition[i][j] = numerator / enumerator;
//                System.out.printf("%f, %f%n", numerator, enumerator);
            }
        }
        
        for (int j = 0; j < 4; j++) {
            for (int k = 0; k < clusterCount; k++) {
                double numerator = 0;
                double enumerator = 0;
                for (int t = 0; t < obsSequence.length; t++) {
                    if (obsSequence[t] == k) {
                        numerator += gamma[t][j];
                    }
                    
                    enumerator += gamma[t][j];
                }
                
                emission[k][j] = numerator / enumerator;
            }
        }
        
        System.out.printf("%nNEW EMISSION%n");
        for (double[] trs : emission) {
            for (double tr : trs) {
                System.out.printf("%f ", tr);
            }
            System.out.println("");
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
        
//        System.out.printf("%f, %f, %f, %f%n", topBound, bottomBound, leftBound, rightBound);
        
        double newWidth = rightBound - leftBound;
        double segmentWidth = newWidth / 4;
        
//        System.out.println("segmentWidth = " + segmentWidth);
        
        double[][] segmentRange = new double[4][2];
        
        for (int i = 0; i < 4; i++) {
            segmentRange[i][0] = segmentWidth * i + leftBound;
            segmentRange[i][1] = segmentWidth * (i + 1) + leftBound;
        }
        
        return imageBounds;
    }
}
