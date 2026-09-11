import org.gradle.api.Action
import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import java.io.File

val flutterProjectDir: File = rootProject.projectDir.parentFile
val cfg: Map<String, Any?> =
    Yaml().load<Map<String, Any?>>(flutterProjectDir.resolve("pubspec.yaml").readText()).orEmpty()

// Kotlin DSL 为静态语言，以下函数等价于 Groovy 的 cfg.jpush_android?.huawei?.enable 式安全取值
fun Map<*, *>?.section(key: String): Map<*, *>? = this?.get(key) as? Map<*, *>

// 对齐 Groovy 的 `String x = value`：SnakeYAML 会把不带引号的数字（如小米/vivo/荣耀的
// app_id、app_key）解析成 Int/Long/BigInteger，Groovy 隐式转字符串，而 as? String 只会得到 null
fun Map<*, *>?.string(key: String): String = this?.get(key)?.toString().orEmpty()

fun Map<*, *>?.flag(key: String): Boolean = this?.get(key) as? Boolean ?: false

val jPushAndroid = cfg.section("jpush_android")

// JPush 信息
val jPushAppKey: String = jPushAndroid.string("app_key")
val jPushChannel: String = jPushAndroid.string("channel")

// JPush 厂商通道信息：每个厂商 section 只解析一次，enable 与各参数复用同一引用
val huawei = jPushAndroid.section("huawei")
val xiaomi = jPushAndroid.section("xiaomi")
val meizu = jPushAndroid.section("meizu")
val vivo = jPushAndroid.section("vivo")
val oppo = jPushAndroid.section("oppo")
val honor = jPushAndroid.section("honor")

val jPushHuaweiEnable: Boolean = huawei.flag("enable")
val jPushXiaomiEnable: Boolean = xiaomi.flag("enable")
val jPushXiaomiAppKey: String = xiaomi.string("app_key")
val jPushXiaomiAppId: String = xiaomi.string("app_id")
val jPushMeiZuEnable: Boolean = meizu.flag("enable")
val jPushMeiZuAppKey: String = meizu.string("app_key")
val jPushMeiZuAppId: String = meizu.string("app_id")
val jPushVivoEnable: Boolean = vivo.flag("enable")
val jPushVivoAppKey: String = vivo.string("app_key")
val jPushVivoAppId: String = vivo.string("app_id")
val jPushOppoEnable: Boolean = oppo.flag("enable")
val jPushOppoAppKey: String = oppo.string("app_key")
val jPushOppoAppId: String = oppo.string("app_id")
val jPushOppoAppSecret: String = oppo.string("app_secret")
val jPushHonorEnable: Boolean = honor.flag("enable")
val jPushHonorAppId: String = honor.string("app_id")

group = "org.leoli.plugin.jpush.flutter.android"
version = "1.0-SNAPSHOT"

buildscript {
    fun channelEnabled(pubspecText: String, channel: String): Boolean {
        val lines = pubspecText.lineSequence().iterator()
        val header = Regex("""^(\s*)${Regex.escape(channel)}\s*:\s*(?:#.*)?$""")
        var blockIndent = -1
        while (lines.hasNext()) {
            val line = lines.next()
            val match = header.matchEntire(line)
            if (match != null) {
                blockIndent = match.groupValues[1].length
                break
            }
        }
        if (blockIndent < 0) return true
        while (lines.hasNext()) {
            val line = lines.next()
            if (line.isBlank()) continue
            val indent = line.indexOfFirst { !it.isWhitespace() }.let { if (it < 0) 0 else it }
            if (indent <= blockIndent) break
            if (Regex("""^\s*enable\s*:""").containsMatchIn(line)) {
                return Regex("""^\s*enable\s*:\s*true\b""").containsMatchIn(line)
            }
        }
        return true
    }

    val pubspecText = rootProject.projectDir.parentFile!!.resolve("pubspec.yaml").readText()
    val huaweiEnabled = channelEnabled(pubspecText, "huawei")
    val honorEnabled = channelEnabled(pubspecText, "honor")

    val kotlinVersion = "2.2.20"
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        if (huaweiEnabled) maven { url = uri("https://developer.huawei.com/repo/") }
        if (honorEnabled) maven { url = uri("https://developer.hihonor.com/repo") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.11.1")
        classpath("org.yaml:snakeyaml:2.6")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        if (huaweiEnabled) classpath("com.huawei.agconnect:agcp:1.9.1.301")
    }
}

rootProject.allprojects {
    repositories {
        google()
        mavenCentral()
        if (jPushHuaweiEnable) maven { url = uri("https://developer.huawei.com/repo/") }
        if (jPushHonorEnable) maven { url = uri("https://developer.hihonor.com/repo") }
    }
}

// ==================== 需要注入的数据映射 ====================

val placeholdersToInject = linkedMapOf<String, String>()
if (jPushAppKey.isNotEmpty()) placeholdersToInject["JPUSH_APPKEY"] = jPushAppKey
if (jPushChannel.isNotEmpty()) placeholdersToInject["JPUSH_CHANNEL"] = jPushChannel
if (jPushXiaomiAppKey.isNotEmpty()) placeholdersToInject["XIAOMI_APPKEY"] = jPushXiaomiAppKey
if (jPushXiaomiAppId.isNotEmpty()) placeholdersToInject["XIAOMI_APPID"] = jPushXiaomiAppId
if (jPushMeiZuAppKey.isNotEmpty()) placeholdersToInject["MEIZU_APPKEY"] = "MZ-${jPushMeiZuAppKey}"
if (jPushMeiZuAppId.isNotEmpty()) placeholdersToInject["MEIZU_APPID"] = "MZ-${jPushMeiZuAppId}"
if (jPushVivoAppKey.isNotEmpty()) placeholdersToInject["VIVO_APPKEY"] = jPushVivoAppKey
if (jPushVivoAppId.isNotEmpty()) placeholdersToInject["VIVO_APPID"] = jPushVivoAppId
if (jPushOppoAppKey.isNotEmpty()) placeholdersToInject["OPPO_APPKEY"] = "OP-${jPushOppoAppKey}"
if (jPushOppoAppId.isNotEmpty()) placeholdersToInject["OPPO_APPID"] = "OP-${jPushOppoAppId}"
if (jPushOppoAppSecret.isNotEmpty()) placeholdersToInject["OPPO_APPSECRET"] =
    "OP-${jPushOppoAppSecret}"
if (jPushHonorAppId.isNotEmpty()) placeholdersToInject["HONOR_APPID"] = jPushHonorAppId

// ==================== 注入到应用模块的每个构建变体 ====================

// Kotlin DSL 为静态语言，而 AGP 的旧版变体相关类不放在本脚本的编译类路径上；
// 以下辅助函数等价于原 Groovy 脚本的动态分发
// （metaClass.respondsTo / hasProperty / 动态属性读取 + putAll / all 的反射调用），
// 从而源码中不直接引用任何已废弃的 AGP 类型，
// 也不需要 @Suppress("UNCHECKED_CAST")（通过反射代替泛型强制转换）。
fun Any?.groovyRespondsTo(methodName: String): Boolean {
    if (this == null) return false
    return this.javaClass.methods.any { it.name == methodName }
}

fun Any?.groovyHasProperty(propertyName: String): Boolean {
    if (this == null) return false
    val capitalized = propertyName.replaceFirstChar { it.uppercase() }
    if (this.javaClass.methods.any { it.parameterCount == 0 && (it.name == "get$capitalized" || it.name == "is$capitalized") }) {
        return true
    }
    var clazz: Class<*>? = this.javaClass
    while (clazz != null) {
        if (clazz.declaredFields.any { it.name == propertyName }) return true
        clazz = clazz.superclass
    }
    return false
}

fun Any?.groovyGetProperty(propertyName: String): Any? {
    if (this == null) return null
    if (this is Map<*, *> && this.containsKey(propertyName)) return this[propertyName]
    val capitalized = propertyName.replaceFirstChar { it.uppercase() }
    this.javaClass.methods.firstOrNull { it.parameterCount == 0 && it.name == "get$capitalized" }
        ?.let { return it.invoke(this) }
    this.javaClass.methods.firstOrNull { it.parameterCount == 0 && it.name == "is$capitalized" }
        ?.let { return it.invoke(this) }
    var clazz: Class<*>? = this.javaClass
    while (clazz != null) {
        val declaringClass: Class<*> = clazz!!
        declaringClass.declaredFields.firstOrNull { it.name == propertyName }?.let {
            it.isAccessible = true
            return it.get(this)
        }
        clazz = declaringClass.superclass
    }
    throw IllegalStateException("无法读取属性: $propertyName")
}

// 通过反射直接调用返回值的 Map.putAll(Map) 与 DomainObjectCollection.all(Action)，
// 避免从 Any? 到泛型类型的强制转换，调用方无需 @Suppress("UNCHECKED_CAST")。
fun Any?.groovyPutAll(propertyName: String, values: Map<*, *>) {
    groovyGetProperty(propertyName)?.let { target ->
        target.javaClass.getMethod("putAll", Map::class.java).invoke(target, values)
    }
}

fun Any?.groovyAllAction(propertyName: String, action: Action<*>) {
    groovyGetProperty(propertyName)?.let { target ->
        target.javaClass.getMethod("all", Action::class.java).invoke(target, action)
    }
}

/**
 * 为单个变体注入占位符（包括动态的 JPUSH_PKGNAME）
 * @param variant 应用变体（ApplicationVariant）
 * @param placeholders 基础占位符 Map（不含 JPUSH_PKGNAME）
 */
fun injectPlaceholdersForVariant(variant: Any, placeholders: Map<String, String>) {
    val combinedPlaceholders = LinkedHashMap<String, Any?>()
    combinedPlaceholders["JPUSH_PKGNAME"] = variant.groovyGetProperty("applicationId")?.toString()
    combinedPlaceholders.putAll(placeholders)

    var set = false

    // 尝试方法1: 直接 variant.manifestPlaceholders
    try {
        if (variant.groovyRespondsTo("getManifestPlaceholders") || variant.groovyHasProperty("manifestPlaceholders")) {
            variant.groovyPutAll("manifestPlaceholders", combinedPlaceholders)
            set = true
        }
    } catch (e: Exception) {
        println("JPush: 方法1失败 - ${e.message}")
    }

    // 尝试方法2: variant.mergedFlavor.manifestPlaceholders
    if (!set) {
        try {
            if (variant.groovyHasProperty("mergedFlavor") && variant.groovyGetProperty("mergedFlavor") != null) {
                variant.groovyGetProperty("mergedFlavor")!!
                    .groovyPutAll("manifestPlaceholders", combinedPlaceholders)
                set = true
            }
        } catch (e: Exception) {
            println("JPush: 方法2失败 - ${e.message}")
        }
    }

    // 尝试方法3: 遍历 outputs
    if (!set) {
        try {
            (variant.groovyGetProperty("outputs") as Iterable<*>).forEach { output ->
                if (output.groovyHasProperty("manifestPlaceholders") && output.groovyGetProperty("manifestPlaceholders") != null) {
                    output.groovyPutAll("manifestPlaceholders", combinedPlaceholders)
                    set = true
                }
            }
        } catch (e: Exception) {
            println("JPush: 方法3失败 - ${e.message}")
        }
    }

    if (!set) println("JPush: 警告 - 无法为变体 ${variant.groovyGetProperty("name")} 设置占位符，请检查 AGP 版本")
}

/**
 * 为指定的 Android 应用模块注入占位符到 manifestPlaceholders：
 * 优先通过反射遍历 applicationVariants（各变体的 mergedFlavor.manifestPlaceholders），
 * 不可用时回退到 defaultConfig.manifestPlaceholders。
 * @param module 应用模块（Project 对象）
 * @param placeholders 基础占位符 Map（不含 JPUSH_PKGNAME，调用方先 building 好传入）
 */
fun injectPlaceholdersForModule(module: Project, placeholders: Map<String, String>) {
    val androidExt = module.extensions.findByName("android")
    if (androidExt == null) {
        println("JPush: 模块 ${module.path} 没有 android 扩展，跳过")
        return
    }

    // 情况1：模块有 applicationVariants（较新 AGP）
    if (androidExt.groovyHasProperty("applicationVariants")) {
        androidExt.groovyAllAction(
            "applicationVariants",
            Action<Any> { injectPlaceholdersForVariant(this, placeholders) })
    }

    // 情况2：模块没有 applicationVariants（较老 AGP 或库项目？但此处已过滤为应用模块），回退到 defaultConfig
    else if (androidExt.groovyGetProperty("defaultConfig")
            ?.groovyGetProperty("manifestPlaceholders") != null
    ) {
        // 尝试从 defaultConfig 获取 applicationId
        var appId: String? = null
        val defaultConfig = androidExt.groovyGetProperty("defaultConfig")
        if (defaultConfig.groovyHasProperty("applicationId")) appId =
            defaultConfig.groovyGetProperty("applicationId") as String?

        val combinedPlaceholders = LinkedHashMap<String, Any?>()
        if (appId != null) combinedPlaceholders["JPUSH_PKGNAME"] = appId
        combinedPlaceholders.putAll(placeholders)

        defaultConfig.groovyPutAll("manifestPlaceholders", combinedPlaceholders)
    } else {
        println("JPush: 错误 - 无法访问 applicationVariants 或 defaultConfig.manifestPlaceholders")
    }
}

if (placeholdersToInject.isNotEmpty()) {
    gradle.projectsEvaluated {
        rootProject.allprojects(Action<Project> {
            val subproject = this
            if (subproject.name == "app" || subproject.path == ":app") {
                if (subproject.plugins.hasPlugin("com.android.application")) {
                    injectPlaceholdersForModule(subproject, placeholdersToInject)
                } else {
                    println("JPush: 项目 ${subproject.path} 不是 Android 应用模块，跳过")
                }
            }
        })
    }
}

// ==================== 注入结束 ====================

plugins {
    id("com.android.library")
    id("kotlin-android")
}

if (jPushHuaweiEnable) apply { plugin("com.huawei.agconnect") }

android {
    namespace = "org.leoli.plugin.jpush.flutter.android"

    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
        }
    }

    defaultConfig {
        minSdk = 24

        // library 混淆 -> 随 library 引用，自动添加到 apk 打包混淆
        consumerProguardFiles("proguard-rules.pro")
    }

    testOptions {
        // Groovy 闭包以 Test 任务为 delegate，kts 中 Test 任务以参数 it 传入
        unitTests.all {
            it.useJUnitPlatform()

            it.testLogging {
                events("passed", "skipped", "failed", "standardOut", "standardError")
                showStandardStreams = true
            }

            it.outputs.upToDateWhen { false }
        }
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.mockito:mockito-core:5.0.0")

    val jPushVersion = "6.2.0"

    // 接入 华为 厂商
    if (jPushHuaweiEnable) implementation("cn.jiguang.sdk.plugin:huawei:${jPushVersion}")

    // 接入 小米 厂商
    if (jPushXiaomiEnable) implementation("cn.jiguang.sdk.plugin:xiaomi:$jPushVersion")

    // 接入 魅族 厂商
    if (jPushMeiZuEnable) implementation("cn.jiguang.sdk.plugin:meizu:$jPushVersion")

    // 接入 vivo 厂商
    if (jPushVivoEnable) implementation("cn.jiguang.sdk.plugin:vivo:$jPushVersion")

    // 接入 oppo 厂商
    if (jPushOppoEnable) {
        implementation("cn.jiguang.sdk.plugin:oppo:$jPushVersion")
        implementation("com.google.code.gson:gson:2.10.1")
        implementation("androidx.annotation:annotation:1.1.0")
    }

    // 接入 荣耀 厂商
    if (jPushHonorEnable) implementation("cn.jiguang.sdk.plugin:honor:$jPushVersion")
}
