import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.features2d.*;

public class ImageData {
    String path;
    boolean type; // T = train; F = test
    int index;
    Mat image;
    Mat descriptor;
    MatOfKeyPoint keypoints;
    double[] bounds;
    double[][] segmentRange;
    
    public ImageData(String path, boolean type, int index) {
        this.path = path;
        this.type = type;
        this.index = index;
        keypoints = new MatOfKeyPoint();
        descriptor = new Mat();
    }

    public String getPath() {
        return path;
    }
    
    public String getClassPath() {
        String classPath = "";
        
        for (int i = path.length() - 1; i >= 0; i--) {
            if (path.charAt(i) == '\\') {
                classPath = path.substring(0, i);
                break;
            }
        }
        
        return classPath;
    }

    public boolean isType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public Mat getImage() {
        return image;
    }

    public Mat getDescriptor() {
        return descriptor;
    }

    public MatOfKeyPoint getKeypoints() {
        return keypoints;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public void detectKeypoints() {
        image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        
        SIFT sift = SIFT.create();
        sift.detectAndCompute(image, new Mat(), keypoints, descriptor);
    }
    
    public void detectKeypoints(int keypointAmount) {
        image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        
        SIFT sift = SIFT.create(keypointAmount);
        sift.detectAndCompute(image, new Mat(), keypoints, descriptor);
    }
    
    public void segmentImage() {
        KeyPoint[] keypoints = this.keypoints.toArray();
        bounds = new double[4];
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
