package Utils.OBDCommands;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.ExecutionException;

public class EngineRPM extends OBDCommand{
    private float rpm;
    public EngineRPM() {
        super("01", "0C");
    }

    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("410C");
        frames = ArrayUtils.removeAll(frames,0);
        String hexRPM = frames[0].substring(0,4);
        this.rpm = (float) (Integer.parseInt(hexRPM,16) * 0.25);
    }

    public float getRpm() {
        return rpm;
    }
}
