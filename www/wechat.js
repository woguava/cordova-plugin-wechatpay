var exec = require('cordova/exec');


module.exports = {
    /**
     * payment request
     * <code>
     * var orderInfo = {
     *       "appid":"wx8888888888888888", //微信开放平台审核通过的应用APPID
     *       "noncestr":"9F2AEF382D8048068FDAB0CC3F72F262", //随机字符串，不长于32位,必须和统一下单上送相同
     *       "package":"Sign=WXPay",    //暂填写固定值Sign=WXPay
     *       "partnerid":"1900000109",  //微信支付分配的商户号
     *       "prepayid":"wx20170503184430404125",   //微信返回的支付交易会话ID
     *       "timestamp":"1493808266",   //时间戳,必须和统一下单上送相同
     *       "sign":"49120EECF3D68E1F241FB43DB0F4C0F9" //签名
     *   };
     * cordova.plugins.WeChat.starPay(orderInfo,data => {
     *   let result = JSON.parse(data);
     *   let resultMsg = result.errCode;
     *   let rtnMsg = '';
     *   if(resultMsg == "0"){
     *       rtnMsg = '{"errCode":"0","errMsg":"支付成功"}';
     *   }else if(resultMsg == "-2"){
     *       rtnMsg = '{"errCode":"2","errMsg":"支付取消"}';
     *   }else{
     *       rtnMsg = '{"errCode":"1","errMsg":"支付失败"}';
     *   }
     *   resolve(JSON.parse(rtnMsg));
     *   }, error => {
     *      reject(error);
     *   });
     */
    starPay: function(arg0, success, error) {
        exec(success, error, "WeChat", "starPay", [arg0]);
    },

    /**
     * 判断微信是否安装 0 未安装 1 已安装
     * cordova.plugins.WeChat.isInstalled(data => {
     *     resolve(data);
     * });
     * 
     */
    isInstalled: function (onSuccess, onError) {
        exec(onSuccess, onError, "WeChat", "isWXAppInstalled", []);
    }
};

