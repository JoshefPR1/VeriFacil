package Utils.ATCommands;

public class ATH_ extends ATCommand{

    public ATH_(boolean onOff) {
        super("H");
        this.comm = this.comm+(onOff?"1":"0");
    }
}
