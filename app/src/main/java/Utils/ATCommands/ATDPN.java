package Utils.ATCommands;

public class ATDPN extends ATCommand {
    private Protocol protocol;
    public ATDPN() {
        super("DPN");
    }

    @Override
    protected void interpretResult() {
        String resultNoSpace = this.resultText.replaceAll(" ","");
        String idProtocol = resultNoSpace.replaceAll("A","");
        this.protocol = Protocol.find(idProtocol);
    }

    public Protocol getProtocol() {
        return protocol;
    }
}
