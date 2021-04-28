package Utils.ATCommands;

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
