package Utils.OBDCommands;

import androidx.core.content.res.TypedArrayUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.TroubleCode;

public class ReadDTC extends OBDCommand {

    private boolean isISO;
    private List<TroubleCode> troubleCodes;

    public ReadDTC() {
        super("03");
        this.isISO = false;
        setCommName("Leer códigos de diagnóstico de falla");
        this.troubleCodes = new ArrayList<>();
    }

    @Override
    protected void interpretResult() throws ExecutionException {

        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("43");
        frames = ArrayUtils.removeAll(frames,0);

        for (String frame : frames){
            System.out.println(frame);
            if(isISO){
                frame = frame.substring(2);
            }
            String[] textCodes = Iterables.toArray(Splitter.fixedLength(4).split(frame),String.class);
            for (String textCode : textCodes){
                System.out.println(textCode);
                if (!"0000".equals(textCode))
                    this.troubleCodes.add(new TroubleCode(textCode));
            }
        }

    }

    public List<TroubleCode> getTroubleCodes(){
        return troubleCodes;
    }

    public void interpretResult(String res) throws ExecutionException {
        this.resultText = res;

        String resultNoSpace = this.resultText.replaceAll(" ","");

        String[] frames = resultNoSpace.split("43");
        frames = ArrayUtils.removeAll(frames,0);

        for (String frame : frames){
            System.out.println(frame);
            if(isISO){
                frame = frame.substring(2);
            }
            String[] textCodes = Iterables.toArray(Splitter.fixedLength(4).split(frame),String.class);
            for (String textCode : textCodes){
                System.out.println(textCode);
                if (!"0000".equals(textCode))
                    this.troubleCodes.add(new TroubleCode(textCode));
            }
        }

        //System.out.println(this.troubleCodes);
    }

    public boolean isISO() {
        return isISO;
    }

    public void setISO(boolean ISO) {
        isISO = ISO;
    }
}
