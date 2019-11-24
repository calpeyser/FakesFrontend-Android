package com.example.api;

import android.arch.lifecycle.ViewModel;

import com.example.data.Index;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.MediaType;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.util.Log.d;

public class StackBackendDao extends ViewModel {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    StackBackendService service = new Retrofit.Builder()
            .baseUrl("https://us-central1-realstack-e4286.cloudfunctions.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(StackBackendService.class);

    public Call<List<Index.Book>> getIndex() {
        return service.getIndex();
    }

    public Call<ResponseBody> getFake(String bookName, String fakeName) {
        d("dao", "Get book " + bookName + " fake " + fakeName);
        RequestBody body = RequestBody.create(JSON, "{\"book_code\":\"" + bookName + "\",\"fake_code\":\"" + fakeName + "\"}");
        return service.getFake(body);
    }

}
