package Utils.OBDCommands;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.ECU;
import Utils.TroubleCode;

public class SupportedPIDs extends OBDCommand{
    public SupportedPIDs() {
        super("01", "00");
        ecus = new ArrayList<>();
    }
    private boolean isISO = false;

    public List<ECU> getEcus() {
        return ecus;
    }

    private List<ECU> ecus;

    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");
        String[] frames = resultNoSpace.split("4100");

        for (int i = 0; i<frames.length; i+=2){

            System.out.println(frames[i]);
            System.out.println(frames[i+1]);

            String header = frames[i];
            String frame = frames[i];
            ECU nECU = new ECU(isISO?headerISO(header):headerNoISO(header));

            String binaryString = BinaryCodec.toAsciiString(Hex.decodeHex(frame.toCharArray()));

            nECU.setSupp01(binaryString.charAt(0) == '1');
            nECU.setSupp0C(binaryString.charAt(11) == '1');
            nECU.setSupp1C(binaryString.charAt(27) == '1');

            ecus.add(nECU);
        }
    }

    public boolean isISO() {
        return isISO;
    }

    public void setISO(boolean ISO) {
        isISO = ISO;
    }

}
