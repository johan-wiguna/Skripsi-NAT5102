import java.util.*;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;

public class Main implements Serializable {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Dataset1Processor dp1 = new Dataset1Processor();
        dp1.storeTrainImagePaths();
        
        ImageData testImage = new ImageData("C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Dataset_temp\\test\\067\\13_067.png", false, -1);
        testImage.detectKeypoints(100);
        
        ArrayList<ImageData>[] realTrainImages = convertPathsToImageData(dp1.getRealTrainPaths(), true);
//        ArrayList<ImageData>[] forgedTrainImages = convertPathsToMat(dp1.getForgedTrainPaths());
        
        ImageMatcher ip = new ImageMatcher();
//        ArrayList<ArrayList<MatOfDMatch>> matches = ip.matchKeypoints(testImage, realTrainImages, 20);
//        ArrayList<ArrayList<MatOfDMatch>> matches = ip.matchKeypoints(testImage, realTrainImages[67], 20);
        
        // WLIS
//        double totalScore = 0;
//        for (int i = 0; i < realTrainImages[67].size(); i++) {
//            ImageData currTest = realTrainImages[67].get(i);
//            ArrayList<ImageData> currTrain = new ArrayList<>();
//            for (int j = 0; j < realTrainImages[67].size(); j++) {
//                if (j != i)
//                    currTrain.add(realTrainImages[67].get(j));
//            }
//            
//            ArrayList<ArrayList<MatOfDMatch>> currMatches = ip.matchKeypoints(currTest, currTrain, 20);
//            
//            ArrayList<ImageData>[] arr = new ArrayList[1];
//            arr[0] = currTrain;
//            
//            WLISValidator wlis = new WLISValidator(arr, currTest, currMatches);
//            wlis.validateImage();
//            System.out.println("final = " + wlis.finalScore);
//            totalScore += wlis.finalScore;
//        }
//        
//        double avgScore = totalScore / realTrainImages[67].size();
//        
//        ArrayList<ImageData>[] arr = new ArrayList[1];
//        arr[0] = realTrainImages[67];
//        WLISValidator wlis = new WLISValidator(arr, testImage, matches);
//        wlis.validateImage();
//        System.out.println("");
//        System.out.println("finalScore = " + wlis.finalScore);
//        System.out.println("avgScore = " + avgScore);
//        
//        if (wlis.finalScore >= avgScore) { 
//            System.out.println("VALID!");
//        } else {
//            System.out.println("NOT VALID!");
//        }
        
        // HMM
        HMM[] trainHMM = new HMM[realTrainImages[67].size()];
        for (int i = 0; i < trainHMM.length; i++) {
            HMM hmm = new HMM(realTrainImages[67].get(i), 3);
            hmm.buildImageModel();
            trainHMM[i] = hmm;
        }
        
        ArrayList<MatOfDMatch> matches = ip.matchKeypoints(testImage, realTrainImages[67]);
        HMMValidator hmmValidator = new HMMValidator(realTrainImages[67], testImage, matches, trainHMM);
        hmmValidator.validateImage();
    }
    
    public static ArrayList<ImageData>[] convertPathsToImageData(ArrayList<String>[] paths, boolean imageType) {
        ArrayList<ImageData>[] res = new ArrayList[paths.length];
        for (int i = 0; i < paths.length; i++) {
            res[i] = new ArrayList<>();
        }
        
        for (int i = 0; i < paths.length; i++) {
            if (paths[i] != null) {
                for (int j = 0; j < paths[i].size(); j++) {
                    ImageData id = new ImageData(paths[i].get(j), imageType, i);
                    id.detectKeypoints(100);
                    res[i].add(id);
                }
            }
        }
        
        return res;
    }
}
