//package com.koolearn.android.volley;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//
//public class VolleyManager {
//	public static final String UTF_8 = "UTF-8";
//	private static VolleyManager instance;
////	private HashMap<String,String> appUserAgent = null;
//
//	private VolleyManager(){
//	}
//
//	// 本app的单例基本无用
//	public static VolleyManager getInstance(){
//		if (instance == null){
//			synchronized (VolleyManager.class) {
//				instance = new VolleyManager();
//			}
//		}
//		return instance;
//	}
//
//	public void beginSubmitRequest(RequestQueue queue,FastJSONRequest request){
//		// 自定义请求需要设置headers
//		request.setHaders(new HashMap<String, String>());
//		queue.add(request);
//	}
//	public void beginSubmitRequest(RequestQueue queue,StringRequest request){
//		queue.add(request);
//	}
//
//	public void beginSubmitRequest(RequestQueue queue,FastJSONRequest request,Map<String,String> params){
////		getUserAgent(application);
////		for(String name:params.keySet()){
////			appUserAgent.put(name,params.get(name));
////		}
//	}
//
////	/**
////	 *
////	 * getUserAgent:(用户手机相关数据). <br/>
////	 *
////	 * @author leixun
////	 * @param appContext
////	 * @return
////	 * @since 1.0
////	 */
////	private HashMap<String,String> getUserAgent(BaseApplication appContext) {
////		if(appUserAgent == null) {
////			appUserAgent = new HashMap<String,String>();
////			appUserAgent.put("m_os_version", android.os.Build.VERSION.RELEASE+"");
////			appUserAgent.put("m_hd_type", android.os.Build.MODEL);
////			appUserAgent.put("m_client_type", "android");
////			appUserAgent.put("m_serial",SystemTool.getPhoneIMEI(appContext)); //手机序列号
////			appUserAgent.put("lang",SystemTool.getLang(appContext)); //手机语言
////			appUserAgent.put("m_hd_type", android.os.Build.MODEL);	// 手机型号
////		}
////		appUserAgent.put("timestamp", appContext.getTimestamp());
////		// 设置 请求超时时间
////		appUserAgent.put("Host", URLs.HOST);
////		appUserAgent.put("Connection","Keep-Alive");
////		appUserAgent.put("Cookie", null);
////		if(appContext.isLogin()){
////			String timestamp = SystemTool.getTimeStamp()+"";
////			appUserAgent.put("timestamp",timestamp);
////			try{
////				appUserAgent.put("token",DES3.encode(
////						URLEncoder.encode(appContext.getLoginUser().getAccountuno()+"#"+
////				appContext.getLoginUser().getAccessToken()+"#"+timestamp,UTF_8)
////						));
////			}catch(Exception e){
////				e.printStackTrace();
////			}
////		} else{
////		}
////		return appUserAgent;
////	}
//
//}
