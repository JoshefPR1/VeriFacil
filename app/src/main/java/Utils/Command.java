package Utils;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public abstract class Command {
    protected Long waitTime = new Long(50);
    protected String commName;
    protected String comm;
    protected String resultText;

    protected boolean sendCommand(OutputStream out) throws IOException, InterruptedException {
        out.write((this.comm + "\r").getBytes());
        out.flush();
        if (waitTime != null && waitTime > 0) {
            Thread.sleep(waitTime);
        }
        return true;
    }

    protected void readResult(InputStream in) throws IOException {
        byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached
        char c;
        // -1 if the end of the stream is reached
        while (((b = (byte) in.read()) > -1)) {
            c = (char) b;
            if (c == '>') // read until '>' arrives
            {
                break;
            }
            res.append(c);
        }

        this.resultText = res.toString()
                .replaceAll("SEARCHING","")
                .replaceAll("\\s","")
                .replaceAll("(BUS INIT(:?))|(BUSINIT(:?))|(\\.\\.\\.)", "");
    }

    protected abstract void interpretResult() throws ExecutionException, DecoderException;

    protected void hasError() throws ExecutionException {
        boolean errorExists = false;
        String[] errors = {
                "\\?",
                "ACT ALERT",
                "\\!ACT ALERT",
                "BUFFER FULL",
                "BUS BUSSY",
                "BUS ERROR",
                "CAN ERROR",
                "DATA ERROR",
                "<DATA ERROR",
                "ERR\\d\\d",
                "FB ERROR",
                "LP ALERT",
                "\\!LP ALERT",
                "LV RESET",
                "NO DATA",
                "<RV ERROR",
                "STOPPED",
                "UNABLE TO CONNECT"
        };

        for (String error : errors){
            if(this.resultText.matches(".*"+error+".*"))
                throw new ExecutionException(
                        "Se encontró un error de tipo:" + error ,
                        new Throwable("Revisa el manual de usuario para saber un poco más del error.")
                );
        }

    }

    /*public static void hasError(String resultText) throws ExecutionException {
        boolean errorExists = false;
        String[] errors = {
                "\\?",
                "ACT ALERT",
                "\\!ACT ALERT",
                "BUFFER FULL",
                "BUS BUSSY",
                "BUS ERROR",
                "CAN ERROR",
                "DATA ERROR",
                "<DATA ERROR",
                "ERR\\d\\d",
                "FB ERROR",
                "LP ALERT",
                "\\!LP ALERT",
                "LV RESET",
                "NO DATA",
                "<RV ERROR",
                "STOPPED",
                "UNABLE TO CONNECT"
        };

        for (String error : errors){
            if(resultText.matches(".*"+error+".*")) {
                throw new ExecutionException("Se encontró un error de tipo:" + error, new Throwable("Revisa el manual de usuario para saber un poco más del error."));
            }
        }

    }*/

    public void run(OutputStream out, InputStream in) throws ExecutionException, IOException, InterruptedException, DecoderException {
        this.sendCommand(out);
        this.readResult(in);
        this.hasError();
        this.interpretResult();
    }

    public Long getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Long waitTime) {
        this.waitTime = waitTime;
    }

    public String getCommName() {
        return commName;
    }

    public void setCommName(String commName) {
        this.commName = commName;
    }
}
