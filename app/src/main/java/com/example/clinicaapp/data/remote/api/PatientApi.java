package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.PatientDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface PatientApi {

    @GET("patients")
    Call<List<PatientDto>> getAll();

    @GET("patients/{id}")
    Call<PatientDto> getById(@Path("id") int id);

    @POST("patients")
    Call<PatientDto> create(@Body PatientDto patient);

    @PUT("patients/{id}")
    Call<PatientDto> update(@Path("id") int id, @Body PatientDto patient);

    @DELETE("patients/{id}")
    Call<Void> delete(@Path("id") int id);
}
