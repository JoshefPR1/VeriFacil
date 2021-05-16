package Utils;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

public interface TroubleCodeService {
    String API_ROUTE = "/api/codigos/";

    @GET(API_ROUTE)
    Call<List<TroubleCode.APITroubleCode>> getTroubleCode();
}
