#import "FlutterBuglyPlugin.h"
#import <Bugly/Bugly.h>

@implementation FlutterBuglyPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"crazecoder/flutter_bugly"
            binaryMessenger:[registrar messenger]];
  FlutterBuglyPlugin* instance = [[FlutterBuglyPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"initBugly" isEqualToString:call.method]) {
      NSString *appId = call.arguments[@"appId"];
      BOOL b = [self isBlankString:appId];
      if(!b){
          BuglyConfig * config = [[BuglyConfig alloc] init];
          [Bugly startWithAppId:appId config:config];
          NSLog(@"Bugly appId: %@", appId);

          result(nil);
      }else{
          result(nil);
      }
      
  }else if([@"postCatchedException" isEqualToString:call.method]){
      NSString *crash_detail = call.arguments[@"crash_detail"];
      NSString *crash_message = call.arguments[@"crash_message"];
      if (crash_detail == nil || crash_detail == NULL) {
         crash_message = @"";
      }
      if ([crash_detail isKindOfClass:[NSNull class]]) {
          crash_message = @"";
      }
      NSArray *stackTraceArray = [crash_detail componentsSeparatedByString:@""];
      NSDictionary *data = call.arguments[@"crash_data"];
      if(data == nil){
        data = [NSMutableDictionary dictionary];
      }

      [Bugly reportExceptionWithCategory:5 name:crash_message reason:@" " callStack:stackTraceArray extraInfo:data terminateApp:NO];
      result(nil);
  }else {
      result(FlutterMethodNotImplemented);
  }
}
- (BOOL) isBlankString:(NSString *)string {
    if (string == nil || string == NULL) {
        return YES;
    }
    
    if ([string isKindOfClass:[NSNull class]]) {
        return YES;
    }
    if ([[string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]] length]==0) {
        return YES;
    }
    return NO;
    
}

@end
