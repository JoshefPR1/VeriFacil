 package Utils;

import java.util.concurrent.ExecutionException;

public class TroubleCode {
    private String name;
    private boolean isSAECode = false;
    private String description;


    public TroubleCode(String textCode) throws ExecutionException {
        if(textCode.matches("[0-9A-F][0-9A-F] ?[0-9A-F][0-9A-F]")){
            this.name = interpretCode(textCode.replaceAll(" ",""));
            this.description = findDescription(this.name);
        }else
            throw new ExecutionException("Objeto TroubleCode no se creó correctamente: " + textCode ,new Throwable("El código no tiene el formato correcto"));
    }
    public static boolean readCode(String textCode){
        return textCode.matches("[0-9A-F][0-9A-F] ?[0-9A-F][0-9A-F]");
    }

    public String interpretCode(String textCode){
        String subCode = textCode.substring(1);
        switch (textCode.charAt(0)){
            case '0':
                isSAECode = true;
                return "P0"+subCode;
            case '1':
                return "P1"+subCode;
            case '2':
                isSAECode = true;
                return "P2"+subCode;
            case '3':
                isSAECode = true;
                return "P3"+subCode;
            case '4':
                isSAECode = true;
                return "C0"+subCode;
            case '5':
                return "C1"+subCode;
            case '6':
                return "C2"+subCode;
            case '7':
                return "C3"+subCode;
            case '8':
                isSAECode = true;
                return "B0"+subCode;
            case '9':
                return "B1"+subCode;
            case 'A':
                return "B2"+subCode;
            case 'B':
                return "B3"+subCode;
            case 'C':
                isSAECode = true;
                return "U0"+subCode;
            case 'D':
                return "U1"+subCode;
            case 'E':
                return "U2"+subCode;
            case 'F':
                return "U3"+subCode;
        }
        return "";
    }

    private String findDescription(String name){
        return "";
    }

    public String getDescription() {
        return description;
    }

    public boolean isSAECode() {
        return isSAECode;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TroubleCode{" +
                "name='" + name + '\'' +
                ", isSAECode=" + isSAECode +
                ", description='" + description + '\'' +
                '}';
    }
}
