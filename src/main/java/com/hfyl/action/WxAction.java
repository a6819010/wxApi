package com.hfyl.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hfyl.util.WxInfo;
import com.hfyl.util.WxUtil;
import com.hfyl.util.YouguuHttpsClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by xyj on 2016/12/8.
 */
@Path(value = "/wx")
public class WxAction {

    /**
     * 请求来源校验
     * @param signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @GET
    @Path(value = "/verify")
    @Produces("text/html;charset=UTF-8")
    public String verify(@QueryParam("signature")String signature,
                         @QueryParam("timestamp")String timestamp,
                         @QueryParam("nonce")String nonce,
                         @QueryParam("echostr")String echostr)
    {
        return echostr;
    }

    /**
     *  获取H5微信朋友圈分享相关签名配置
     * @param url
     * @return
     */
    @GET
    @Path(value = "/getSignatureInfo")
    @Produces("text/html;charset=UTF-8")
    public String getSignatureInfo(@QueryParam("url")String url)
    {
        JSONObject jsonData = new JSONObject();

        WxInfo wxInfo = WxInfo.getCacheWxInfo();

        //生成签名的时间戳
        String timestamp = String.valueOf(System.currentTimeMillis());

        //生成签名的随机串
        String noncestr = WxUtil.getnoncestr();

        //获取jsapi_ticket票据
        String jsapi_ticket = wxInfo.getJsapi();

        //生成签名
        Map<String, String> params = new TreeMap<>();
        params.put("noncestr", noncestr);
        params.put("timestamp", timestamp);
        params.put("url", url);
        params.put("jsapi_ticket", jsapi_ticket);
        String signature = wxInfo.signature(params);

        jsonData.put("url", url);
        jsonData.put("timestamp", timestamp);
        jsonData.put("noncestr", noncestr);
        jsonData.put("status", "0000");
        jsonData.put("signature", signature);
        jsonData.put("jsapi_ticket", jsapi_ticket);
        jsonData.put("token", wxInfo.access_token);
        jsonData.put("appid", wxInfo.getAppId());
        return jsonData.toJSONString();
    }

    @GET
    @Path(value = "/createMenu")
    @Produces("text/html;charset=UTF-8")
    public String createMenu(@QueryParam("type")String type,
                                            @QueryParam("name")String name,
                                            @QueryParam("url")String url)
    {
        WxInfo wx = new WxInfo();
        String apiUrl = wx.getCareateMenuUrl();
        JSONObject obj = new JSONObject();
        JSONArray ja = new JSONArray();
        JSONObject ben = new JSONObject();
        ben.put("type",type);
        ben.put("name",name);
        ben.put("url",url);
        ja.add(ben);
        obj.put("button",ja);
        return YouguuHttpsClient.getClient().doPost(apiUrl,obj.toJSONString());
    }

    public static void main(String [] args)
    {
        WxInfo wx = new WxInfo();
        String apiUrl = wx.getCareateMenuUrl();
        JSONObject obj = new JSONObject();
        JSONArray ja = new JSONArray();

        JSONObject button1 = new JSONObject();
        button1.put("name","咨询");
        JSONArray subJa = new JSONArray();
        JSONObject subButton = new JSONObject();
        subButton.put("name","快速问诊");
        subButton.put("url","http://xfcheck.com/wenzhen1.html");
        subButton.put("type","view");
        subJa.add(subButton);

        subButton = new JSONObject();
        subButton.put("name","自助测评");
        subButton.put("url","http://xfcheck.com/ceping.html");
        subButton.put("type","view");
        subJa.add(subButton);

        button1.put("sub_button",subJa);
        ja.add(button1);

        JSONObject button2 = new JSONObject();
        button2.put("name","检测");
        subJa = new JSONArray();
        subButton = new JSONObject();
        subButton.put("name","STD检测");
        subButton.put("url","http://xfcheck.com/index.html");
        subButton.put("type","view");
        subJa.add(subButton);

        subButton = new JSONObject();
        subButton.put("name","不孕不育检测");
        subButton.put("url","http://xfcheck.com/index.html");
        subButton.put("type","view");
        subJa.add(subButton);

        subButton = new JSONObject();
        subButton.put("name","优生优育检测");
        subButton.put("url","http://xfcheck.com/index.html");
        subButton.put("type","view");
        subJa.add(subButton);

        button2.put("sub_button",subJa);
        ja.add(button2);

        JSONObject button3 = new JSONObject();
        button3.put("name","我的");
        subJa = new JSONArray();
        subButton = new JSONObject();
        subButton.put("name","个人中心");
        subButton.put("url","http://xfcheck.com/member-after.html");
        subButton.put("type","view");
        subJa.add(subButton);

        subButton = new JSONObject();
        subButton.put("name","性服知识");
        subButton.put("url","http://xfcheck.com/st-list.html");
        subButton.put("type","view");
        subJa.add(subButton);

        subButton = new JSONObject();
        subButton.put("name","下载APP");
        subButton.put("url","http://xfcheck.com/index.html");
        subButton.put("type","view");
        subJa.add(subButton);

        button3.put("sub_button",subJa);
        ja.add(button3);

        obj.put("button",ja);
        System.out.println(YouguuHttpsClient.getClient().doPost(apiUrl, obj.toJSONString()));
    }

}
