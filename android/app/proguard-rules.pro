# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class ge.myshop.app.**$$serializer { *; }
-keepclassmembers class ge.myshop.app.** { *** Companion; }
-keepclasseswithmembers class ge.myshop.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Ktor / Supabase
-dontwarn org.slf4j.**
-dontwarn io.ktor.**
-keep class io.github.jan.supabase.** { *; }
