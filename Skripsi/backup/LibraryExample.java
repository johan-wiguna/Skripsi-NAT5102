import java.util.ArrayList;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.features2d.*;

public class LibraryExample {
    public static void main(String[] args) {
        String path1 = "C:\\_My Files\\1.png";
        Mat image1 = Imgcodecs.imread(path1, Imgcodecs.IMREAD_GRAYSCALE);
        MatOfKeyPoint keypoint1 = new MatOfKeyPoint();
        Mat descriptor1 = new Mat();
        
        String path2 = "C:\\_My Files\\2.png";
        Mat image2 = Imgcodecs.imread(path2, Imgcodecs.IMREAD_GRAYSCALE);
        MatOfKeyPoint keypoint2 = new MatOfKeyPoint();
        Mat descriptor2 = new Mat();
        
        SIFT sift = SIFT.create();
        sift.detectAndCompute(image1, new Mat(), keypoint1, descriptor1);
        sift.detectAndCompute(image2, new Mat(), keypoint2, descriptor2);
        
        KeyPoint[] kp1 = keypoint1.toArray();
        KeyPoint[] kp2 = keypoint2.toArray();
        
        ArrayList<MatOfDMatch> match = new ArrayList<>();
        BFMatcher bf = new BFMatcher();
        bf.knnMatch(descriptor1, descriptor2, match, 20);
    }
}
