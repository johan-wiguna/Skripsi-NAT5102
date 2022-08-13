import org.opencv.core.*;

public class Validator {
    MatOfKeyPoint testKeypoints;
    MatOfKeyPoint[] trainKeypoints;
    MatOfKeyPoint[][] segmentedTestKeypoints;
    MatOfKeyPoint[][] segmentedTrainKeypoints;
    
    public boolean getImageValidity() {
        return false;
    }
}
