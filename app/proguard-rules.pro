# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============ GSON RULES ============
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep generic signature of TypeToken and its subclasses with R8 version 3.0 and higher
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ============ FEATURE ANNOUNCEMENT DATA CLASSES ============
-keep class com.matrix.autoreply.utils.FeatureAnnouncementManager$FeatureAnnouncement { *; }
-keep class com.matrix.autoreply.utils.FeatureAnnouncementManager$Feature { *; }

# ============ AI NETWORK MODEL CLASSES ============
-keep class com.matrix.autoreply.network.model.ai.** { *; }

# ============ OTHER DATA CLASSES ============
-keep class com.matrix.autoreply.model.** { *; }
-keep class com.matrix.autoreply.constants.PromptTemplate { *; }
