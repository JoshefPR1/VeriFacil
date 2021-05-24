package Utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.List;

import Utils.OBDCommands.Monitor;

public class ECU {
    private String id;
    private boolean isMILOn;
    private int numDTC;

    private boolean supp01;
    private boolean supp0C;
    private boolean supp1C;

    private List<Monitor> supportedMonitorsB;
    private List<Integer> completeMonitorsB;
    private List<Monitor> supportedMonitorsC;
    private List<Integer> completeMonitorsC;

    public ECU(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isMILOn() {
        return isMILOn;
    }

    public int getNumDTC() {
        return numDTC;
    }

    public List<Monitor> getSupportedMonitorsB() {
        return supportedMonitorsB;
    }

    public List<Integer> getCompleteMonitorsB() {
        return completeMonitorsB;
    }

    public List<Monitor> getSupportedMonitorsC() {
        return supportedMonitorsC;
    }

    public List<Integer> getCompleteMonitorsC() {
        return completeMonitorsC;
    }

    public void setMILOn(boolean MILOn) {
        isMILOn = MILOn;
    }

    public void setNumDTC(int numDTC) {
        this.numDTC = numDTC;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isUncompletedMonitorB(){
        return completeMonitorsB.stream().anyMatch(x -> x==1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isUncompletedMonitorC(){
        return completeMonitorsC.stream().anyMatch(x -> x==1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ECU findECU(String id,List<ECU> ecus){
        return ecus.stream()
                .filter(cd -> id.equals(cd.getId()))
                .findAny()
                .orElse(null);
    }

    public boolean isSupp01() {
        return supp01;
    }

    public void setSupp01(boolean supp01) {
        this.supp01 = supp01;
    }

    public boolean isSupp0C() {
        return supp0C;
    }

    public void setSupp0C(boolean supp0C) {
        this.supp0C = supp0C;
    }

    public boolean isSupp1C() {
        return supp1C;
    }

    public void setSupp1C(boolean supp1C) {
        this.supp1C = supp1C;
    }

    public void setSupportedMonitorsB(List<Monitor> supportedMonitorsB) {
        this.supportedMonitorsB = supportedMonitorsB;
    }

    public void setCompleteMonitorsB(List<Integer> completeMonitorsB) {
        this.completeMonitorsB = completeMonitorsB;
    }

    public void setSupportedMonitorsC(List<Monitor> supportedMonitorsC) {
        this.supportedMonitorsC = supportedMonitorsC;
    }

    public void setCompleteMonitorsC(List<Integer> completeMonitorsC) {
        this.completeMonitorsC = completeMonitorsC;
    }

    @Override
    public String toString() {
        return "ECU{" +
                "id='" + id + '\'' +
                ", isMILOn=" + isMILOn +
                ", numDTC=" + numDTC +
                ", supp01=" + supp01 +
                ", supp0C=" + supp0C +
                ", supp1C=" + supp1C +
                ", supportedMonitorsB=" + supportedMonitorsB +
                ", completeMonitorsB=" + completeMonitorsB +
                ", supportedMonitorsC=" + supportedMonitorsC +
                ", completeMonitorsC=" + completeMonitorsC +
                '}';
    }
}
