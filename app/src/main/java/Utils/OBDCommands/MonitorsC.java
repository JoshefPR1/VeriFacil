package Utils.OBDCommands;

public enum MonitorsC implements Monitor {
    catalyst("0","Monitor de convertidor catalítico"),
    heatedCatalyst("1","Monitor de calor del convertidor catalítico"),
    evaporativeSystem("2","Monitor del sistema evaporativo"),
    secondaryAirSystem("3","Monitor del sistema de aire secundario"),
    oxygenSensor("5","Monitor del sensor de oxígeno"),
    heatedOxygenSensor("6","Monitor de calor del sensor de oxígeno"),
    EGRVVT("7", "Monitor del sistema EGR/VVT");

    private String id;
    private String name;

    MonitorsC(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
