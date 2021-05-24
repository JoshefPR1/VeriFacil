package Utils.OBDCommands;

import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import Utils.TroubleCode;

import org.apache.commons.codec.binary.*;
import org.apache.commons.lang3.Conversion;

public class CountDTC extends OBDCommand {

    private boolean isMILOn;
    private int[] numDTC;

    public CountDTC() {
        super("01", "01");
        isMILOn = false;
        numDTC = new int[]{};
    }

    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("4101");
        frames = ArrayUtils.removeAll(frames,0);

        for (String frame : frames){
//            System.out.println(frame);
            String binaryString = BinaryCodec.toAsciiString(Hex.decodeHex(frame.substring(0,2).toCharArray()));
            this.isMILOn = binaryString.charAt(0) == '1';
            binaryString = binaryString.replaceFirst("1","0");
            int num = Integer.parseInt(binaryString,2);
//            System.out.println(num);
            Arrays.fill(this.numDTC,num);
        }
    }

    public void interpretResult(String res) throws ExecutionException, DecoderException {
        this.resultText = res;

        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("4101");
        frames = ArrayUtils.removeAll(frames,0);

        for (String frame : frames){
//            System.out.println(frame);
            String binaryString = BinaryCodec.toAsciiString(Hex.decodeHex(frame.substring(0,2).toCharArray()));
            this.isMILOn = binaryString.charAt(0) == '1';
            binaryString = binaryString.replaceFirst("1","0");
            int num = Hex.decodeHex( Hex.encodeHex( BinaryCodec.fromAscii( binaryString.toCharArray() ) ) )[0];
//            System.out.println(num);
            Arrays.fill(this.numDTC,num);
        }
    }

    public boolean isMILOn() {
        return isMILOn;
    }

    public int[] getNumDTC() {
        return numDTC;
    }
}
