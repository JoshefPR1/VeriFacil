package Utils.ATCommands;

import java.io.IOException;
import java.io.InputStream;

public class ATZ extends ATCommand{

    private String version;

    public ATZ() {
        super("Z");
        setCommName("Reiniciar ELM327");
    }

    @Override
    protected void interpretResult() {
        String resultNoSpace = this.resultText.replaceAll(" ","");
        this.isOK = resultNoSpace.contains("ELM327");
        this.version = resultNoSpace.substring(resultNoSpace.lastIndexOf('v'));
    }
    @Override
    protected void readResult(InputStream in) throws IOException {
        byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached
        char c;
        // -1 if the end of the stream is reached
        int i = 0;

        while (i < 2){
            while (((b = (byte) in.read()) > -1)) {
                c = (char) b;
                if (c == '>') // read until '>' arrives
                {
                    break;
                }
                res.append(c);
            }
            i++;
        }

        this.resultText = res.toString()
                .replaceAll("SEARCHING","")
                .replaceAll("\\s","")
                .replaceAll("(BUS INIT(:?))|(BUSINIT(:?))|(\\.\\.\\.)", "");
    }

    /*public void interpretResult(String res) {
        this.resultText = res;
        String resultNoSpace = this.resultText.replaceAll(" ","");
        this.isOK = resultNoSpace.matches("ELM327");
        this.version = resultNoSpace.substring(resultNoSpace.lastIndexOf('v'));
    }*/

    public String getELMVersion() {
        return version;
    }
}
