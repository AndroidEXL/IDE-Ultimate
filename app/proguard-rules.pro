# Default AGP rules plus Compose consumer rules are enough for this app.
# Keep line numbers to make crash reports readable.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Keep data models used with Firebase Realtime Database
-keep class com.androidexl.ideultimate.data.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
