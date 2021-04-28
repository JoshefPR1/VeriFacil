package Utils.ATCommands;

import Utils.Command;

public abstract class ATCommand extends Command {
    protected boolean isOK;

    public ATCommand(String comm){
        this.comm = "AT"+comm;
        isOK  = false;
    }

    protected void interpretResult(){
        String resultNoSpace = this.resultText.replaceAll(" ","");
        this.isOK = resultNoSpace.contains("OK");
    }

    public boolean isOK() {
        return isOK;
    }
}
