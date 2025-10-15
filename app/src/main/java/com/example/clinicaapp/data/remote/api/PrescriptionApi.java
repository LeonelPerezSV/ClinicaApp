package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.PrescriptionDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface PrescriptionApi {

    @GET("prescriptions")
    Call<List<PrescriptionDto>> getAll();

    @GET("prescriptions/{id}")
    Call<PrescriptionDto> getById(@Path("id") int id);

    @POST("prescriptions")
    Call<PrescriptionDto> create(@Body PrescriptionDto prescription);

    @PUT("prescriptions/{id}")
    Call<PrescriptionDto> update(@Path("id") int id, @Body PrescriptionDto prescription);

    @DELETE("prescriptions/{id}")
    Call<Void> delete(@Path("id") int id);
}
