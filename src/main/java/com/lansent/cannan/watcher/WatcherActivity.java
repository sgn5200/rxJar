package com.lansent.cannan.watcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lansent.cannan.R;
import com.lansent.cannan.api.cookie.MyResponse;
import com.lansent.cannan.base.BaseActivity;
import com.lansent.cannan.util.Log;

import java.util.ArrayList;

public class WatcherActivity extends BaseActivity implements View.OnClickListener {

	private ListView listView;
	private WatcherAdapter adapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watcher);
		Log.i(TAG);
		initView();

		ArrayList<MyResponse> list = getIntent().getParcelableArrayListExtra("response");
		adapter.setListData(list);
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG);

	}

	private void initView() {
		listView = (ListView) findViewById(R.id.listView);
		adapter = new WatcherAdapter(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(WatcherActivity.this, ResponseDetailActivity.class);
				intent.putExtra("response", adapter.getItem(position));
				startActivity(intent);
			}
		});


		findViewById(R.id.ivBack).setOnClickListener(this);
		findViewById(R.id.btRight).setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG);
	}


	@Override
	public void onClick(View v) {
		Log.i(TAG, v.getId() + "");
		if (v.getId() == R.id.btRight) {
			if (adapter.getListData() != null) {
				adapter.getListData().clear();
				adapter.notifyDataSetChanged();
			}
		} else if (v.getId() == R.id.ivBack) {
			finish();
		}
	}
}
