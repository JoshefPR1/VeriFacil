package Utils.OBDCommands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import Utils.Command;

public abstract class OBDCommand extends Command {
    protected String commMode;
    protected String commPid;

    OBDCommand(String commMode, String commPid){
        if (commMode.matches("\\d\\d") && commPid.matches("\\d\\d")){
            this.commMode = commMode;
            this.commPid = commPid;
            this.comm = commMode + commPid;
        }
    }

    OBDCommand(String commMode){
        if (commMode.matches("\\d\\d")){
            this.commMode = commMode;
            this.comm = commMode;
        }
    }

    protected String headerISO(String header){
        return header.substring(0,3);
    }

    protected String headerNoISO(String header){
        return header.substring(4);
    }

}
