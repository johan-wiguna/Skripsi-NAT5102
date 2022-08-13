import java.io.File;
import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.MatOfDMatch;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author johan
 */
public class Tester {
    public static int keypointCount = 250;
    public static boolean genuineTest = false;
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        int lastIdx = 0;
        for (int x = 0; x < 4; x++) {
            String trainPath = "";
            switch (x) {
                case 0:
                    trainPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\A";
                    break;
                case 1:
                    trainPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\B";
                    break;
                case 2:
                    trainPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\C";
                    break;
                case 3:
                    trainPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\D";
                    break;
                default:
                    break;
            }
            
            System.out.println("trainPath " + trainPath);
            
            FolderProcessor fpTrain = new FolderProcessor(trainPath);
            ArrayList<String> trainFilePaths = fpTrain.getStrFilePaths();
            ArrayList<ImageData> trainImages = convertPathsToImageData(trainFilePaths, true, keypointCount);
            
            String testPath = "";
            if (genuineTest) {
                testPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\Genuine";
            } else {
                testPath = "C:\\_My Files\\Johan\\GitHub\\Skripsi-NAT5102\\Dataset\\Experiment\\Forgery";
            }
            
            FolderProcessor fpTest = new FolderProcessor(testPath);
            ArrayList<String> testFilePaths = fpTest.getStrFilePaths();
            
            for (int y = 0; y < 5; y++) {
                ImageData testImage = new ImageData(testFilePaths.get(lastIdx), false, -1);
                System.out.println("testPath " + testFilePaths.get(lastIdx));
            
                ImageMatcher im = new ImageMatcher();
                testImage.detectKeypoints(keypointCount);

                // WLIS
//                long start1 = System.currentTimeMillis();
//                double totalScore = 0;
//                for (int i = 0; i < trainImages.size(); i++) {
//                    ImageData currTest = trainImages.get(i);
//                    ArrayList<ImageData> currTrain = new ArrayList<>();
//                    for (int j = 0; j < trainImages.size(); j++) {
//                        if (j != i)
//                            currTrain.add(trainImages.get(j));
//                    }
//
//                    ArrayList<ArrayList<MatOfDMatch>> currMatches = im.matchKeypoints(currTest, currTrain, 20);
//
//                    ArrayList<ImageData>[] arr = new ArrayList[1];
//                    arr[0] = currTrain;
//
//                    WLISValidator wlis = new WLISValidator(arr, currTest, currMatches);
//                    wlis.validateImage();
//                    totalScore += wlis.getMaxSimilarity();
//                }
//
//                double avgScore = totalScore / trainImages.size();
//
//                ArrayList<ImageData>[] trainImagesArr = new ArrayList[1];
//                trainImagesArr[0] = trainImages;
//                ArrayList<ArrayList<MatOfDMatch>> matchesWLIS = im.matchKeypoints(testImage, trainImages, 20);
//                WLISValidator wlis = new WLISValidator(trainImagesArr, testImage, matchesWLIS);
//                wlis.validateImage();
//                long end1 = System.currentTimeMillis();
//
//                System.out.print("TH: ");
//                if (wlis.getMaxSimilarity() >= avgScore * 1) {
//                    System.out.print("P ");
//                } else {
//                    System.out.print("N ");
//                }
//
//                if (wlis.getMaxSimilarity() >= avgScore * 0.9) {
//                    System.out.print("P ");
//                } else {
//                    System.out.print("N ");
//                }
//
//                if (wlis.getMaxSimilarity() >= avgScore * 0.8) {
//                    System.out.print("P ");
//                } else {
//                    System.out.print("N ");
//                }
//
//                if (wlis.getMaxSimilarity() >= avgScore * 0.7) {
//                    System.out.print("P ");
//                } else {
//                    System.out.print("N ");
//                }
//
//                if (wlis.getMaxSimilarity() >= avgScore * 0.6) {
//                    System.out.print("P ");
//                } else {
//                    System.out.print("N ");
//                }
                
                // HMM
                long start2 = System.currentTimeMillis();
                HMM[] trainHMM = new HMM[trainImages.size()];
                for (int i = 0; i < trainHMM.length; i++) {
                    HMM hmm = new HMM(trainImages.get(i), 3);
                    hmm.buildImageModel();
                    trainHMM[i] = hmm;
                }

                ArrayList<MatOfDMatch> matchesHMM = im.matchKeypoints(testImage, trainImages);
                HMMValidator hmmValidator = new HMMValidator(trainImages, testImage, matchesHMM, trainHMM);
                hmmValidator.validateImage();
                
//                double hmmThreshold = Math.pow(10, -40);
                System.out.print("HMM: ");
                if (hmmValidator.getMaxProbability() >= hmmValidator.getAvgTrainProb() * Math.pow(10, -30)) {
                    System.out.print("P ");
                } else {
                    System.out.print("N ");
                }
                
                if (hmmValidator.getMaxProbability() >= hmmValidator.getAvgTrainProb() * Math.pow(10, -40)) {
                    System.out.print("P ");
                } else {
                    System.out.print("N ");
                }
                if (hmmValidator.getMaxProbability() >= hmmValidator.getAvgTrainProb() * Math.pow(10, -50)) {
                    System.out.print("P ");
                } else {
                    System.out.print("N ");
                }
                if (hmmValidator.getMaxProbability() >= hmmValidator.getAvgTrainProb() * Math.pow(10, -55)) {
                    System.out.print("P ");
                } else {
                    System.out.print("N ");
                }
                if (hmmValidator.getMaxProbability() >= hmmValidator.getAvgTrainProb() * Math.pow(10, -60)) {
                    System.out.print("P");
                } else {
                    System.out.print("N");
                }
                System.out.println("");
                
                long end2 = System.currentTimeMillis();
//                System.out.println("WLIS: " + (end1 - start1));
//                System.out.println("HMM: " + (end2 - start2));
                
                lastIdx++;
            }
            System.out.println("");
        }
    }
    
    public static ArrayList<ImageData> convertPathsToImageData(ArrayList<String> paths, boolean imageType, int keypointAmount) {
        ArrayList<ImageData> res = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            ImageData id = new ImageData(paths.get(i), imageType, i);
            id.detectKeypoints(keypointAmount);
            res.add(id);
        }

        return res;
    }
    
    public static ArrayList<ImageData> convertPathsToImageData(ArrayList<String> paths, boolean imageType) {
        ArrayList<ImageData> res = new ArrayList<>();

        for (int i = 0; i < paths.size(); i++) {
            ImageData id = new ImageData(paths.get(i), imageType, i);
            id.detectKeypoints();
            res.add(id);
        }

        return res;
    }
}
