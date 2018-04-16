package com.lansent.cannan.api;

import android.text.TextUtils;

import com.lansent.cannan.api.cookie.AddCookiesInterceptor;
import com.lansent.cannan.api.cookie.ReceivedCookiesInterceptor;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxHttpClient {


	public static class Builder{
		private int connectTimeout = 10;
		private int writeTimeout = 30;
		private int readTimeout = 30;
		private boolean retry = false;
		private SSLSocketFactory sslSocketFactory;
		private X509TrustManager trustManager;
		private boolean useCookie = false;
		private String baseUrl;
		private static Builder builder = new Builder();

		public  Builder setConnectTimeout(int connectTimeout) {
			builder.connectTimeout = connectTimeout;
			return builder;
		}

		public  Builder setWriteTimeout(int writeTimeout) {
			builder.writeTimeout = writeTimeout;
			return builder;
		}

		public  Builder setReadTimeout(int readTimeout) {
			builder.readTimeout = readTimeout;
			return builder;
		}

		public  Builder setRetry(boolean retry) {
			builder.retry = retry;
			return builder;
		}

		public  Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
			builder.sslSocketFactory = sslSocketFactory;
			return builder;
		}

		public  Builder setTrustManager(X509TrustManager trustManager) {
			builder.trustManager = trustManager;
			return builder;
		}

		public Builder setUseCookie(boolean useCookie) {
			builder.useCookie = useCookie;
			return builder;
		}

		public Builder setBaseUrl(String baseUrl) {
			builder.baseUrl = baseUrl;
			return builder;
		}
	}

	protected static Retrofit getClient(Builder config) {
		Retrofit.Builder reBuilder = new Retrofit.Builder();
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
				.writeTimeout(config.writeTimeout,TimeUnit.SECONDS)
				.readTimeout(config.readTimeout,TimeUnit.SECONDS)
				.retryOnConnectionFailure(config.retry);
		if(config.sslSocketFactory!=null){
			builder.sslSocketFactory(config.sslSocketFactory,config.trustManager);
		}
		if(config.useCookie){
			builder.addInterceptor(new AddCookiesInterceptor());
			builder.addInterceptor(new ReceivedCookiesInterceptor());
		}

		if(!TextUtils.isEmpty(config.baseUrl)){
			reBuilder.baseUrl(config.baseUrl);
		}
		reBuilder.addCallAdapterFactory(RxJava2CallAdapterFactory.create()); //添加RX和Retrofit结合的adapter
		//.addConverterFactory(GsonConverterFactory.create(new Gson()));  //该结构不需要实现Gson，已经自己处理
		 return reBuilder.client(builder.build()).build();
	}
}
