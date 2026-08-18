# ProGuard rules para release

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# DTOs do app (todas as classes com @Serializable)
-keep,includedescriptorclasses class com.mlopes.gestor.data.remote.dto.**$$serializer { *; }
-keepclassmembers class com.mlopes.gestor.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.mlopes.gestor.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep @dagger.hilt.android.HiltAndroidApp class *

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Tink (criptografia, Jetpack Security) - errorprone annotations sao opcionais
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.api.client.**
-dontwarn com.google.api.client.http.**
-dontwarn com.google.api.client.http.javanet.**
-dontwarn org.joda.time.**
-dontwarn org.joda.time.Instant
-keep class com.google.crypto.tink.** { *; }
-dontwarn javax.annotation.**

# Compose
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-dontwarn androidx.compose.**
