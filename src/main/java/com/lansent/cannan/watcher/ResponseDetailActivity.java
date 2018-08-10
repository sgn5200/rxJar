package com.lansent.cannan.watcher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.lansent.cannan.R;
import com.lansent.cannan.api.cookie.MyResponse;
import com.lansent.cannan.util.Log;

/**
  * @Desc    : 
  * @Params : 
  * @Author : Cannan
  * @Date    : 2018/6/25
  * @Return :  
 */
public class ResponseDetailActivity extends AppCompatActivity {

	private static final String TAG = "ResponseDetailActivity";
	private MyResponse response;
	private TextView tvRequestId,tvRequestHead,tvResponseHead,tvResponseBody;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_feed_detail);
		initView();
		Log.i(TAG);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		response = getIntent().getParcelableExtra("response");
		tvRequestId = (TextView) findViewById(R.id.tvRequestId);
		tvRequestHead = (TextView) findViewById(R.id.tvRequestHead);
		tvResponseHead = (TextView) findViewById(R.id.tvResponseHead);
		tvResponseBody = (TextView) findViewById(R.id.tvResponseBody);

		tvRequestId.setText(response.getRequestId());
		tvRequestHead.setText(response.getRequestHead());
		tvResponseHead.setText(response.getResponseHead());

		String json = JsonUtils.stringToJSON(response.getBodyEntity());
		tvResponseBody.setText(json);
		findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
