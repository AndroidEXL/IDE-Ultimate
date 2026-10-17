# Default AGP rules plus Compose consumer rules are enough for this app.
# Keep line numbers to make crash reports readable.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Keep data models and Firebase classes intact under R8
-keep class com.androidexl.ideultimate.data.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
