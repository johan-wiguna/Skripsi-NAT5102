/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author johan
 */
public class LabelledKeypoint implements Comparable<LabelledKeypoint> {
    int label;
    int keypointIdx;
    double x;
    double y;

    public LabelledKeypoint(int label, int keypointIdx, double x, double y) {
        this.label = label;
        this.keypointIdx = keypointIdx;
        this.x = x;
        this.y = y;
    }

    public int getLabel() {
        return label;
    }

    public int getKeypointIdx() {
        return keypointIdx;
    }
    
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public int compareTo(LabelledKeypoint lk) {
        if (this.x < lk.x) {
            return -1;
        } else if (this.x > lk.x) {
            return 1;
        } else {
            if (this.y < lk.y) {
                return -1;
            }
        }
        
        return 0;
    }
}
