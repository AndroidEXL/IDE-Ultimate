# Default AGP rules plus Compose consumer rules are enough for this app.
# Keep line numbers to make crash reports readable.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Keep data models and Firebase classes intact under R8
-keep class com.androidexl.ideultimate.data.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.termux.terminal.** { *; }
-keep class com.termux.view.** { *; }
-keep class com.example.ubuntuterminal.** { *; }
-keep class com.hoho.android.usbserial.** { *; }
-keep class me.jahnen.libaums.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
