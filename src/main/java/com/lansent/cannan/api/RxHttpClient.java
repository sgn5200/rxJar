package com.lansent.cannan.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxHttpClient {

	
	private static String BASE_URL = "https://api.lookdoor.cn";
	private static OkHttpClient.Builder okb;
	private static Retrofit.Builder reb;


	public static OkHttpClient.Builder getOKClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.retryOnConnectionFailure(true)
				.connectTimeout(10, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS);
//				.sslSocketFactory(SSLUtils.getSslSocketFactory().sSLSocketFactory,SSLUtils.UnSafeTrustManager)
//				.addInterceptor(new ReceivedCookiesInterceptor())   //接收保存cookie
//                .addInterceptor(new AddCookiesInterceptor());
		return builder;
	}



	public static <T extends ApiService>T getApiService(OkHttpClient client,Class<T > clazz) {
		reb = new Retrofit.Builder()
				.baseUrl(BASE_URL)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create());//添加RX和Retrofit结合的adapter
//             .addConverterFactory(GsonConverterFactory.create(new Gson()));  //该结构不需要实现Gson，已经自己处理
		reb.client(client);
		return reb.build().create(clazz);
	}
}
