import java.util.*;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import org.opencv.features2d.*;



public class Main implements Serializable {
//    public static void main(String[] args) {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//        
//        SIFT sift = SIFT.create();
//        // Limit jumlah keypoint 10
//        // SIFT sift = SIFT.create(10);
//        
//        String img1Path = "C:/_My Files/Johan/GitHub/Skripsi-NAT5102/rubik1.jpg";
//        String img2Path = "C:/_My Files/Johan/GitHub/Skripsi-NAT5102/rubik2.jpg";
//        
//        Mat img1 = Imgcodecs.imread(img1Path, Imgcodecs.IMREAD_GRAYSCALE);
//        Mat img2 = Imgcodecs.imread(img2Path, Imgcodecs.IMREAD_GRAYSCALE);
//        Mat imgRes = new Mat();
//        
//        MatOfKeyPoint kp1 = new MatOfKeyPoint();
//        Mat desc1 = new Mat();
//        MatOfKeyPoint kp2 = new MatOfKeyPoint();
//        Mat desc2 = new Mat();
//        
//        sift.detectAndCompute(img1, new Mat(), kp1, desc1);
//        sift.detectAndCompute(img2, new Mat(), kp2, desc2);
//        
//        // Keypoint matching
//        BFMatcher bf = new BFMatcher();
//        
//        // match(): 1 keypoint 1 pasangan
//        MatOfDMatch matches = new MatOfDMatch();
//        bf.match(desc1, desc2, matches);
//        
//        // knnMatch(): 1 keypoint bisa punya lebih dari 1 pasangan
//        List<MatOfDMatch> lstMatches = new ArrayList<>();
//        bf.knnMatch(desc1, desc2, lstMatches, 2);
//        
////        List<MatOfDMatch> goodMatches = new ArrayList<>();
////        
////        for (int i = 0; i < lstMatches.size(); i++) {
////            int j = i + 1;
////                if (lstMatches.get(i).distance > 0.75 * lstMatches.get(j)) {
////                    goodMatches.add(lstMatches.get(i));
////                }
////        }
//        
//        Features2d.drawMatches(img1, kp1, img2, kp2, matches, imgRes);
////        Features2d.drawMatchesKnn(img1, kp1, img2, kp2, lstMatches, imgRes);
//        
//        // Convert keypoints to array
//        KeyPoint[] kpArr = kp1.toArray();
//        
////        for (int i = 0; i < kpArr.length; i++) {
////            System.out.println(kpArr[i]);
////        }
//        
//        System.out.println(matches);
//        
//        System.out.println("LSTMATCHES");
//
//        for (MatOfDMatch match : lstMatches) {
//            System.out.println(match);
//        }
//        
//        imshow(imgRes);
//    }
    
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
//        new MainFrame();
        
        Dataset1Processor dp1 = new Dataset1Processor();
        dp1.storeTrainImagePaths();
        
        ImageData testImage = new ImageData("C:/_My Files/Johan/GitHub/Skripsi-NAT5102/Dataset/Dataset_temp/test/055/02_055.png", false, -1);
        testImage.detectKeypoints();
        
        ArrayList<ImageData>[] realTrainImages = convertPathsToMat(dp1.getRealTrainPaths(), true);
//        ArrayList<ImageData>[] forgedTrainImages = convertPathsToMat(dp1.getForgedTrainPaths());
        
        // Test print matrix
//        Mat desc = realTrainImages[67].get(0).descriptor;
        
//        for (int k = 0; k < desc.height(); k++) {
//            System.out.print("[");
//            for (int j = 0; j < desc.width(); j++) {
//                double[] res = desc.get(k, j);
//                System.out.print(res[0] + ", ");
//            }
//            System.out.print("]");
//            System.out.println("");
//        }
        
        ImageProcessor ip = new ImageProcessor();
        ArrayList<ArrayList<MatOfDMatch>> matches = ip.matchKeypoints(testImage, realTrainImages, 1);
        
        // WLIS
        WLISValidator wlis = new WLISValidator(realTrainImages, testImage, matches);
        wlis.calculateWeight();
        wlis.labelMatches();
        
        
//        Mat imgRes = new Mat();
//        Features2d.drawMatchesKnn(test1.image, test1.keypoints, train[0].image, train[0].keypoints, lstMatches.get(0), imgRes);
//        Features2d.drawMatchesKnn(test1.image, test1.keypoints, train[1].image, train[1].keypoints, lstMatches.get(1), imgRes);
//        imshow(imgRes);
    }
    
    public static ArrayList<ImageData>[] convertPathsToMat(ArrayList<String>[] paths, boolean imageType) {
        ArrayList<ImageData>[] res = new ArrayList[paths.length];
        for (int i = 0; i < paths.length; i++) {
            res[i] = new ArrayList<>();
        }
        
        for (int i = 0; i < paths.length; i++) {
            if (paths[i] != null) {
                for (int j = 0; j < paths[i].size(); j++) {
                    ImageData id = new ImageData(paths[i].get(j), imageType, i);
                    id.detectKeypoints();
                    res[i].add(id);
                }
            }
        }
        
        return res;
    }
    
    public static void imshow(Mat src){
        BufferedImage buffImage;
        
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", src, matOfByte); 
            byte[] byteArray = matOfByte.toArray();
            InputStream in = new ByteArrayInputStream(byteArray);
            buffImage = ImageIO.read(in);
            
            BufferedImage resized = resize(buffImage, buffImage.getWidth(), buffImage.getHeight());

            JFrame frame = new JFrame("Image");
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(resized)));
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }  
}
