import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.opencv.core.*;

public class KMeans {
    int k;
    Centroid[] centroids;
    double[] bounds;
    

    public KMeans(int k, double bounds[]) {
        this.k = k;
        this.centroids = new Centroid[k];
        this.bounds = bounds;
    }
    
    public int[] clusterKeypoints(ImageData image) {
        int keypointCount = image.keypoint.height();
        KeyPoint[] keypoints = image.keypoint.toArray();
        initializeCentroids();
        
        int[] keypointCentroid = new int[keypointCount];
        
        int iteration = 0;
        while (iteration < 50) {
            for (int i = 0; i < keypointCentroid.length; i++) {
                double x = keypoints[i].pt.x;
                double y = keypoints[i].pt.y;

                double minDist = Double.MAX_VALUE;
                int minDistIdx = -1;

                for (int j = 0; j < k; j++) {
                    double currDist = Math.sqrt(
                            Math.pow((x - centroids[j].getX()), 2) + Math.pow((y - centroids[j].getY()), 2));
                    
                    if (currDist < minDist) {
                        minDist = currDist;
                        minDistIdx = j;
                    }
                }

                keypointCentroid[i] = minDistIdx;
            }
            
            int[] clusterSize = getClusterSize(keypointCentroid);
            
            // Handle empty clusters
            for (int i = 0; i < k; i++) {
                if (clusterSize[i] == 0) {
                    // Find the biggest cluster
                    int biggestClusterSize = -1;
                    int biggestCluster = -1;

                    for (int j = 0; j < k; j++) {
                        if (clusterSize[j] > biggestClusterSize) {
                            biggestClusterSize = clusterSize[j];
                            biggestCluster = j;
                        }
                    }

                    // Find the farthest point in the biggest cluster
                    double farthestPoint = -1;
                    int farthestPointIdx = -1;
                    for (int j = 0; j < keypointCentroid.length; j++) {
                        if (keypointCentroid[j] == biggestCluster) {
                            double x = keypoints[j].pt.x;
                            double y = keypoints[j].pt.y;
                            double currDist = Math.sqrt(Math.pow((x - centroids[biggestCluster].getX()), 2) + Math.pow((y - centroids[biggestCluster].getY()), 2));

                            if (currDist > farthestPoint) {
                                farthestPoint = currDist;
                                farthestPointIdx = j;
                            }
                        }
                    }

                    // Assign the fathest point into the empty cluster
                    keypointCentroid[farthestPointIdx] = i;
                    clusterSize = getClusterSize(keypointCentroid);
                }
            }
            
            for (int i = 0; i < k; i++) {
                double sumX = 0;
                double sumY = 0;
                int totalKeypoint = 0;

                for (int j = 0; j < keypointCount; j++) {
                    if (keypointCentroid[j] == i) {
                        sumX += keypoints[j].pt.x;
                        sumY += keypoints[j].pt.y;
                        totalKeypoint++;
                    }
                }
                
                centroids[i].setX(sumX / totalKeypoint);
                centroids[i].setY(sumY / totalKeypoint);
            }
            
            iteration++;
        }
        
//        System.out.printf("%n%n");
//        
//        for (int i = 0; i < k; i++) {
//            System.out.printf("====================CLUSTER-%d%n", i);
//            System.out.printf("Centroid: %f, %f%n", centroids[i].getX(), centroids[i].getY());
//            for (int j = 0; j < keypointCount; j++) {
//                if (keypointCentroid[j] == i) {
//                    System.out.printf("%d, ", j);
//                }
//            }
//            System.out.println("");
//        }
        
        return keypointCentroid;
    }
    
    private void initializeCentroids() {
        for (int i = 0; i < k; i++) {
            double x = ThreadLocalRandom.current().nextDouble(bounds[2], bounds[3]);
            double y = ThreadLocalRandom.current().nextDouble(bounds[1], bounds[0]);
            
            centroids[i] = new Centroid(x, y);
        }
    }
    
    public int[] getClusterSize(int[] keypointCentroid) {
        int[] clusterSize = new int[k];
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < keypointCentroid.length; j++) {
                if (keypointCentroid[j] == i) {
                    clusterSize[i]++;
                }
            }
        }
        
        return clusterSize;
    }
}
