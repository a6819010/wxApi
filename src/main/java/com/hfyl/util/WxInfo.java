package com.hfyl.util;

import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by xyj on 2016/12/8.
 */
public class WxInfo {

    public  String access_token;

    private  String jsapi_ticket;

    private  long lasttime  = 0 ;

    private String appId;

    private String appSecret;

    /**
     *  商户名称
     */
    public static String MCHNAME = "北京佳品益德健康科技有限公司-自检商品";

    /**
     * 微信异步回调接口地址
     */
    public static String notifyUrl = "";

    /**
     *  交易类型为公众号支付
     */
    public static String tradeType = "JSAPI";

    /**
     *  商户号
     */
    private String mch_id;

    /**
     * 商户密钥key
     */
    private static String mch_key = "W1u2h3u4a5j6i7a8n9x1u2y3u4n5j6ie";

    private static Hashtable<Integer,WxInfo> map = new Hashtable();

    private String grant_type = "client_credential";

    private static WxInfo cacheWxInfo;

    /**
     * 获取token的url
     */
    private String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";

    /**
     * 获取jsapi接口
     */
    private String jsapiUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket";

    /**
     * 创建菜单接口
     */
    private String careateMenuUrl = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=%s";

    /**
     * 微信下单接口
     */
    public static String ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    /**
     *  获取微信token的接口地址
     */
    private String get_weixin_token_api = "https://api.weixin.qq.com/sns/oauth2/access_token";

    public static String SUCCESS = "SUCCESS"; //成功return_code

    public static String FAIL = "FAIL";   //失败return_code

    public String getAppId() {
        return appId;
    }

    /**
     *  创建菜单
     * @return
     */
    public String getCareateMenuUrl() {
        if(this.access_token==null){
            this.getToken();
        }
        careateMenuUrl = String.format(careateMenuUrl,this.access_token);
        return careateMenuUrl;
    }

    public String getToken(){
        StringBuffer sb = new StringBuffer(this.tokenUrl);
        sb.append("?")
                .append("grant_type").append("=").append(grant_type).append("&")
                .append("appid").append("=").append(appId).append("&")
                .append("secret").append("=").append(appSecret);
        String response =  HttpsClientUtil.getClient().doGet(sb.toString());
        String access_token_new = null;
        if(response!=null){
            access_token_new = JSONObject.parseObject(response).getString("access_token");
            this.access_token = access_token_new;
        }
        return access_token_new;
    }

    /**
     *  根据code获取openid
     * @param code
     * @return
     */
    private JSONObject getWxData(String code)
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",appId);
        paramMap.put("secret",appSecret);
        paramMap.put("code", code);
        paramMap.put("grant_type", "authorization_code");
        Response<String> res = HttpsClientUtil.getClient().sendGet(get_weixin_token_api, paramMap, null, "UTF-8");
        if("0000".equals(res.getCode())){
            String result = res.getT();
            JSONObject o = JSONObject.parseObject(result);
            //如果获取token正常
            if(!o.containsKey("errcode"))
            {
                //o.getString("openid");
                return o;
            }
        }
        return null;
    }

    public String getOpenId(String code)
    {
        JSONObject obj = getWxData(code);
        return obj.getString("openid");
    }

    public WxInfo()
    {
        //测试环境
        //this.appId = "wx7f33eae536af41dc";
        //this.appSecret = "931116f743ebcb4fc6677c76020dc2f4";

        //生产环境
        this.appId = "wx4f81fa7f2a9ee51e";
        this.appSecret = "1a11cf20785fd083238b10a9dc20b4d9";
        this.mch_id = "1415330802";
    }

    public String getMchId() {
        return mch_id;
    }

    public static WxInfo getCacheWxInfo(){
        if(cacheWxInfo != null)
        {
            return cacheWxInfo;
        }
        cacheWxInfo = new WxInfo();
        return cacheWxInfo;
    }

    public String getJsapiPath(String access_token){
        StringBuilder sb = new StringBuilder(this.jsapiUrl);
        sb.append("?")
                .append("access_token").append("=").append(this.access_token).append("&")
                .append("type").append("=").append("jsapi");
        return sb.toString();
    }

    public String getJsapi(){

        if(System.currentTimeMillis() - lasttime < 1*60*60*1000 && jsapi_ticket!=null){
            return jsapi_ticket;
        }

        try{
            //初始获取token
            if(this.access_token==null){
                synchronized (WxInfo.class) {
                    if(this.access_token==null){
                        this.getToken();
                    }
                }
            }
            String response =  HttpsClientUtil.getClient().doGet(getJsapiPath(this.access_token));
            if(response!=null){
                JSONObject json= JSONObject.parseObject(response);
                int errcode = json.getIntValue("errcode");
                if(0 == errcode){
                    jsapi_ticket = json.getString("ticket");
                }else if(40001 == errcode || 42001 == errcode){ // access_token 过期，重新获取
                    this.access_token=null;
                    synchronized (WxInfo.class) {
                        if(this.access_token==null){
                            this.getToken();
                        }
                    }
                    response =  HttpsClientUtil.getClient().doGet(getJsapiPath(this.access_token));
                    json= JSONObject.parseObject(response);
                    errcode = json.getIntValue("errcode");
                    if(0 == errcode){
                        jsapi_ticket = json.getString("ticket");
                    }else{
                        jsapi_ticket = null;
                    }
                }else{
                    jsapi_ticket = null;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        lasttime = System.currentTimeMillis();
        return jsapi_ticket;
    }

    /**
     * 生成签名
     * @param
     * @return
     */
    public String signature(Map<String, String> params){

        StringBuffer sb = new StringBuffer("");
        Set<Map.Entry<String, String>> es = params.entrySet();
        Iterator<Map.Entry<String, String>> it = es.iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String k = entry.getKey();
            String v = entry.getValue();
            sb.append(k).append("=").append(v).append("&");
        }

        String param = sb.substring(0, sb.lastIndexOf("&"));
        String appsign = WxUtil.getSha1(param);
        return appsign;
    }

    /**
     *  生成签名
     * @param parameters
     * @return
     */
    public static String createSign(Map<String, Object> parameters){
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();//所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if(null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + mch_key);
        return getMd5(sb.toString().toUpperCase());
    }

    private static String getMd5(String str)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @author wangkai
     * @date
     * @Description：返回给微信的参数
     * @param return_code
     *            返回编码
     * @param return_msg
     *            返回信息
     * @return
     */
    public static String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code
                + "]]></return_code><return_msg><![CDATA[" + return_msg
                + "]]></return_msg></xml>";
    }

}
