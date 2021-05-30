package com.TT.verifacil;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Utils.ATCommands.ATZ;
import Utils.Command;
import Utils.OBDCommands.CountDTC;
import Utils.OBDCommands.ReadDTC;
import Utils.TroubleCode;
import Utils.TroubleCodeService;
import Utils.Utils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void troubleCode_isCorrect(){
        try{
            TroubleCode testTroubleCode = new TroubleCode("013");
            assertEquals(true, testTroubleCode.isSAECode() );
            assertEquals("P0133",testTroubleCode.getName());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void troubleCode1_isCorrect(){assertEquals(true, TroubleCode.readCode("0A3A"));}

    @Test
    public void ReadDTC_isCorrect() throws ExecutionException {
        ReadDTC prueba = new ReadDTC();

//        prueba.interpretResult("43 01 33 00 00 00 00 43 01 33 00 00 00 00");
//        System.out.println(prueba.getTroubleCodes());

        prueba.setISO(true);
        prueba.interpretResult("430101330000000000000000");
        System.out.println(prueba.getTroubleCodes());
    }

    @Test
    public void ATZ_isCorrect() throws ExecutionException{
        ATZ atz = new ATZ();
        //atz.interpretResult("ELM327 v2.3");
        System.out.println("IsOk: " + atz.isOK());
        System.out.println(atz.getELMVersion());
    }

    @Test
    public void CountDTC_isCorrect() throws ExecutionException, DecoderException {
        CountDTC count = new CountDTC();
        count.interpretResult("41 01 81 07 65 04");
    }

    @Test
    public void Command_Has_Error_isCorrect() throws ExecutionException {
        System.out.println("UNABLE TO CONNECT".matches(".*"+"UNABLE TO CONNECT"+".*"));
//        Command.hasError("UNABLE TO CONNECT");
    }

    @Test
    public void APIRetriveCodes_isCorrect() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://s834259532.onlinehome.mx/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TroubleCodeService postService = retrofit.create(TroubleCodeService.class);
        Call<List<TroubleCode.APITroubleCode>> call = postService.getTroubleCode();

        System.out.println(call.execute().body());
    }

    @Test
    public void Pruebas_isCorrect() throws DecoderException {
        System.out.println(Integer.parseInt("FFFF",16));
        System.out.println(Integer.parseInt("10101010101",2));
        System.out.println( BinaryCodec.toAsciiString(Hex.decodeHex("0070".toCharArray())) );
        System.out.println(Utils.hexToBin("00076500"));
    }

}