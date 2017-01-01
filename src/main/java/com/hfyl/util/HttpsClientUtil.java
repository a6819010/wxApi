package com.hfyl.util;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * 
* @Title: YouguuHttpsClient.java 
* @Package com.youguu.game.pay.util 
* @Description: http请求类
* @author wangd
* @date 2014年5月5日 上午9:19:04 
* @version V1.0
 */
public class HttpsClientUtil {
	private static String CHARCODING = "UTF-8";
	private int TIMEOUT = 3000;
	public static HttpsClientUtil getClient(){
		return new HttpsClientUtil();
	}
	
	/**
	 * get请求
	 * @param url
	 * @return
	 */
	public String doGet(String url){
		
		InputStreamReader insr = null;
		BufferedReader br = null;
		try {
			
			URL reqURL = new URL(url); //创建URL对象
			HttpsURLConnection httpsConn = (HttpsURLConnection)reqURL.openConnection();
			httpsConn.setSSLSocketFactory(this.getSSLSocketFactory());
			//设置连接和超时时间
			httpsConn.setConnectTimeout(TIMEOUT);
			httpsConn.setReadTimeout(TIMEOUT);
			insr = new InputStreamReader(httpsConn.getInputStream(),CHARCODING);
			br = new BufferedReader(insr);
			StringBuffer sb = new StringBuffer("");
			String line  = null;
			while( (line = br.readLine()) !=null){
				sb.append(line);
			}
			httpsConn.disconnect();
			return sb.toString();		
		}catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(insr!=null){
				try {
					insr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * post请求
	 * @param url
	 * @return
	 */
	public String doPost(String url,String content){
		OutputStream os = null;
		InputStreamReader insr = null;
		BufferedReader br = null;
		OutputStreamWriter osw = null;
		try {
			URL reqURL = new URL(url);
			HttpsURLConnection httpsConn = (HttpsURLConnection)reqURL.openConnection();
			
			httpsConn.setSSLSocketFactory(this.getSSLSocketFactory());
			//设置连接和超时时间
			httpsConn.setConnectTimeout(TIMEOUT);
			
			httpsConn.setReadTimeout(TIMEOUT);
			
			// 以post方式通信
			httpsConn.setRequestMethod("POST");
			// 设置请求默认属性
//			httpsConn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			
			if(content!=null){
				httpsConn.setDoOutput(true);
				
				os = httpsConn.getOutputStream();
				
				osw = new OutputStreamWriter(os, "UTF-8");
				
				osw.write(content);
				
				osw.flush();
				
				
			}
			
			
			
			if(httpsConn.getResponseCode()==200){
				insr = new InputStreamReader(httpsConn.getInputStream(),CHARCODING);
				br = new BufferedReader(insr);
				StringBuffer sb = new StringBuffer("");
				String line  = null;
				while( (line = br.readLine()) !=null){
					sb.append(line);
				}
				httpsConn.disconnect();
				return sb.toString();
			}
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(osw!=null){
				try {
					osw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(os!=null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(insr!=null){
				try {
					insr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(br!=null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

	public Response<String> sendGet(String url, Map<String, String> params,Map<String, String> headParam,String charset){
		Response<String> res = new Response<String>();
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			String param = null;
			if(params!=null && params.size()>0){
				StringBuilder paramBuffer = new StringBuilder();
				for(Map.Entry<String, String> entry:params.entrySet()){
					paramBuffer.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), charset)).append("&");
				}
				param = paramBuffer.substring(0, paramBuffer.length()-1);
			}

			String urlNameString = url;
			if(param!=null){
				if(url.indexOf("?")>0){
					urlNameString = url + "&" + param;
				}else{
					urlNameString = url + "?" + param;
				}
			}


			URL realUrl = new URL(urlNameString);

			HttpsURLConnection connection = (HttpsURLConnection)realUrl.openConnection();

			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setRequestProperty("Accept-Charset",charset);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset="+ charset);
			if(headParam!=null && headParam.size()>0){
				for(Map.Entry<String, String> entry:headParam.entrySet()){
					connection.setRequestProperty(entry.getKey(),entry.getValue());
				}
			}
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(15000);
			connection.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			connection.connect();



			int code = connection.getResponseCode();

			if(code==200){
				in = new BufferedReader(new InputStreamReader(connection.getInputStream(),charset));
				String line;
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
				res.setCode("0000");
				res.setMsg("ok");
				res.setT(result.toString());
			}else{
				res.setCode("1001");
				res.setMsg("返回码异常:" + code);
			}


		}catch(SocketTimeoutException e){
			res.setCode("1001");
			res.setMsg("连接超时");
			e.printStackTrace();
		}catch (Exception e) {
			res.setCode("1001");
			res.setMsg("连接异常");
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				res.setCode("1002");
				res.setMsg("关闭连接异常");
				e2.printStackTrace();
			}
		}
		return res;
	}

	/**
	 *  POST发送xml请求
	 * @param url
	 * @param charset
	 * @param xml
     * @return
     */
	public Response<String> sendPostXml(String url,String charset,String xml)
	{
		Response<String> res = new Response<String>();
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection) realUrl.openConnection();
			connection.setRequestMethod("POST");// 提交模式
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setRequestProperty("Accept-Charset",charset);
			connection.setRequestProperty("Content-Type","text/xml");

			connection.setConnectTimeout(10000);
			connection.setReadTimeout(15000);
			connection.setDoOutput(true);
			connection.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			connection.connect();

			byte[] bypes = xml.getBytes();

			connection.getOutputStream().write(bypes);// 输入参数

			int code = connection.getResponseCode();

			if(code==200){
				in = new BufferedReader(new InputStreamReader(connection.getInputStream(),charset));
				String line;
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
				res.setCode("0000");
				res.setMsg("ok");
				res.setT(result.toString());
			}else{
				res.setCode("1001");
				res.setMsg("返回码异常:" + code);
			}
		}catch(SocketTimeoutException e){
			res.setCode("1001");
			res.setMsg("连接超时");
			e.printStackTrace();
		}catch (Exception e) {
			res.setCode("1001");
			res.setMsg("连接异常");
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				res.setCode("1002");
				res.setMsg("关闭连接异常");
				e2.printStackTrace();
			}
		}
		return res;
	}

	/**
	 *
	 * @Title: sendPost
	 * @Description: 发送Post请求
	 * @param @param url
	 * @param @param params
	 * @param @param charset
	 * @param @return
	 * @return Response<String>    返回类型
	 * @throws
	 */
	public Response<String> sendPost(String url, Map<String, String> params,Map<String, String> headParam,String charset){
		Response<String> res = new Response<String>();
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection) realUrl.openConnection();
			connection.setRequestMethod("POST");// 提交模式
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setRequestProperty("Accept-Charset",charset);
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset="+ charset);

			if(headParam!=null && headParam.size()>0){
				for(Map.Entry<String, String> entry:headParam.entrySet()){
					connection.setRequestProperty(entry.getKey(),entry.getValue());
				}
			}

			connection.setConnectTimeout(10000);
			connection.setReadTimeout(15000);
			connection.setDoOutput(true);
			connection.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			connection.connect();

			String param = null;
			if(params!=null && params.size()>0){

				StringBuilder paramBuffer = new StringBuilder();

				for(Map.Entry<String, String> entry:params.entrySet()){

					paramBuffer.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(),charset)).append("&");
				}
				param = paramBuffer.substring(0, paramBuffer.length()-1);

				byte[] bypes = param.toString().getBytes();

				connection.getOutputStream().write(bypes);// 输入参数
			}

			int code = connection.getResponseCode();

			if(code==200){
				in = new BufferedReader(new InputStreamReader(connection.getInputStream(),charset));
				String line;
				while ((line = in.readLine()) != null) {
					result.append(line);
				}
				res.setCode("0000");
				res.setMsg("ok");
				res.setT(result.toString());
			}else{
				res.setCode("1001");
				res.setMsg("返回码异常:" + code);
			}


		}catch(SocketTimeoutException e){
			res.setCode("1001");
			res.setMsg("连接超时");
			e.printStackTrace();
		}catch (Exception e) {
			res.setCode("1001");
			res.setMsg("连接异常");
			e.printStackTrace();
		}
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				res.setCode("1002");
				res.setMsg("关闭连接异常");
				e2.printStackTrace();
			}
		}



		return res;
	}
	public static void main(String[] args){

		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", "youguu");
		params.put("client_secret", "3cf01470-140b-40bd-909f-0d95b9c1f70c");
		params.put("code", "cf0e2ab1-48b8-4dd0-af2d-cb363b1eaf4b");
		params.put("redirect_uri", "http://test.youguu.com/mobile/wap_trade/guangfa");
		params.put("grant_type", "authorization_code");


//		System.out.println(HttpsUtil.sendPost("https://testauth.gf.com.cn/server/ws/pub/token/access_token", params, "UTF-8"));
	}
	
	
	/**
	 * 
	* @Title: YouguuHttpsClient.java 
	* @Package com.youguu.game.pay.util 
	* @Description: 证书信任管理
	* @author wangd
	* @date 2014年5月5日 上午9:23:10 
	* @version V1.0
	 */
	class MyX509TrustManager implements javax.net.ssl.X509TrustManager{

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		
	}
	
	private SSLSocketFactory getSSLSocketFactory(){
		SSLSocketFactory ssf  = null;
		TrustManager[] tm = {new MyX509TrustManager ()}; 
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("SSL","SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom()); 
			ssf = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//从上述SSLContext对象中得到SSLSocketFactory对象
		
		return ssf;
	}
	
}
