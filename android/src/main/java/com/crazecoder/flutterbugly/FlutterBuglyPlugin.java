package com.crazecoder.flutterbugly;

import android.app.Activity;
import android.text.TextUtils;

import com.crazecoder.flutterbugly.bean.BuglyInitResultInfo;
import com.crazecoder.flutterbugly.utils.JsonUtil;
import com.crazecoder.flutterbugly.utils.MapUtil;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterBuglyPlugin
 */
public class FlutterBuglyPlugin implements MethodCallHandler {
    private Activity activity;
    private Result result;
    private boolean isResultSubmitted = false;


    public FlutterBuglyPlugin(Activity activity) {
        this.activity = activity;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "crazecoder/flutter_bugly");
        FlutterBuglyPlugin plugin = new FlutterBuglyPlugin(registrar.activity());
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onMethodCall(final MethodCall call, final Result result) {
        isResultSubmitted = false;
        this.result = result;
        if (call.method.equals("initBugly")) {
            if (call.hasArgument("appId")) {
                String appId = call.argument("appId").toString();
                Bugly.init(activity.getApplicationContext(), appId, BuildConfig.DEBUG);
                result(getResultBean(true, appId, "Bugly 初始化成功"));
            } else {
                result(getResultBean(false, null, "Bugly appId不能为空"));
            }
        } else if (call.method.equals("postCatchedException")) {
            postException(call);
            result(null);
        } else {
            result.notImplemented();
            isResultSubmitted = true;
        }

    }

    private void postException(MethodCall call) {
        String message = "";
        String detail = null;
        Map<String, String> map = null;
        if (call.hasArgument("crash_message")) {
            message = call.argument("crash_message");
        }
        if (call.hasArgument("crash_detail")) {
            detail = call.argument("crash_detail");
        }
        if (TextUtils.isEmpty(detail)) return;
        if (call.hasArgument("crash_data")) {
            map = call.argument("crash_data");
        }
        CrashReport.postException(8, "Flutter Exception", message, detail, map);

//        String[] details = detail.split("#");
//        List<StackTraceElement> elements = new ArrayList<>();
//        for (String s : details) {
//            if (!TextUtils.isEmpty(s)) {
//                String methodName = null;
//                String fileName = null;
//                int lineNum = -1;
//                String[] contents = s.split(" \\(");
//                if (contents.length > 0) {
//                    methodName = contents[0];
//                    if (contents.length < 2) {
//                        break;
//                    }
//                    String packageContent = contents[1].replace(")", "");
//                    String[] packageContentArray = packageContent.split("\\.dart:");
//                    if (packageContentArray.length > 0) {
//                        if (packageContentArray.length == 1) {
//                            fileName = packageContentArray[0];
//                        } else {
//                            fileName = packageContentArray[0] + ".dart";
//                            Pattern patternTrace = Pattern.compile("[1-9]\\d*");
//                            Matcher m = patternTrace.matcher(packageContentArray[1]);
//                            if (m.find()) {
//                                String lineNumStr = m.group();
//                                lineNum = Integer.parseInt(lineNumStr);
//                            }
//                        }
//                    }
//                }
//                StackTraceElement element = new StackTraceElement("Dart", methodName, fileName, lineNum);
//                elements.add(element);
//            }
//        }
//        Throwable throwable = new Throwable(message);
//        if (elements.size() > 0) {
//            StackTraceElement[] elementsArray = new StackTraceElement[elements.size()];
//            throwable.setStackTrace(elements.toArray(elementsArray));
//        }
//        CrashReport.postCatchedException(throwable);
    }

    private void result(Object object) {
        if (result != null && !isResultSubmitted) {
            if (object == null) {
                result.success(null);
            } else {
                result.success(JsonUtil.toJson(MapUtil.deepToMap(object)));
            }
            isResultSubmitted = true;
        }
    }

    private BuglyInitResultInfo getResultBean(boolean isSuccess, String appId, String msg) {
        BuglyInitResultInfo bean = new BuglyInitResultInfo();
        bean.setSuccess(isSuccess);
        bean.setAppId(appId);
        bean.setMessage(msg);
        return bean;
    }
}