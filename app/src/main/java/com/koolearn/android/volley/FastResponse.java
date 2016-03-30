//package com.koolearn.android.volley;
//
//import com.alibaba.fastjson.JSONObject;
//import com.android.volley.VolleyError;
//
//public class FastResponse<T> {
//
//	public interface Listener<T>{
//		/**
//		 *
//		 * @param obj
//		 * 		操作的数据类型
//		 * @param executeMethod
//		 * 		反射要用的方法
//		 * @param flag	傻逼服务器各接口返回数据处理逻辑不一样
//		 * 		0:取results字段
//		 * 		1:取state
//		 * 		2:取msg
//		 * @param dialogFlag
//		 * 		true:请求完，主动mis掉通讯框（默认）
//		 * 		false:请求完，手动mis掉通讯框
//		 */
//		public void onResponse(T obj, String executeMethod, String flag, boolean dialogFlag);
//	}
//
//}
