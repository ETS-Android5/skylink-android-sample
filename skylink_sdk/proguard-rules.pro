# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/janidu/Desktop/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Do not obfuscate webrtc library
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**
-keep class io.socket.** { *; }
-dontwarn io.socket.**

-keep public class * {
    public *;
}

-keepclassmembers class * {
    public *;
}

-keepattributes Exceptions,InnerClasses
-keepparameternames