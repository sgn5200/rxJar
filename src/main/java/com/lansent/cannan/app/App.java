package com.lansent.cannan.app;

import android.app.Application;

import com.google.gson.reflect.TypeToken;
import com.lansent.cannan.api.ApiClient;
import com.lansent.cannan.api.BaseResponse;
import com.lansent.cannan.api.BaseSub;
import com.lansent.cannan.api.URLParam;
import com.lansent.cannan.util.RxEvent;

import io.reactivex.functions.Consumer;

/**
 * Created by Cannan on 2017/9/26 0026.
 * 全局 Application
 * <p>
 * 初始化 bugly 、Stetho
 */

public class App extends Application {

	private static App mContext;
	private String TAG = getClass().getSimpleName();

	public static App getInstance() {
		return mContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;

		RxEvent.getInstance().register(BaseResponse.class, new Consumer<BaseResponse>() {
			@Override
			public void accept(BaseResponse ba) throws Exception {

			}
		}, new Consumer<Throwable>() {
			@Override
			public void accept(Throwable throwable) throws Exception {

			}
		});

		RxEvent.getInstance().unRegister(BaseResponse.class);



		ApiClient.getInstance().request(new URLParam(""), new TypeToken<BaseResponse<String>>() {}, this)
				.subscribe(new BaseSub<BaseResponse<String>>() {
					@Override
					public void callSuccess(BaseResponse<String> response) {

					}

					@Override
					public void CallFailure(BaseResponse<String> response) {

					}

					@Override
					public void callError(String e) {

					}
				});

		BaseResponse baseResponse = new BaseResponse();
		RxEvent.getInstance().post(baseResponse);
	}

}
