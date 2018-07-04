/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant 
 * of patent rights can be found in the PATENTS file in the same directory.
*/

package com.lansent.cannan.api.cookie;


import com.lansent.cannan.api.ApiManager;
import com.lansent.cannan.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;

/**
 * Provides easy integration with <a href="http://square.github.io/okhttp/">OkHttp</a> 3.x by way of
 * the new <a href="https://github.com/square/okhttp/wiki/Interceptors">Interceptor</a> system. To
 * use:
 * <pre>
 *   OkHttpClient client = new OkHttpClient.Builder()
 *       .addNetworkInterceptor(new OkNetworkMonitorInterceptor())
 *       .build();
 * </pre>
 */
public class OkNetworkMonitorInterceptor implements Interceptor {
	private String TAG = getClass().getSimpleName();
	private MyResponse myResponse;

	@Override
	public Response intercept(Chain chain) throws IOException {

		if (ApiManager.getConfig().isStetho()) {
			myResponse = new MyResponse();

			Request request = chain.request();
			myResponse.setRequestId(request.method().toUpperCase() + " " + request.url().toString());
			Log.i(TAG, "request headers " + request.headers().toString());
			myResponse.setRequestHead(request.headers().toString());

			Response response = chain.proceed(request);
			Log.i(TAG, "response headers " + response.message());

			StringBuffer sb = new StringBuffer();
			sb.append("Version: ")
					.append(response.protocol().toString())
					.append("\n")
					.append("Code: ")
					.append(response.code())
					.append("\n")
					.append("Message: ")
					.append(response.message())
					.append("\n")
					.append(response.headers().toString());

			myResponse.setResponseHead(sb.toString());
			if (response.body() != null) {
				InputStream responseStream = saveInterpretResponseStream(response.header("Content-Encoding"), response.body().byteStream());
				if (responseStream != null) {
					response = response.newBuilder()
							.body(new ForwardingResponseBody(response.body(), responseStream))
							.build();
				}
			}
			return response;
		} else {
			return chain.proceed(chain.request());
		}
	}


	private InputStream saveInterpretResponseStream(String contentEncoding, InputStream inputStream) {
		ByteArrayOutputStream byteArrayOutputStream = parseAndSaveBody(inputStream, contentEncoding);
		InputStream newInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		try {
			byteArrayOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return newInputStream;
	}

	private ByteArrayOutputStream parseAndSaveBody(InputStream inputStream, String contentEncoding) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = inputStream.read(buffer)) > -1) {
				byteArrayOutputStream.write(buffer, 0, len);
			}
			byteArrayOutputStream.flush();
			InputStream newStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			BufferedReader bufferedReader;
			if ("gzip".equals(contentEncoding)) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(newStream);
				bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream));
			} else {
				bufferedReader = new BufferedReader(new InputStreamReader(newStream));
			}
			StringBuilder bodyBuilder = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				bodyBuilder.append(line + '\n');
			}
			String body = bodyBuilder.toString();
			myResponse.setBodyEntity(body);
//			Log.i(TAG, "body----" + body);
			ApiManager.getInstance().startInterceptor(myResponse);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
		return byteArrayOutputStream;
	}

	private static class ForwardingResponseBody extends ResponseBody {
		private final ResponseBody mBody;
		private final BufferedSource mInterceptedSource;

		private ForwardingResponseBody(ResponseBody body, InputStream interceptedStream) {
			mBody = body;
			mInterceptedSource = Okio.buffer(Okio.source(interceptedStream));
		}

		@Override
		public MediaType contentType() {
			return mBody.contentType();
		}

		@Override
		public long contentLength() {
			return mBody.contentLength();
		}

		@Override
		public BufferedSource source() {
			return mInterceptedSource;
		}
	}


}
