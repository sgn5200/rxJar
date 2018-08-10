package com.lansent.cannan.watcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lansent.cannan.R;
import com.lansent.cannan.api.cookie.MyResponse;

import java.util.ArrayList;

/**
 * Description    :
 * CreateAuthor: Cannan
 * CreateTime   : 2018/8/9     15:11
 * Project          : MyApplication3
 * PackageName :  com.lansent.cannan.watcher;
 */

public class WatcherAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<MyResponse> listData ;

	public WatcherAdapter(Context c){
		this.context=c;
	}

	public void setListData(ArrayList<MyResponse> listData){
		this.listData = listData ;
		notifyDataSetChanged();
	}
	public void setListData(MyResponse data){
		if(listData==null){
			listData = new ArrayList<>();
		}
		listData.add(data);
		notifyDataSetChanged();
	}

	public ArrayList<MyResponse> getListData() {
		return listData;
	}

	@Override
	public int getCount() {
		return listData==null?0:listData.size();
	}

	@Override
	public MyResponse getItem(int position) {
		return listData==null?null:listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	ViewHolder viewHolder;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_watcher,null);
			viewHolder = new ViewHolder();
			viewHolder.tvId = (TextView) convertView.findViewById(R.id.tvRequestId);
			viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.tvTime.setText(listData.get(position).getCreateTime());
		viewHolder.tvId.setText(listData.get(position).getRequestId());
		return convertView;
	}


	static class ViewHolder{
		  public TextView tvId,tvTime;
	}
}
