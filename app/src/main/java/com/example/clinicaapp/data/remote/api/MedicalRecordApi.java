package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.MedicalRecordDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface MedicalRecordApi {

    @GET("medical_records")
    Call<List<MedicalRecordDto>> getAll();

    @GET("medical_records/{id}")
    Call<MedicalRecordDto> getById(@Path("id") int id);

    @POST("medical_records")
    Call<MedicalRecordDto> create(@Body MedicalRecordDto record);

    @PUT("medical_records/{id}")
    Call<MedicalRecordDto> update(@Path("id") int id, @Body MedicalRecordDto record);

    @DELETE("medical_records/{id}")
    Call<Void> delete(@Path("id") int id);
}
