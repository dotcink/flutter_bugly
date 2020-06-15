package com.crazecoder.flutterbugly;

import android.app.Activity;
import android.text.TextUtils;

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
        if (call.method.equals("initBugly")) {
            if (call.hasArgument("appId")) {
                String appId = call.argument("appId").toString();
                Bugly.init(activity.getApplicationContext(), appId, BuildConfig.DEBUG);
                result.success(null);
            } else {
                result.success(null);
            }
        } else if (call.method.equals("postCatchedException")) {
            postException(call);
            result.success(null);
        } else {
            result.notImplemented();
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
    }

}