import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.opencv.core.*;

public class KMeans {
    int k;
    Centroid[] centroids;
    double bounds[];
    Random random;

    public KMeans(int k, double bounds[]) {
        this.k = k;
        this.centroids = new Centroid[k];
        this.bounds = bounds;
        this.random = new Random();
    }
    
    public void initializeCentroids() {
        for (int i = 0; i < k; i++) {
            double x = ThreadLocalRandom.current().nextDouble(bounds[2], bounds[3]);
            double y = ThreadLocalRandom.current().nextDouble(bounds[1], bounds[0]);
            
            centroids[i] = new Centroid(x, y);
//            System.out.printf("CentroidInit: %f, %f%n", centroids[i].getX(), centroids[i].getY());
        }
    }
    
    public int[] clusterKeypoints(ImageData trainImage) {
        int keypointCount = trainImage.keypoints.height();
        KeyPoint[] keypoints = trainImage.keypoints.toArray();
        initializeCentroids();
        
        int[] keypointCentroid = new int[keypointCount];
        boolean isFirst = true;
        
        int iteration = 0;
        while (iteration < 50) {
            isFirst = false;
            
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
            
//            if (iteration == 12) {
//                for (int i = 0; i < k; i++) {
//                    System.out.printf("====================CLUSTER-%d%n", i);
//                    System.out.printf("Centroid: %f, %f%n", centroids[i].getX(), centroids[i].getY());
//                    for (int j = 0; j < keypointCount; j++) {
//                        if (keypointCentroid[j] == i) {
//                            System.out.printf("%d, ", j);
//                        }
//                    }
//                    System.out.println("");
//                }
//            }
            
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
        System.out.printf("%n%n");
        
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
    
    public int[] assignKeypoint(int[] keypointCentroid, KeyPoint[] keypoints) {
        
        
        return keypointCentroid;
    }
    
    public void getDistortion(ImageData trainImage) {
        int keypointCount = trainImage.keypoints.height();
        KeyPoint[] keypoints = trainImage.keypoints.toArray();
        initializeCentroids();
        
        int[] keypointCentroid = new int[keypointCount];
        
        int c = 0;
        
        while (c < 50) {
            
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
            
            for (int i = 0; i < 9; i++) {
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
            
            c++;
        }
        
        for (int h = 0; h < 9; h++) {
            for (int i = 0; i < keypointCount; i++) {
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
        }
    }
}
