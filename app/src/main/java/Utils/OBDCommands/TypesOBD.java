package Utils.OBDCommands;

import java.sql.Types;

import Utils.ATCommands.Protocol;

public enum TypesOBD {

    OBDII("01","OBD II"),
    OBD("02","OBD"),
    OBD_OBDII("03","OBD y OBD II"),
    OBDI("04","OBD I"),
    NoOBD("05","No OBD"),
    EOBD("06", "Euro OBD"),
    EOBD_OBDII("07","Euro OBD y OBD II"),
    EOBD_OBD("08","Euro OBD y OBD"),
    EOBD_OBD_OBDII("09","Euro OBD, OBD y OBD II"),
    JOBD("0A","Japan OBD"),
    JOBD_OBDII("0B","Japan OBD y OBD II"),
    JOBD_EOBD("0C","Japan OBD y Euro OBD"),
    JOBD_EOBD_OBD("0D", "Japan OBD, Euro OBD y OBD II");

    private String id;
    private String name;

    TypesOBD(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static TypesOBD find(String id){
        for (TypesOBD p : TypesOBD.values()){
            if(p.getId().equals(id))
                return p;
        }
        return null;
    }

    @Override
    public String toString() {
        return "TypesOBD{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
