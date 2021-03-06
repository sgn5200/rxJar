package com.lansent.cannan.api.cookie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description    :
 * CreateAuthor: Cannan
 * CreateTime   : 2018/6/23     14:59
 * Project          : MyApplication3
 * PackageName :  com.lansent.cannan.api.cookie;
 */

public class MyResponse implements Parcelable {
	private String requestId;
	private String requestHead;
	private String responseHead;
	private String bodyEntity;

	private String createTime;

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getRequestHead() {
		return requestHead;
	}

	public void setRequestHead(String requestHead) {
		this.requestHead = requestHead;
	}

	public String getResponseHead() {
		return responseHead;
	}

	public void setResponseHead(String responseHead) {
		this.responseHead = responseHead;
	}

	public String getBodyEntity() {
		return bodyEntity;
	}

	public void setBodyEntity(String bodyEntity) {
		this.bodyEntity = bodyEntity;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.requestId);
		dest.writeString(this.requestHead);
		dest.writeString(this.responseHead);
		dest.writeString(this.bodyEntity);
		dest.writeString(this.createTime);
	}

	public MyResponse() {
	}

	protected MyResponse(Parcel in) {
		this.requestId = in.readString();
		this.requestHead = in.readString();
		this.responseHead = in.readString();
		this.bodyEntity = in.readString();
		this.createTime = in.readString();
	}

	public static final Parcelable.Creator<MyResponse> CREATOR = new Parcelable.Creator<MyResponse>() {
		@Override
		public MyResponse createFromParcel(Parcel source) {
			return new MyResponse(source);
		}

		@Override
		public MyResponse[] newArray(int size) {
			return new MyResponse[size];
		}
	};
}
