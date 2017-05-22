package com.openunion.cordova.plugins.wechat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * This class echoes a string called from JavaScript.
 */
public class WeChat extends CordovaPlugin {
    private final static String LOG_TAG = "OU.WeChat";
    private final static String APP_ID = "WECHAT_APPID";

    protected CallbackContext g_CurrentCallbackContext;
    protected static String g_AppID = "";
    protected static IWXAPI g_wxAPI;
    public static WeChat g_Instance = null;

    @Override
    public void pluginInitialize() {
        super.pluginInitialize();
        g_Instance = this;
        initWXAPI();
        Log.d(LOG_TAG, "plugin initialized.");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
      Log.d(LOG_TAG, "Execute:" + action + " with :" + args.toString());
      g_CurrentCallbackContext = callbackContext;

      if (action.equals("starPay")) {
          this.starPay(args,callbackContext);
      }else if(action.equals("isWXAppInstalled")) {
          this.isInstalled(callbackContext);
      }else{
          return false;
      }
      return true;
    }

    protected void isInstalled(CallbackContext callbackContext) {
        final IWXAPI wxApi = getWXAPI();

         if (!wxApi.isWXAppInstalled()) {
            callbackContext.success("0");
        } else {
            callbackContext.success("1");
        }
    }

    protected void starPay(JSONArray args, CallbackContext callbackContext){
      final IWXAPI wxApi = getWXAPI();
      //判断微信是否安装
      if (!wxApi.isWXAppInstalled()) {
        Log.d(LOG_TAG, "Wechat is not installed");
        sendErrorResultPluginResult(callbackContext,"未安装微信!");
        return;
      }

      //准备支付参数
      try{
          JSONObject payParams = null;
          if (args.get(0) instanceof JSONObject){
            payParams = args.getJSONObject(0);
          }else{
            callbackContext.error("starPay parameter error:" + args.get(0));
            return;
          }

          PayReq wxPayReq = new PayReq();
          wxPayReq.appId = payParams.getString("appid");
          wxPayReq.partnerId = payParams.getString("partnerid");
          wxPayReq.prepayId = payParams.getString("prepayid");
          wxPayReq.nonceStr = payParams.getString("noncestr");
          wxPayReq.timeStamp = payParams.getString("timestamp");
          wxPayReq.packageValue = payParams.getString("package");
          wxPayReq.sign = payParams.getString("sign");

          if(!wxPayReq.checkArgs() || !wxPayReq.appId.equals(g_AppID)){
              Log.e(LOG_TAG,"parameter error: req AppId[" +wxPayReq.appId + "] config AppId[" + g_AppID + "]");
              sendErrorResultPluginResult(callbackContext,"注册的APPID与设置的不匹配!");
              return;
          }

          //发送请求
          if (wxApi.sendReq(wxPayReq)) {
            Log.d(LOG_TAG, "支付请求发送成功.");
            sendNoResultPluginResult(callbackContext);
          } else {
            Log.d(LOG_TAG, "支付请求发送失败.");
            // send error
            sendErrorResultPluginResult(callbackContext,"支付请求发送失败!");
          }
      } catch (Exception e) {
          Log.e(LOG_TAG, "parameter: " + args.toString() + "Exception: " + e.getMessage());
          sendErrorResultPluginResult(callbackContext,"参数格式错误!");
          return;
      }

    }

    private void initWXAPI() {
      g_AppID = preferences.getString(APP_ID, "");
      Log.i(LOG_TAG,"get config appid : " + g_AppID);
      // 将该app注册到微信
      g_wxAPI = WXAPIFactory.createWXAPI(webView.getContext(), g_AppID, true);
      if(!g_wxAPI.registerApp(g_AppID)){
        Log.e(LOG_TAG,"registerApp error!" + g_AppID);
      }
    }

    private IWXAPI getWXAPI(){
      return g_wxAPI;
    }

    public String getWxAppId(){
      return g_AppID;
    }

    public CallbackContext getCurrentCallbackContext() {
      return g_CurrentCallbackContext;
    }

    //无返回结果
    private void sendNoResultPluginResult(CallbackContext callbackContext) {
      PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
      result.setKeepCallback(true);
      callbackContext.sendPluginResult(result);
    }

    //返回错误信息
    private void sendErrorResultPluginResult(CallbackContext callbackContext,String errStr) {
      try {
        JSONObject resultJson = new JSONObject();
        resultJson.put("errCode","-1");
        resultJson.put("errStr",errStr);
        callbackContext.error(resultJson.toString());
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

}
