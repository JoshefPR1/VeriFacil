package Utils.OBDCommands;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.ECU;

public class TypeOBD extends OBDCommand {
    private TypesOBD typeOBD;

    public TypeOBD(){
        super("01", "1C");
    }

    @Override
    protected void interpretResult() throws ExecutionException, DecoderException {
        String resultNoSpace = this.resultText.replaceAll(" ","");
        String[] frames = resultNoSpace.split("411C");
        frames = ArrayUtils.removeAll(frames,0);
        String idOBD = frames[0].substring(0,2);
        this.typeOBD = TypesOBD.find(idOBD);
    }

    public TypesOBD getTypeOBD() {
        return typeOBD;
    }
}
