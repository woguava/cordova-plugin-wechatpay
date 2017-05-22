package __PACKAGE_NAME__;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.openunion.cordova.plugins.wechat.WeChat;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by wangxg on 2017/4/28.
 */

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String LOG_TAG = "OU.WXPayEntryActivity";
    private IWXAPI wxApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      //必须进行注册
      wxApi = WXAPIFactory.createWXAPI(this, WeChat.g_Instance.getWxAppId());
      wxApi.handleIntent(getIntent(), this);
      Log.d(LOG_TAG, "wxApi.handleIntent");
    }

    @Override
    protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      setIntent(intent);
      wxApi.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
      finish();
    }

    @Override
    public void onResp(BaseResp resp) {
      if (WeChat.g_Instance.getCurrentCallbackContext() == null) {
        Log.d(LOG_TAG, "CallbackContext 无效");
        startMainActivity();
        return ;
      }

      try {
        JSONObject resultJson = new JSONObject();

        resultJson.put("errCode",""+resp.errCode);
        resultJson.put("errStr",resp.errStr);
        resultJson.put("transaction",resp.transaction);
        resultJson.put("openId",resp.openId);

        switch (resp.getType()){
          case ConstantsAPI.COMMAND_PAY_BY_WX: {
              PayResp wxPayResp = (PayResp) resp;
              resultJson.put("prepayId", wxPayResp.prepayId);
              resultJson.put("extData", wxPayResp.extData);
              resultJson.put("returnKey", wxPayResp.returnKey);
            }
            break;
        }
        Log.d(LOG_TAG,"wechat return ::" + resultJson.toString());
        WeChat.g_Instance.getCurrentCallbackContext().success(resultJson.toString());
      } catch (JSONException e) {
        WeChat.g_Instance.getCurrentCallbackContext().error(e.getMessage());
      }

      finish();
    }

    protected void startMainActivity() {
      Intent intent = new Intent();
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setPackage(getApplicationContext().getPackageName());
      getApplicationContext().startActivity(intent);
    }
}
