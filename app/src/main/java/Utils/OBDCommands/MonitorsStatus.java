package Utils.OBDCommands;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.ECU;

public class MonitorsStatus extends OBDCommand {
    private List<ECU> ecus;
    private int numResponse;
    private boolean isISO;

    public MonitorsStatus() {
        super("01", "01");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");
        String[] frames = resultNoSpace.split("4101");

        for (int i = 0; i<frames.length; i+=2){

            System.out.println(frames[i]);
            System.out.println(frames[i+1]);

            String header = frames[i];
            String frame = frames[i];
            ECU ecu =  ECU.findECU(isISO?headerISO(header):headerNoISO(header),ecus);

            String binaryString = BinaryCodec.toAsciiString(Hex.decodeHex(frame.toCharArray()));

            String statusMILBS = binaryString.substring(0,8);
            String monitorsBBS = binaryString.substring(8,16);
            String monitorsCBS = binaryString.substring(16);

            ecu.setMILOn(statusMILBS.charAt(0)==1);
            ecu.setNumDTC(Integer.parseInt(statusMILBS.substring(1),2));
            List<Monitor> monitors = new ArrayList();
            List<Integer> statusMonitors = new ArrayList();

            for (Monitor mon : MonitorsB.values()){
                if (monitorsBBS.charAt(mon.getBit()+4)==1){
                    monitors.add(mon);
                    statusMonitors.add(((int) monitorsBBS.charAt(mon.getBitStatus())) - 48);
                }
            }

            ecu.setSupportedMonitorsB(monitors);
            ecu.setCompleteMonitorsB(statusMonitors);

            monitors = new ArrayList();
            statusMonitors = new ArrayList();

            for (Monitor mon : MonitorsC.values()){
                if (monitorsCBS.charAt(mon.getBit()+4)==1){
                    monitors.add(mon);
                    statusMonitors.add(((int) monitorsCBS.charAt(mon.getBit())) - 48);
                }
            }

            ecu.setSupportedMonitorsC(monitors);
            ecu.setCompleteMonitorsC(statusMonitors);

        }
    }

    public List<ECU> getEcus() {
        return ecus;
    }

    public void setEcus(List<ECU> ecus) {
        this.ecus = ecus;
    }

    public void setISO(boolean ISO) {
        isISO = ISO;
    }

    public int getNumResponse() {
        return numResponse;
    }
}
