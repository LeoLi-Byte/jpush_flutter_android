## 1.1.3

* Added a `.pubignore` file to exclude `CLAUDE.md` and the `.claude/` directory from the pub.dev package.
* Added a GitHub Actions workflow for automated releases, triggered by tag pushes, which extracts the changelog entry and creates a GitHub Release.

## 1.1.2

* Renamed the Android package from `org.leoli.plugin.jpush.flutter.android` to `org.leoli.plugin.jpush_flutter_android`.

## 1.1.1

* Added support for jpush_flutter 3.5.8.
* Updated JPush Android SDK to version 6.2.1.

## 1.1.0

* Added support for jpush_flutter 3.5.7.
* Migrated the Android build scripts from Groovy to Kotlin DSL (`build.gradle.kts`).
* Renamed the Android package from `com.jpush.flutter.android` to `org.leoli.plugin.jpush.flutter.android`.
* The Huawei AGConnect plugin and the Huawei/Honor Maven repositories are now configured only when the corresponding vendor channel is enabled.

## 1.0.2

* Added support for jpush_flutter 3.4.8.
* Updated JPush Android SDK to version 6.2.0.

## 1.0.1

* Added support for jpush_flutter 3.4.5.
* Removed NIO platform support.

## 1.0.0

* Official release with support for parameter configuration.

## 0.0.11

* Improved score.

## 0.0.10

* Added support for jPush 3.4.3.

## 0.0.9

* Fixed an issue where files could not be downloaded via the Honor channel.

## 0.0.8

* Added support for jPush 3.3.8.

## 0.0.7

* Added support for jPush 3.3.2.

## 0.0.6

* Use a local Maven repository.

## 0.0.5

* Added support for jPush 3.2.4.

## 0.0.4

* Added support for jPush 3.1.9.

## 0.0.3

* Added support for jPush 3.1.8.
* Removed the requirement to add the "MI-" prefix for Xiaomi devices (as per Xiaomi's specifications).

## 0.0.1

* Initial release.