import 'dart:async';
import 'dart:io';
import 'dart:isolate';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'dart:convert';

class FlutterBugly {
  FlutterBugly._();

  static const MethodChannel _channel =
      const MethodChannel('crazecoder/flutter_bugly');

  ///初始化
  static Future init({
    String androidAppId,
    String iOSAppId,
  }) async {
    assert((Platform.isAndroid && androidAppId != null) ||
        (Platform.isIOS && iOSAppId != null));
    Map<String, Object> map = {
      "appId": Platform.isAndroid ? androidAppId : iOSAppId,
    };
    await _channel.invokeMethod('initBugly', map);
  }

  ///异常上报
  static void postCatchedException<T>(
    T callback(), {
    FlutterExceptionHandler handler, //异常捕捉，用于自定义打印异常
    String filterRegExp, //异常上报过滤正则，针对message
    bool debugUpload = false,
  }) {
    bool _isDebug = false;
    assert(_isDebug = true);
    // This captures errors reported by the Flutter framework.
    FlutterError.onError = (details) {
      Zone.current.handleUncaughtError(details.exception, details.stack);
    };
    Isolate.current.addErrorListener(new RawReceivePort((dynamic pair) {
      var isolateError = pair as List<dynamic>;
      var _error = isolateError.first;
      var _stackTrace = isolateError.last;
      Zone.current.handleUncaughtError(_error, _stackTrace);
    }).sendPort);
    // This creates a [Zone] that contains the Flutter application and stablishes
    // an error handler that captures errors and reports them.
    //
    // Using a zone makes sure that as many errors as possible are captured,
    // including those thrown from [Timer]s, microtasks, I/O, and those forwarded
    // from the `FlutterError` handler.
    //
    // More about zones:
    //
    // - https://api.dartlang.org/stable/1.24.2/dart-async/Zone-class.html
    // - https://www.dartlang.org/articles/libraries/zones
    runZoned<Future<Null>>(() async {
      callback();
    }, onError: (error, stackTrace) {
      _filterAndUploadException(
        debugUpload,
        _isDebug,
        handler,
        filterRegExp,
        FlutterErrorDetails(exception: error, stack: stackTrace),
      );
    });
  }

  static void _filterAndUploadException(
    debugUpload,
    _isDebug,
    handler,
    filterRegExp,
    FlutterErrorDetails details,
  ) {
    if (!_filterException(
      debugUpload,
      _isDebug,
      handler,
      filterRegExp,
      details,
    )) {
      uploadException(
          message: details.exception.toString(),
          detail: details.stack.toString());
    }
  }

  static bool _filterException(
      bool debugUpload,
      bool _isDebug,
      FlutterExceptionHandler handler,
      String filterRegExp,
      FlutterErrorDetails details) {
    //默认debug下打印异常，不上传异常
    if (!debugUpload && _isDebug) {
      handler == null
          ? FlutterError.dumpErrorToConsole(details)
          : handler(details);
      return true;
    }
    //异常过滤
    if (filterRegExp != null) {
      RegExp reg = new RegExp(filterRegExp);
      Iterable<Match> matches = reg.allMatches(details.exception.toString());
      if (matches.length > 0) {
        return true;
      }
    }
    return false;
  }

  ///上报自定义异常信息，data为文本附件
  ///Android 错误分析=>跟踪数据=>extraMessage.txt
  ///iOS 错误分析=>跟踪数据=>crash_attach.log
  static Future<Null> uploadException(
      {@required String message, @required String detail, Map data}) async {
    var map = {};
    map.putIfAbsent("crash_message", () => message);
    map.putIfAbsent("crash_detail", () => detail);
    if (data != null) map.putIfAbsent("crash_data", () => data);
    await _channel.invokeMethod('postCatchedException', map);
  }
}
