# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this plugin is

`jpush_flutter_android` is an **Android-only Flutter federated plugin** that bundles the JPush (极光推送) Android vendor-channel push SDKs (Huawei, Xiaomi, Meizu, vivo, OPPO, Honor). It has no standalone API — it must be used together with the `jpush_flutter` package (the example app pins `jpush_flutter: ^3.5.8`; published compatibility is jpush_flutter 3.5.8+ for plugin 1.1.1 — full table in `README.md`).

**The plugin's real behavior is implemented in `android/build.gradle.kts`, not in Dart/Kotlin runtime code.** The Dart and Kotlin sources are essentially the stock `flutter create -t plugin` template (only `getPlatformVersion` on method channel `jPush_flutter_android`). The Gradle script does three things:

1. Reads the `jpush_android:` block from the **host app's** `pubspec.yaml` (the parent of the Gradle root project — for development, that block lives in `example/pubspec.yaml`) using SnakeYAML from the buildscript classpath.
2. Conditionally adds `cn.jiguang.sdk.plugin:{huawei,xiaomi,meizu,vivo,oppo,honor}:6.2.1` per `enable:` flag (OPPO additionally pulls gson 2.10.1 and androidx.annotation). When Huawei is enabled it also adds the Huawei Maven repo (`https://developer.huawei.com/repo/`) to both `buildscript` and `rootProject.allprojects`, puts agcp 1.9.1.301 on the buildscript classpath, and applies the `com.huawei.agconnect` plugin; the Honor Maven repo (`https://developer.hihonor.com/repo`, required since jiguang SDK v5.9.0) is added to both repository blocks only when Honor is enabled. This mirrors the per-channel setup in the official JPush Android vendor-channel guide.
3. Injects `manifestPlaceholders` into the host `:app` module's build variants (host needs zero configuration — see next two sections), resolving the jiguang aars' `${...}` manifest placeholders.

Prefixes are added automatically — users must not include them: `MZ-` for Meizu app_key/app_id; `OP-` for OPPO app_key/app_id/app_secret.

### How vendor parameters reach the manifest (important)

The jiguang aars declare meta-data whose values are placeholders, e.g. `<meta-data android:name="XIAOMI_APPKEY" android:value="${XIAOMI_APPKEY}"/>`. The script builds a `placeholdersToInject` map (prefixing Meizu values with `MZ-` and OPPO values with `OP-`) and, inside a `gradle.projectsEvaluated {}` hook, finds the `:app` project (matched by name/path and only when it applies `com.android.application`), iterates its **legacy** `android.applicationVariants`, and puts the map — plus `JPUSH_PKGNAME = variant.applicationId` — into each variant's live `manifestPlaceholders`. It tries, in order: `variant.manifestPlaceholders`, `variant.mergedFlavor.manifestPlaceholders`, each output's `manifestPlaceholders`, and finally falls back to `defaultConfig.manifestPlaceholders`. On AGP 8 only the `mergedFlavor` branch actually matches, but the fallbacks stay as a port of the original Groovy logic. The manifest merge tasks run after `projectsEvaluated`, so the late mutation is picked up and the merged manifest ends up with literal values; `:app` needs zero manifest configuration. This is a direct Kotlin port of the original Groovy `build.gradle` injection: helpers (`groovyRespondsTo`, `groovyHasProperty`, `groovyGetProperty`, `groovyPutAll`, `groovyAllAction`) emulate Groovy dynamic dispatch and use direct `getMethod("putAll")` / `getMethod("all")` reflection calls to avoid generic casts entirely — the script has zero `@Suppress("UNCHECKED_CAST")` annotations. The legacy variant classes are not on the script's compile classpath. Note `DomainObjectCollection.all(Action)` — through `groovyAllAction` — still needs an explicit `org.gradle.api.Action`; the earlier `all { }` alternative would resolve to Kotlin's `Iterable.all(predicate): Boolean`.

**Why the deprecated legacy Variant API is still mandatory (do not "modernize" this, and do not move the logic to a host-applied Gradle plugin — the project requires zero host-side configuration):** `example/android/build.gradle.kts` and the Flutter-generated templates (verified in Flutter 3.41.9's kotlin/java templates) contain `subprojects { project.evaluationDependsOn(":app") }`, so this library module's script body runs only after `:app` has fully evaluated. Every non-deprecated injection seam has been empirically verified to fail from that position: (a) registering `androidComponents.onVariants` late throws *"It is too late to add actions as the callbacks already executed"* — note the new `Variant.manifestPlaceholders: MapProperty<String,String>` API exists, the registration window is the problem, not the API surface; (b) writing the non-deprecated DSL `com.android.build.api.dsl.ApplicationExtension.defaultConfig.manifestPlaceholders` post-evaluation is silently ignored because AGP snapshots these into each variant's merged flavor at variant creation — the merge then fails with "no value for <X> is provided"; (c) writing the `ProcessApplicationManifest.manifestPlaceholders` task input fails with "property ... cannot be changed any further" (AGP calls `disallowChanges` during wiring). There is also no Flutter mechanism for a plugin subproject to auto-apply a Gradle plugin into `:app` (the settings-side `dev.flutter.flutter-plugin-loader` only `include`s plugin projects). The legacy `ApplicationVariant.mergedFlavor.manifestPlaceholders` plain mutable JDK map — read by the merge **task at execution time** — is the unique order-tolerant seam. Revisit only when AGP removes it or Flutter drops the `evaluationDependsOn` template.

Gotchas when touching this:

- Placeholder **keys** must match each aar's `${...}` exactly, and the key is not always the meta-data name: vivo declares meta-data `com.vivo.push.api_key` / `com.vivo.push.app_id` backed by **`${VIVO_APPKEY}` / `${VIVO_APPID}`**, and honor's `com.hihonor.push.app_id` is backed by **`${HONOR_APPID}`** — hence the map keys are `VIVO_APPKEY`/`VIVO_APPID`/`HONOR_APPID`. When in doubt, unzip the aar and read its `AndroidManifest.xml`.
- An entry is only added when its parameter is non-empty, and the aar itself is only on the classpath when the channel's `enable:` is true. Enabling a channel but leaving its parameters empty leaves the aar's `${...}` unresolved and fails the manifest merge — `enable: true` without credentials is only fine for Huawei, whose aar declares no `${...}` placeholders (HMS is configured via `agconnect-services.json`).
- `JPUSH_PKGNAME` is still injected per variant (set to the variant `applicationId`) even though no 6.x aar references it — SDK 6.x uses the built-in `${applicationId}`. It is harmless legacy parity, not a required value.
- The `com.huawei.agconnect` plugin (classpath agcp 1.9.1.301), the Huawei Maven repo, and the Honor Maven repo are each added only when their channel is `enable:`d — gating all three together is required because the buildscript repo resolves the agcp classpath artifact; enabling the repo but skipping the classpath (or vice versa) breaks configuration. When Huawei is disabled, no agconnect plugin is applied.
- The `jpush_android:` block is parsed twice, differently: the script body uses SnakeYAML (SnakeYAML coerces unquoted numeric app_id/app_key values to Int/Long — the `string()` helper calls `toString()` for that reason), while the `buildscript` block runs before that classpath is available and re-parses enable flags with a line-based regex scan (`channelEnabled`). Keep both in sync when changing the config schema.
- Verify with `aapt2 dump xmltree --file AndroidManifest.xml <apk>`: every vendor meta-data must carry a literal value and no `${...}` may remain (commented-out nodes in source manifests don't count).

## Commands

Toolchain: Flutter 3.41.9 / Dart 3.11.5, JDK 17, AGP 8.11.1, Gradle 8.14 wrapper, Kotlin 2.2.20, compileSdk 36, minSdk 24.

```bash
flutter pub get
flutter analyze
dart format .

# Dart tests (plugin root)
flutter test
flutter test test/jpush_flutter_android_test.dart   # single file

# Kotlin unit tests — the plugin module has no own Gradle wrapper; build through the example app
cd example/android && ./gradlew :jpush_flutter_android:testDebugUnitTest

# Verify the manifest pipeline and full packaging (needs the vendor aars to resolve)
cd example/android && ./gradlew :app:processDebugMainManifest
cd example/android && ./gradlew :app:assembleDebug
# inspect merged values: aapt2 dump xmltree --file AndroidManifest.xml <apk>

# Example app / integration tests (need an Android device or emulator)
cd example && flutter run
cd example && flutter test integration_test/plugin_integration_test.dart
```

## Conventions

- User-facing documentation (`README.md`) and release commit messages are written in Chinese; code comments in the Gradle script are Chinese as well. Match the surrounding language.
- Releases follow: bump `version:` in `pubspec.yaml`, add a `CHANGELOG.md` entry, update the compatibility table in `README.md`, then verify with `dart pub publish --dry-run`.
- The Android package is `org.leoli.plugin.jpush_flutter_android` (renamed from `org.leoli.plugin.jpush.flutter.android`, itself renamed from `com.jpush.flutter.android` in 1.1.0); it is declared in three places that must move together: `pubspec.yaml` (`flutter.plugin.platforms.android.package`), `android/build.gradle.kts` (`namespace`), and the Kotlin source/test directory paths.
- `pubspec.lock` is intentionally gitignored (library package).
- ProGuard keep rules ship through `consumerProguardFiles("proguard-rules.pro")`; the NIO (蔚来) rules in that file are legacy — that channel was removed in 1.0.1.