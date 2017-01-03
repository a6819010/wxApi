package com.hfyl.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hfyl.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.hfyl.util.HttpsClientUtil.getClient;
import static com.hfyl.util.WxInfo.createSign;

/**
 * Created by xyj on 2016/12/8.
 */
@Path(value = "/wx")
public class WxAction {

    private static Log log = LogFactory.getLog(WxAction.class);

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

        //获取jsapi_ticket票据
        String jsapi_ticket = wxInfo.getJsapi();

        //生成签名的随机串
        String noncestr = WxUtil.getnoncestr();

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

    /**
     *  创建菜单
     * @param type
     * @param name
     * @param url
     * @return
     */
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
        return getClient().doPost(apiUrl,obj.toJSONString());
    }

    /**
     * 下单接口
     * @param ip
     * @param code
     * @return
     */
    @POST
    @Path(value = "/createOrder")
    @Produces("text/html;charset=UTF-8")
    public String createOrder(
            @FormParam("orderId")String orderId,
            @FormParam("ip")String ip,
            @FormParam("code")String code)
    {
        //TODO  解析productData，将里面的商品从新做总价计算，如果与totalFee不一致，则返回错误

        //TODO  创建新的订单

        //TODO 请求微信统一下单接口

        try {
            JSONObject jsonData = new JSONObject();

            //初始化微信信息
            WxInfo wxInfo = WxInfo.getCacheWxInfo();

            //获取下单接口提交的参数 TODO 订单总价格和我们生成的订单号需要替换下
            String dataXml = getOrderXml(wxInfo,10,"ABCD1234",ip,code);
            log.info("dataXml："+dataXml);

            //调用微信下单接口
            Response<String> res =  HttpsClientUtil.getClient().sendPostXml(WxInfo.ORDER_URL,"utf-8",dataXml);
            log.info("下单返回res："+res);

            //将微信回参转为Map结构
            Map<String, Object> resDataMap = Util.converterMap(res.getT());

            //签名验证
            boolean flag = checkSign(resDataMap);
            if(!flag)
            {
                return "签名验证失败，支付异常";
            }

            //TODO 数据库操作，修改订单信息，添加微信支付订单号transaction_id

            //获取交易会话ID，返回给H5//TODO  这里要处理
            String prepayId = getPrepayId(resDataMap);

            log.info("交易会话ID："+prepayId);
            return resJson("0000","ok",getDataInfo(prepayId));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        }
        return "";
    }

    private String  resJson(String status,String message,JSONObject obj)
    {
        JSONObject res = new JSONObject();
        res.put("status",status);
        res.put("message",message);
        if(obj != null)
        {
            res.put("result",obj);
        }
        return res.toJSONString();
    }

    private JSONObject getDataInfo(String prepay_id)
    {
        JSONObject res = new JSONObject();
        try {
            //生成签名的时间戳
            String timestamp = String.valueOf(System.currentTimeMillis());
            //生成签名的随机串
            String noncestr = WxUtil.getnoncestr();
            //签名方式
            String signType = "MD5";
            String packages = "prepay_id="+prepay_id;

            Map<String, Object> map = new HashMap<>();
            map.put("appId","wx4f81fa7f2a9ee51e");
            map.put("timeStamp",timestamp);
            map.put("nonceStr",noncestr);
            map.put("package",packages);
            map.put("signType",signType);

            //生成签名
            String sign = WxInfo.createSign(map);
            res.put("appId","wx4f81fa7f2a9ee51e");
            res.put("timeStamp",timestamp);
            res.put("nonceStr",noncestr);
            res.put("package",packages);
            res.put("signType",signType);
            res.put("paySign",sign);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        }
        return res;
    }

    /**
     *  微信异步通知接口
     * @param request
     * @return
     */
    @POST
    @Path(value = "/wxNotification")
    @Produces("text/html;charset=UTF-8")
    public String wxNotification(@Context HttpServletRequest request)
    {
        try {
            //解析回调参数
            String resStr = Util.parseWeixinCallback(request);

            //将微信回参转为Map结构
            Map<String, Object> resDataMap = Util.converterMap(resStr);

            //签名验证
            boolean flag = checkSign(resDataMap);
            if(!flag)
            {
                return "签名验证失败，支付异常";
            }

            //TODO 数据库操作，修改订单的状态


            return WxInfo.setXML(WxInfo.SUCCESS,"");
        } catch (IOException e) {
            log.error("wxNotification："+e);
        }
        return WxInfo.setXML(WxInfo.FAIL,"错误");
    }

    /**
     *  校验签名
     * @param resMap
     * @return
     */
    private boolean checkSign(Map<String, Object> resMap)
    {
        String retCode = String.valueOf(resMap.get("return_code"));
        if(!"SUCCESS".equals(retCode))
        {
            String resMsg = String.valueOf(resMap.get("return_msg"));
            log.error("disResOrder："+resMsg);
            return false;
        }
        //校验签名
        String wxSign = String.valueOf(resMap.get("sign"));
        //本地签名从新计算
        String sign = createSign(resMap);

        //判断签名是否一致
        if(!wxSign.equals(sign))
        {
            log.error("签名不一致，支付异常");
            return false;
        }
        return true;
    }

    /**
     * 校验签名，获取交易会话ID
     * @param resMap
     * @return
     */
    private String getPrepayId(Map<String, Object> resMap)
    {
        String resCode = String.valueOf(resMap.get("result_code"));

        if(!"SUCCESS".equals(resCode))
        {
            String resMsg = String.valueOf(resMap.get("err_code_des"));
            log.error("disResOrder："+resMsg);
            return resMsg;
        }

        //获取支付交易会话ID，有效期2个小时
        String prepay_id = String.valueOf(resMap.get("prepay_id"));
        return prepay_id;
    }

    /**
     *  组装订单xml
     * @param wxInfo
     * @param total_fee
     * @param out_trade_no
     * @param ip
     * @param code
     * @return
     */
    private String getOrderXml(WxInfo wxInfo,int total_fee,String out_trade_no,String ip,String code)
    {
        //获取APPID
        String appid = wxInfo.getAppId();

        //获取商户号
        String mch_id = wxInfo.getMchId();

        //生成签名的随机串
        String nonce_str = WxUtil.getnoncestr();

        //拼装到Map中
        Map<String,Object> map = new HashMap<>();
        map.put("appid",appid);
        map.put("mch_id",mch_id);
        map.put("nonce_str",nonce_str);
        map.put("body",WxInfo.MCHNAME);//商品描述
        map.put("spbill_create_ip",ip);//终端IP
        map.put("notify_url","http://xfcheck.com/wxApi/wx/wxNotification");//通知地址
        map.put("trade_type","JSAPI");//交易类型
        map.put("openid",wxInfo.getOpenId(code));//openid
        map.put("out_trade_no",out_trade_no);
        map.put("total_fee",total_fee);

        //生成签名
        String sign = WxInfo.createSign(map);
        map.put("sign",sign);
        log.info("sign："+sign);

        String xml = Util.converterXml(map,"xml");
        return xml;
    }


    /**
     * 直接运行创建菜单
     */
    public static void addMenu()
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
        System.out.println(getClient().doPost(apiUrl, obj.toJSONString()));
    }

    public static void main(String [] args)
    {
        WxAction wa = new WxAction();
        wa.createOrder("ABCD1234","123.114.130.131","001SYTKl0q73sn1HIvKl0UsPKl0SYTKl");
    }

}
