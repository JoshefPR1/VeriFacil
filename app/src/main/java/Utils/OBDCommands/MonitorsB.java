package Utils.OBDCommands;

public enum MonitorsB implements Monitor {
    misfire("0","Monitor de fallos de encendido"),
    fuel("1","Monitor de combustible"),
    comprehensive("2","Monitor de componentes integrales"),
    compressionIgn("3","Monitor de encendido por compresión");

    private String id;
    private String name;

    MonitorsB(String id, String name){
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
