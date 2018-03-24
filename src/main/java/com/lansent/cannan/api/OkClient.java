package com.lansent.cannan.api;

import com.lansent.cannan.api.cookie.AddCookiesInterceptor;
import com.lansent.cannan.api.cookie.ReceivedCookiesInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class OkClient {

	
	private static final String BASE_URL = "https://api.lookdoor.cn";

	private static OkHttpClient getOKClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.connectTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS);
		builder
                .addInterceptor(new ReceivedCookiesInterceptor())   //接收保存cookie
                .addInterceptor(new AddCookiesInterceptor());
		return builder.build();
	}

	public static ApiService getApiService() {
		Retrofit.Builder retrofit = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//添加RX和Retrofit结合的adapter
//             .addConverterFactory(GsonConverterFactory.create(new Gson()));  //该结构不需要实现Gson，已经自己处理
		retrofit.client(getOKClient());
		return retrofit.build().create(ApiService.class);
	}
}
