package Utils;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.TT.verifacil.CarInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TroubleCode implements Parcelable {
    private String name;
    private boolean isSAECode = false;
    private String description;


    public TroubleCode(String textCode) throws ExecutionException {
        if(textCode.matches("[0-9A-F][0-9A-F] ?[0-9A-F][0-9A-F]")){
            this.name = interpretCode(textCode.replaceAll(" ",""));
            this.description = "";
        }else
            throw new ExecutionException("Objeto TroubleCode no se creó correctamente: " + textCode ,new Throwable("El código no tiene el formato correcto"));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public TroubleCode(Parcel in){
        this.name = in.readString();
        this.isSAECode = in.readBoolean();
        this.description = in.readString();
    }

    public static final Creator<TroubleCode> CREATOR = new Creator<TroubleCode>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public TroubleCode createFromParcel(Parcel in) {
            return new TroubleCode(in);
        }

        @Override
        public TroubleCode[] newArray(int size) {
            return new TroubleCode[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeBoolean(this.isSAECode);
        dest.writeString(this.description);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getDescriptions(List<TroubleCode> codes, CarInfo.AdapterTroubleCode adapter) throws IOException {
//        List<String> nCodes = new ArrayList<>();
//        for(TroubleCode code: codes){
//            nCodes.add(code.getName());
//        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://s834259532.onlinehome.mx/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TroubleCodeService postService = retrofit.create(TroubleCodeService.class);
        Call< List<APITroubleCode> > call = postService.getTroubleCode();


        call.enqueue(new Callback<List<APITroubleCode>>() {
            @Override
            public void onResponse(Call<List<APITroubleCode>> call, Response<List<APITroubleCode>> response) {
                List<APITroubleCode> listCodes = response.body();
                for(TroubleCode code : codes) {
                    APITroubleCode tCode = listCodes.stream()
                            .filter(cd -> code.getName().equals(cd.getNombre()))
                            .findAny()
                            .orElse(null);
                    code.description = tCode.getDescripcion();
                    adapter.add(code);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<APITroubleCode>> call, Throwable t) {

                System.out.println("Error");
                System.out.println(t.getMessage());

            }
        });


    }

    public class APITroubleCode{
        private String nombre;
        private String descripcion;
        private String autos;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getAutos() {
            return autos;
        }

        public void setAutos(String autos) {
            this.autos = autos;
        }
    }
}
