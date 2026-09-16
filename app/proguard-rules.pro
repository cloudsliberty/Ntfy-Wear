# OkHttp / Okio use some reflection-free R8 rules bundled in their own jars.
-dontwarn okhttp3.**
-dontwarn okio.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class io.ntfy.wear.data.** {
    *** Companion;
}
-keepclassmembers class io.ntfy.wear.data.** {
    ** Companion;
}
-keep,includedescriptorclasses class io.ntfy.wear.**$$serializer { *; }
-keepclassmembers class io.ntfy.wear.** {
    *** Companion;
}
