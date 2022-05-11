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

    public LabelledKeypoint(int label, int keypointIdx) {
        this.label = label;
        this.keypointIdx = keypointIdx;
    }

    public int getLabel() {
        return label;
    }

    public int getKeypointIdx() {
        return keypointIdx;
    }

    @Override
    public int compareTo(LabelledKeypoint lk) {
        return this.keypointIdx - lk.keypointIdx;
    }
}
