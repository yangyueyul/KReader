//package com.koolearn.android.volley;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.android.volley.AuthFailureError;
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.NetworkResponse;
//import com.android.volley.Request;
//import com.android.volley.Response;
//import com.android.volley.Response.ErrorListener;
//import com.android.volley.toolbox.HttpHeaderParser;
//import com.koolearn.android.volley.FastResponse.Listener;
//
//public class FastJSONRequest extends Request<JSONObject>{
//	private Listener<JSONObject> listener = null;
//	private String executeMethod = "";
//	private String flag = "";
//	private boolean dialogFlag = true;
//	private HashMap<String,String> headers;
//
//	/**
//	 * 默认GET方法
//	 * @param url
//	 * @param listener
//	 * @param errorListener
//	 */
//	public FastJSONRequest(String url,String executeMethod,Listener<JSONObject> listener,
//			ErrorListener errorListener){
//		// 默认get请求
//		this(Method.GET,url,executeMethod,listener,errorListener);
//	}
//	/**
//	 * 默认GET方法
//	 * @param url
//	 * @param listener
//	 * @param errorListener
//	 */
//	public FastJSONRequest(String url,String executeMethod,Listener<JSONObject> listener,
//			ErrorListener errorListener,boolean dialogFlag){
//		// 默认get请求
//		this(Method.GET,url,executeMethod,"0",dialogFlag,listener,errorListener);
//	}
//
//	public FastJSONRequest(int method, String url, String executeMethod,Listener<JSONObject> listener,
//			ErrorListener errorListener) {
//		// 默认取出服务端results字段
//		this(Method.GET,url,executeMethod,"0",true,listener,errorListener);
//	}
//
//	public FastJSONRequest(int method,String url,String executeMethod,String flag,boolean dialogFlag,Listener<JSONObject> listener,
//			ErrorListener errorListener) {
//		super(method, url, errorListener);
//		this.setRetryPolicy(new DefaultRetryPolicy(15000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		this.listener = listener;
//		this.executeMethod = executeMethod;
//		this.flag = flag;
//		this.dialogFlag = dialogFlag;
//	}
//
//	@Override
//	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//		JSONObject json = null;
//		try{
//			String str = new String(response.data,HttpHeaderParser.parseCharset(response.headers));
//			System.out.println(str);
//			json = JSONObject.parseObject(str);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
//	}
//
//	@Override
//	protected void deliverResponse(JSONObject response) {
//		listener.onResponse(response,executeMethod,flag,dialogFlag);
//	}
//
//	@Override
//	public Map<String, String> getHeaders() throws AuthFailureError {
//		return this.headers;
//	}
//
//	public void setHaders(HashMap<String, String> params) {
//		this.headers = params;
//	}
//
//
//
//}
