package Utils.OBDCommands;

public enum MonitorsC implements Monitor {
    catalyst("0","Monitor de convertidor catalítico",7),
    heatedCatalyst("1","Monitor de calor del convertidor catalítico",6),
    evaporativeSystem("2","Monitor del sistema evaporativo",5),
    secondaryAirSystem("3","Monitor del sistema de aire secundario",4),
    oxygenSensor("5","Monitor del sensor de oxígeno",2),
    heatedOxygenSensor("6","Monitor de calor del sensor de oxígeno",1),
    EGRVVT("7", "Monitor del sistema EGR/VVT",0);

    private String id;
    private String name;
    private int bit;

    MonitorsC(String id, String name, int bit){
        this.id = id;
        this.name = name;
        this.bit = bit;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBit() {
        return bit;
    }

    @Override
    public int getBitStatus() {
        return 0;
    }
}
