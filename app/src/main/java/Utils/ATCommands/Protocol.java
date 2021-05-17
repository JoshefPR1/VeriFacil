package Utils.ATCommands;

public enum Protocol {

    AUTO("0", "Automatic"),
    SAE_J1850_PWM("1", "SAE J1850 PWM (41.6 kbaud)"),
    SAE_J1850_VPW("2", "SAE J1850 VPW (10.4 kbaud)"),
    ISO_9141_2("3", "ISO 9141-2 (5 baud init, 10.4 kbaud)"),
    ISO_14230_4_KWP("4", "ISO 14230-4 KWP (5 baud init, 10.4kbaud)"),
    ISO_14230_4_KWP_F("5", "ISO 14230-4 KWP (fast init, 10.4kbaud)"),
    ISO_15735_4_11_500("6", "ISO 15765-4 CAN (11 bit ID, 500 kbaud)"),
    ISO_15735_4_29_500("7", "ISO 15765-4 CAN (29 bit ID, 500 kbaud)"),
    ISO_15735_4_11_250("8", "ISO 15765-4 CAN (11 bit ID, 250 kbaud)"),
    ISO_15735_4_29_250("9", "ISO 15765-4 CAN (29 bit ID, 250 kbaud)"),
    SAE_J1939("A","SAE J1939 CAN (29 bit ID, 250* kbaud)"),
    USER1("B", "USER1 CAN (11* bit ID, 125* kbaud)"),
    USER2("C", "USER2 CAN (11* bit ID, 50* kbaud)");

    private String id;
    private String description;

    Protocol(String id, String desc){
        this.id = id;
        this.description = desc;
    }

    public static Protocol find(String id){
        for (Protocol p : Protocol.values()){
            if(p.getId().equals(id))
                return p;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
