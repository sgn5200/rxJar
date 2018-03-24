package com.lansent.cannan.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lansent.cannan.util.Log;
import com.lansent.cannan.util.SimpleUtil;

import org.reactivestreams.Publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Description    : api 请求的封装
 *                        定制请求和返回response的处理方式
 * CreateAuthor: Cannan
 * Create time   : 2017/7/26 0026.
 */
public class ApiClient {

	private static String TAG = "API_TAG";

	/**
	 * retrofit 请求service
	 */
	private ApiService service;

	/**
	 * retrofit 请求service
	 *
	 * @param service 由Retrofit 包装的service
	 */
	private ApiClient(ApiService service) {
		this.service = service;
	}
	
	private static ApiClient instance;
	public static ApiClient getInstance(){
		if(instance == null){
			instance = new ApiClient(OkClient.getApiService());
		}
		return instance;
	}

	public static ApiClient getInstance(ApiService service){
		instance = new ApiClient(service);
		return instance;
	}

	/**
	 * 执行网络请求
	 *
	 * @param p<T> p是请求的参数，见{@link URLParam }   T为data泛型
	 *             p包含 url 以及请求方法
	 * @return 具体 Flowable 对象
	 */
	public <T extends BaseResponse<?>> Flowable<T> request(URLParam p, TypeToken<T> token, Context context) {
		Flowable<T> netErrorFb = netError(context);
		if (netErrorFb != null && context!=null) return netErrorFb;

		Flowable<ResponseBody> base = null;
		switch (p.getMethod()) {
			case ApiMethod.GET:
				if (p.getParam() == null || p.getParam().isEmpty()) {
					base = service.get(p.getUrl());
				} else {
					base = service.get(p.getUrl(), p.getParam());
				}
				break;
			case ApiMethod.POST:
				base = service.post(p.getUrl(), p.getParam());
				break;
			case ApiMethod.PUT:
				base = service.put(p.getUrl(), p.getParam());
				break;
			case ApiMethod.DELETE:
				base = service.delete(p.getUrl(), p.getParam());
				break;
			default:
				Log.e(TAG, " method is null");
				break;
		}
		return flatMapOb(base,token);
	}

	/**
	 * 检查网络情况
	 * @return true 有网络
	 */
	private <T extends BaseResponse<?>> Flowable<T> netError(Context context) {

		boolean networkEnable = SimpleUtil.isNetworkAvailable(context);
		if (!networkEnable) {
			Flowable<T> netErrorFb = Flowable.create(new FlowableOnSubscribe<T>() {
				@Override
				public void subscribe(FlowableEmitter<T> e) throws Exception {
					e.onError(new Throwable("网络不可用，请检查后重试"));
					e.onComplete();
				}
			}, BackpressureStrategy.ERROR);
			return netErrorFb;
		}
		return null;
	}


	/**
	 * 转换为泛型中的解析对象
	 *
	 * @param base OKHttp 3 和Retrofit Flowable<ResponseBody>
	 * @param <T>  解析的泛型
	 * @return Flowable<BaseResponse<T>
	 */
	private <T extends BaseResponse<?>> Flowable<T> flatMapOb(Flowable<ResponseBody> base,final TypeToken<T> token) {
		return base.flatMap(new Function<ResponseBody, Publisher<T>>() {
			@Override
			public Publisher<T> apply(ResponseBody responseBody) throws Exception {
				T response = null;
				String dataStr = responseBody.string();
//				Log.i(TAG, dataStr);
				Gson gson = new Gson();
				try{
					response =  gson.fromJson(dataStr,token.getType());
				}catch(Exception e){
					Log.e(TAG, "解析异常：检查json格式和接收泛型");
				}
				return getReturnFlowable(response);
			}

			private Flowable<T> getReturnFlowable(final T t) {
				return Flowable.create(new FlowableOnSubscribe<T>() {
					@Override
					public void subscribe(FlowableEmitter<T> e) throws Exception {
						if (t == null) {
							e.onError(new Throwable("解析异常"));
						} else {
							e.onNext(t);
						}
						e.onComplete();
					}
				}, BackpressureStrategy.ERROR);
			}
		});
	}

	public  Flowable<ResponseBody> uploadFile(String url, RequestBody body, HashMap<String,String> map){
		return service.uploadFile(url,body);
	}

	public Flowable<ResponseBody> requestUploadWork(String url,List<MultipartBody.Part> parts,	Map<String, String> map){
		return service.requestUploadWork(url,map,parts);
	}

	public Flowable<ResponseBody> download(String url){
		return  service.download(url);
	}

}
