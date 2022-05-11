import java.util.ArrayList;
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

    public String getPath() {
        return path;
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

    public ImageData(String path, boolean type, int index) {
        this.path = path;
        this.type = type;
        keypoints = new MatOfKeyPoint();
        descriptor = new Mat();
    }
    
    public void detectKeypoints() {
        image = Imgcodecs.imread(path, Imgcodecs.IMREAD_GRAYSCALE);
        
        SIFT sift = SIFT.create();
        sift.detectAndCompute(image, new Mat(), keypoints, descriptor);
    }
}
