package Utils.ATCommands;

public class ATSP_ extends ATCommand {
    public ATSP_(String protocolNumber) {
        super("SP");
        this.comm = this.comm + protocolNumber;
    }
}
