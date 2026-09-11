import 'package:flutter_test/flutter_test.dart';
import 'package:jpush_flutter_android/jpush_flutter_android.dart';
import 'package:jpush_flutter_android/jpush_flutter_android_platform_interface.dart';
import 'package:jpush_flutter_android/jpush_flutter_android_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockJPushFlutterAndroidPlatform with MockPlatformInterfaceMixin implements JPushFlutterAndroidPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final JPushFlutterAndroidPlatform initialPlatform = JPushFlutterAndroidPlatform.instance;

  test('$MethodChannelJPushFlutterAndroid is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelJPushFlutterAndroid>());
  });

  test('getPlatformVersion', () async {
    JPushFlutterAndroid jpushFlutterAndroidPlugin = JPushFlutterAndroid();
    MockJPushFlutterAndroidPlatform fakePlatform = MockJPushFlutterAndroidPlatform();
    JPushFlutterAndroidPlatform.instance = fakePlatform;

    expect(await jpushFlutterAndroidPlugin.getPlatformVersion(), '42');
  });
}
