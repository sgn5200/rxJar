# rxJar
rx with retrofit2.0 for eclipse and android studio 
自己封装的库，一个rxJar的模块，可使用于studio中，也可使用在eclipse中，在eclipce中需要使用将该项目导出为jar包，具体导出方法在Android studio Terminal中执行 gradlew makeJar 生成jar包立即可用。 根目录存在一个cannan.jar就是早期导出的jar包，不包含新的更改。
	
# 初始化

//setStetho为是否开启网络抓包，需要存储权限
 在Application oncreate()中Utils.init(this, ApiManager.getConfig().setStetho(true));  //初始化
	
# 使用例子	
	
	private void request() {
		URLParam param = new URLParam("http://gank.io/api/xiandu/categories");
		param.setMethod(Method.GET);

		/**
		 * request中可设置泛型解析类型，其中格式 ? extend BaseResponse ,? 可代表一个实体对象，或者一个集合对象
		 */
		TypeToken<MyResponse<List<Bean>>>  token = new TypeToken<MyResponse<List<Bean>>>(){};
		ApiManager.getInstance().request(param,token)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new BaseSub<MyResponse<List<Bean>>>() {
			@Override
			public void callSuccess(MyResponse<List<Bean>> response) {
				Log.i("cannan",response.getError()+"");
				Log.i("cannan",response.getData().get(0).getName()+"");
			}

			@Override
			public void CallFailure(MyResponse<List<Bean>> response) {
				Log.i("cannan",response.toString());
			}

			@Override
			public void callError(String e) {
				Log.i("cannan",e);
			}
		});
	}


	/**
	 * 例如 某工程师习惯返回 {error:false,results:[{a,b,c,d}]}
	 * json 的根目录 T为对象或集合对象
	 * @param <T>
	 */
	class MyResponse<T> extends BaseResponse<T>{
		  boolean error;
		  T results;

		@Override
		public void setData(T results) {
			super.setData(results);
			this.results = results;
		}

		@Override
		public T getData() {
			return results;
		}

		public void setError(boolean error) {
			this.error = error;
		}

		public boolean getError(){
			return this.error;
		}
	}

	/**
	 * 真正有用的返回实体
	 * 使用 ？extend BaseResponse 包装
	 */
	class Bean{

		/**
		 * _id : 5829b881421aa911dbc9156b
		 * en_name : teamblog
		 * name : 团队博客
		 * rank : 600
		 */

		private String _id;
		private String en_name;
		private String name;
		private int rank;

		public String get_id() {
			return _id;
		}

		public void set_id(String _id) {
			this._id = _id;
		}

		public String getEn_name() {
			return en_name;
		}

		public void setEn_name(String en_name) {
			this.en_name = en_name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}
	}
