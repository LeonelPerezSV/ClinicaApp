package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.AppointmentDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface AppointmentApi {

    @GET("appointments")
    Call<List<AppointmentDto>> getAll();

    @GET("appointments/{id}")
    Call<AppointmentDto> getById(@Path("id") int id);

    @POST("appointments")
    Call<AppointmentDto> create(@Body AppointmentDto appointment);

    @PUT("appointments/{id}")
    Call<AppointmentDto> update(@Path("id") int id, @Body AppointmentDto appointment);

    @DELETE("appointments/{id}")
    Call<Void> delete(@Path("id") int id);
}
