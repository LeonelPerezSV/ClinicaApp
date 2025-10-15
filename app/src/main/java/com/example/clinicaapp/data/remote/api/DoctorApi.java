package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.DoctorDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface DoctorApi {

    @GET("doctors")
    Call<List<DoctorDto>> getAll();

    @GET("doctors/{id}")
    Call<DoctorDto> getById(@Path("id") int id);

    @POST("doctors")
    Call<DoctorDto> create(@Body DoctorDto doctor);

    @PUT("doctors/{id}")
    Call<DoctorDto> update(@Path("id") int id, @Body DoctorDto doctor);

    @DELETE("doctors/{id}")
    Call<Void> delete(@Path("id") int id);
}
