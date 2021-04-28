package Utils.ATCommands;

public class ATE_ extends ATCommand {
    public ATE_(boolean onOff) {
        super("E");
        this.comm = this.comm+(onOff?"1":"0");
    }
}
