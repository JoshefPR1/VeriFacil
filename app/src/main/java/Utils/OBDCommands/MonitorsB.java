package Utils.OBDCommands;

public enum MonitorsB implements Monitor {
    misfire("0","Monitor de fallos de encendido",3,3),
    fuel("1","Monitor de combustible",2,2),
    comprehensive("2","Monitor de componentes integrales",1,1),
    compressionIgn("3","Monitor de encendido por compresión",0,1);

    private String id;
    private String name;
    private int bit;
    private int bitStatus;

    MonitorsB(String id, String name, int bit, int bitStatus){
        this.id = id;
        this.name = name;
        this.bit = bit;
        this.bitStatus = bitStatus;
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

    public int getBitStatus() {
        return bitStatus;
    }
}
