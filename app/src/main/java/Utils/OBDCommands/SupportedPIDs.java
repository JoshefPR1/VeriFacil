package Utils.OBDCommands;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.ExecutionException;

import Utils.TroubleCode;

public class SupportedPIDs extends OBDCommand{
    SupportedPIDs() {
        super("01", "00");
    }

    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("4100");
        frames = ArrayUtils.removeAll(frames,0);

        for (String frame : frames){
            System.out.println(frame);

            if(frame.length()<4)
                return;
            String[] textCodes = Iterables.toArray(Splitter.fixedLength(4).split(frame),String.class);
            for (String textCode : textCodes){
                System.out.println(textCode);
            }
        }
    }
}
