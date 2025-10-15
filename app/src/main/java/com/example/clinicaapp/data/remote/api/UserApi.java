package com.example.clinicaapp.data.remote.api;

import com.example.clinicaapp.data.remote.dto.UserDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApi {

    @GET("users")
    Call<List<UserDto>> getAll();

    @GET("users/{id}")
    Call<UserDto> getById(@Path("id") int id);

    @POST("users")
    Call<UserDto> create(@Body UserDto user);

    @PUT("users/{id}")
    Call<UserDto> update(@Path("id") int id, @Body UserDto user);

    @DELETE("users/{id}")
    Call<Void> delete(@Path("id") int id);

    @POST("login")
    Call<UserDto> login(@Body UserDto credentials);
}
