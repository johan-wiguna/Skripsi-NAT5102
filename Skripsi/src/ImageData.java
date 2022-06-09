import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.features2d.*;

public class ImageData {
    String path;
    boolean type; // T = train; F = test
    int index;
    Mat image;
    Mat descriptor;
    MatOfKeyPoint keypoint;
    double[] bounds;
    double[][] segmentRange;
    
    public ImageData(String path, boolean type, int index) {
        this.path = path;
        this.type = type;
        this.index = index;
        keypoint = new MatOfKeyPoint();
        descriptor = new Mat();
    }

    public String getPath() {
        return path;
    }

    public boolean getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public Mat getDescriptor() {
        return descriptor;
    }

    public MatOfKeyPoint getKeypoint() {
        return keypoint;
    }
    
    public double[][] getSegmentRange() {
        return segmentRange;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public void detectKeypoints() {
        image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        
        SIFT sift = SIFT.create();
        sift.detectAndCompute(image, new Mat(), keypoint, descriptor);
    }
    
    public void detectKeypoints(int keypointAmount) {
        image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        
        SIFT sift = SIFT.create(keypointAmount);
        sift.detectAndCompute(image, new Mat(), keypoint, descriptor);
    }
    
    public void segmentImage() {
        KeyPoint[] kpArr = this.keypoint.toArray();
        bounds = new double[4];
        double topBound = -1;
        double bottomBound = Double.MAX_VALUE;
        double leftBound = Double.MAX_VALUE;
        double rightBound = -1;
        
        for (KeyPoint keypoint : kpArr) {
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
        
        bounds[0] = topBound;
        bounds[1] = bottomBound;
        bounds[2] = leftBound;
        bounds[3] = rightBound;
        
        double newWidth = rightBound - leftBound;
        double segmentWidth = newWidth / 4;
        
        segmentRange = new double[4][2];
        
        for (int i = 0; i < 4; i++) {
            segmentRange[i][0] = segmentWidth * i + leftBound;
            segmentRange[i][1] = segmentWidth * (i + 1) + leftBound;
        }
    }
}
