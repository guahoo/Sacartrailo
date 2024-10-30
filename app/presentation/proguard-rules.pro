# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn org.xmlpull.v1.**
-keep class org.xmlpull.v1.** { *; }
-keep class android.content.res.XmlResourceParser { *; }
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-keep class android.util.Log { *; }
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # Add project specific ProGuard rules here.
 # You can control the set of applied configuration files using the
 # proguardFiles setting in build.gradle.
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

 -keepclassmembers class * extends java.lang.Enum {
     <fields>;
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }
 -dontwarn javax.annotation.**
 -dontwarn java.lang.invoke.**

 -keep class butterknife.** { *; }
 -dontwarn butterknife.internal.**
 -keep class **$$ViewBinder { *; }

 -keepclasseswithmembernames class * {
     @butterknife.* <fields>;
 }

 -keepclasseswithmembernames class * {
     @butterknife.* <methods>;
 }

# # rxjava
# -keep class rx.schedulers.Schedulers {
#     public static <methods>;
# }
# -keep class rx.schedulers.ImmediateScheduler {
#     public <methods>;
# }
# -keep class rx.schedulers.TestScheduler {
#     public <methods>;
# }
# -keep class rx.schedulers.Schedulers {
#     public static ** test();
# }
 -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
     long producerIndex;
     long consumerIndex;
 }
# -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#     long producerNode;
#     long consumerNode;
# }

 #glide
# -keep public class * implements com.bumptech.glide.module.GlideModule
# -keep public class * extends com.bumptech.glide.module.AppGlideModule
 -keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
   **[] $VALUES;
   public *;
 }
 -dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

 #Room
 -dontwarn android.arch.util.paging.CountedDataSource
 -dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource

 #Retrofit
 #-dontwarn retrofit.**
 #-keep class retrofit.** { *; }
 #-keepattributes Signature
 #-keepattributes Exceptions
 # Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
 # EnclosingMethod is required to use InnerClasses.
 -keepattributes Signature, InnerClasses, EnclosingMethod
 # Retrofit does reflection on method and parameter annotations.
 -keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
 # Retain service method parameters when optimizing.
 -keepclassmembers,allowshrinking,allowobfuscation interface * {
     @retrofit2.http.* <methods>;
 }
 # Ignore annotation used for build tooling.
 -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
 # Ignore JSR 305 annotations for embedding nullability information.
 -dontwarn javax.annotation.**
 # With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
 # and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
 -if interface * { @retrofit2.http.* <methods>; }
 -keep,allowobfuscation interface <1>

 #Okhttp
 -keepattributes Signature
 -keepattributes *Annotation*
 -keep class okhttp3.** { *; }
 -keep interface okhttp3.** { *; }
 -dontwarn okhttp3.**
 -dontnote okhttp3.**

 # Okio
 -keep class sun.misc.Unsafe { *; }
 -dontwarn java.nio.file.*
 -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

 #Gson
 -keep class sun.misc.Unsafe { *; }
 -keep class com.google.gson.stream.** { *; }

 -dontnote com.google.gson.*
 -dontnote android.net.http.*
 -dontnote org.apache.commons.codec.**
 -dontnote org.apache.http.**

 # Keep all model classes that Gson uses for deserialization
 -keep class com.guahoo.data.response.** { *; }
 -keepattributes Signature
 -keepattributes *Annotation

 # Keep Gson classes
 -keep class com.google.gson.** { *; }
# TODO FIX THIS
 -keep class com.guahoo.** { *; }




