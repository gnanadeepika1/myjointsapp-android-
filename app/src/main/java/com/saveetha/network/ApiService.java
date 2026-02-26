package com.saveetha.network;

import com.saveetha.myjoints.PainResponse;
import com.saveetha.myjoints.data.DiseaseScores;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ✅ FIXED: use FormUrlEncoded + Field (CRITICAL)
    @FormUrlEncoded
    @POST("save_graph.php")
    Call<Map<String, Object>> insertDiseaseScore(
            @Field("patient_id") String patientId,
            @Field("tjc") int tjc,
            @Field("sjc") int sjc,
            @Field("pga") float pga,
            @Field("ea") float ea,
            @Field("crp") float crp
    );

    // UNCHANGED
    @GET("get_graph.php")
    Call<DiseaseScores> getGraph(@Query("patient_id") String patientId);

    // UNCHANGED
    @POST("save_daily_pain.php")
    Call<Map<String, Object>> saveDailyPainValue(@Body Map<String, Object> request);

    // UNCHANGED
    @GET("get_daily_pain_value.php")
    Call<PainResponse> getPainValues(@Query("user_id") String patientId);
}
