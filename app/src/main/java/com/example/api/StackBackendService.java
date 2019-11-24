package com.example.api;

import com.example.data.Index;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface StackBackendService {

    @POST("getIndex/")
    Call<List<Index.Book>> getIndex();

    @Headers("Content-Type: application/json ")
    @POST("getFake/")
    Call<ResponseBody> getFake(@Body RequestBody body);
}
