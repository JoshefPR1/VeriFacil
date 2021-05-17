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
    private int[] completeMonitorsB;
    private List<Monitor> supportedMonitorsC;
    private int[] completeMonitorsC;

    ECU(String id){
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

    public int[] getCompleteMonitorsB() {
        return completeMonitorsB;
    }

    public List<Monitor> getSupportedMonitorsC() {
        return supportedMonitorsC;
    }

    public int[] getCompleteMonitorsC() {
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
        return Arrays.stream(completeMonitorsB).anyMatch(x -> x==1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isUncompletedMonitorC(){
        return Arrays.stream(completeMonitorsC).anyMatch(x -> x==1);
    }
}
