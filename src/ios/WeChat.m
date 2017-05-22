/********* wechat.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "WXApi.h"
#import "WXApiObject.h"

@interface WeChat : CDVPlugin <WXApiDelegate>{
  // Member variables go here.
  NSString * g_appId;
  NSString * g_currentCallbackId;
}

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command;
- (void)starPay:(CDVInvokedUrlCommand*)command;
@end

@implementation WeChat

#pragma mark "API"
- (void)pluginInitialize {
    g_appId = [[self.commandDelegate settings] objectForKey:@"wechat_appid"];
    
    if (g_appId){
        [WXApi registerApp: g_appId];
    }
    
    NSLog(@"initialized. Wechat SDK Version: %@. APP_ID: %@.", [WXApi getApiVersion], g_appId);
}

- (void)isWXAppInstalled:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[WXApi isWXAppInstalled]];
    
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

- (void)starPay:(CDVInvokedUrlCommand*)command
{
    if(![WXApi isWXAppInstalled])
    {
        NSString *resultString  = [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",-1,@"微信未安装！"];
        [self failWithCallbackID:command.callbackId withMessage:resultString];
        return ;
    }
    
    // check arguments
    NSDictionary *params = [command.arguments objectAtIndex:0];
    if (!params)
    {
        NSString *resultString  = [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",-1,@"参数格式错误!"];
        [self failWithCallbackID:command.callbackId withMessage:resultString];
        return ;
    }
    
    // check required parameters
    NSArray *requiredParams = @[@"partnerid", @"prepayid", @"timestamp", @"noncestr",@"package", @"sign"];
    for (NSString *key in requiredParams)
    {
        if (![params objectForKey:key])
        {
            NSString *resultString  = [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",-1,@"参数格式错误!"];
            [self failWithCallbackID:command.callbackId withMessage:resultString];
            return ;
        }
    }
    
    PayReq *wxReq = [[PayReq alloc] init];
    wxReq.partnerId = [params objectForKey:requiredParams[0]];
    wxReq.prepayId = [params objectForKey:requiredParams[1]];
    wxReq.timeStamp = [[params objectForKey:requiredParams[2]] intValue];
    wxReq.nonceStr = [params objectForKey:requiredParams[3]];
    wxReq.package = [params objectForKey:requiredParams[4]];
    wxReq.sign = [params objectForKey:requiredParams[5]];
    
    if ([WXApi sendReq:wxReq])
    {
        // save the callback id
        g_currentCallbackId = command.callbackId;
    }
    else
    {
        NSString *resultString  = [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",-1,@"发送请求失败!"];
        [self failWithCallbackID:command.callbackId withMessage:resultString];
    }
}


#pragma mark "WXApiDelegate"
/**
 * Not implemented
 */
- (void)onReq:(BaseReq *)req
{
    NSLog(@"%@", req);
}



- (void)onResp:(BaseResp *)resp
{
    if ([resp isKindOfClass:[PayResp class]]) {
        //支付返回结果，实际支付结果需要去微信服务器端查询
        NSString *resultString;
        switch(resp.errCode){
            case 0:
                resultString= [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",resp.errCode,@"支付成功"];
                break;
            case -2:
                resultString= [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",resp.errCode,@"用户取消"];
                break;
            default:
                resultString= [NSString stringWithFormat:@"{\"errCode\":%d,\"errStr\":\"%@\"}",resp.errCode,@"支付失败"];
                break;
        }
        
        NSLog(@"%@", resultString);
        [self successWithCallbackID:g_currentCallbackId withMessage:resultString];
    }
    
    g_currentCallbackId = nil;
}

#pragma mark "CDVPlugin Overrides"

- (void)handleOpenURL:(NSNotification *)notification
{
    NSURL* url = [notification object];
    
    if ([url isKindOfClass:[NSURL class]] && [url.scheme isEqualToString:g_appId])
    {
        [WXApi handleOpenURL:url delegate:self];
    }
}


#pragma mark "Private methods"
- (void)successWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

- (void)failWithCallbackID:(NSString *)callbackID withMessage:(NSString *)message
{
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:message];
    [self.commandDelegate sendPluginResult:commandResult callbackId:callbackID];
}

@end
