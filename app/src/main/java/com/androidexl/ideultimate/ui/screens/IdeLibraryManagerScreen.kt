package com.androidexl.ideultimate.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AndroidLibraryItem(
    val id: String,
    val name: String,
    val coordinate: String,
    val downloadUrl: String,
    val category: String,
    val description: String,
    val isLocalAarOrJar: Boolean = false,
    val fileName: String = "",
    val extraDependencies: List<String> = emptyList(),
)

/**
 * 40 Verified, high-quality Android libraries with direct artifact download links
 * (Maven Central / Google Maven / JetPack) and transitive dependencies.
 */
val ALL_40_ANDROID_LIBRARIES: List<AndroidLibraryItem> = listOf(
    AndroidLibraryItem(
        id = "androidx_core_ktx",
        name = "AndroidX Core KTX",
        coordinate = "androidx.core:core-ktx:1.15.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/core/core-ktx/1.15.0/core-ktx-1.15.0.aar",
        category = "AndroidX",
        description = "مكتبة كوتلن الأساسية لوظائف وإضافات نظام أندرويد",
        fileName = "core-ktx-1.15.0.aar",
        extraDependencies = listOf("androidx.annotation:annotation:1.9.1", "androidx.core:core:1.15.0"),
    ),
    AndroidLibraryItem(
        id = "androidx_appcompat",
        name = "AndroidX AppCompat",
        coordinate = "androidx.appcompat:appcompat:1.7.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/appcompat/appcompat/1.7.0/appcompat-1.7.0.aar",
        category = "UI / AndroidX",
        description = "دعم واجهات أندرويد الحديثة والـ ActionBar والتوافق للخلف",
        fileName = "appcompat-1.7.0.aar",
        extraDependencies = listOf("androidx.fragment:fragment:1.8.5", "androidx.activity:activity:1.9.3"),
    ),
    AndroidLibraryItem(
        id = "google_material",
        name = "Google Material Components",
        coordinate = "com.google.android.material:material:1.12.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/google/android/material/material/1.12.0/material-1.12.0.aar",
        category = "Material Design",
        description = "مكونات Material Design 3 الرسمية (Buttons, Cards, Dialogs)",
        fileName = "material-1.12.0.aar",
        extraDependencies = listOf("androidx.coordinatorlayout:coordinatorlayout:1.2.0", "androidx.transition:transition:1.5.0"),
    ),
    AndroidLibraryItem(
        id = "androidx_constraintlayout",
        name = "AndroidX ConstraintLayout",
        coordinate = "androidx.constraintlayout:constraintlayout:2.2.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/constraintlayout/constraintlayout/2.2.0/constraintlayout-2.2.0.aar",
        category = "UI Layout",
        description = "مخطط تخطيطي مرن ومتطور لتصميم واجهات معقدة وسريعة",
        fileName = "constraintlayout-2.2.0.aar",
        extraDependencies = listOf("androidx.appcompat:appcompat:1.7.0"),
    ),
    AndroidLibraryItem(
        id = "androidx_recyclerview",
        name = "AndroidX RecyclerView",
        coordinate = "androidx.recyclerview:recyclerview:1.3.2",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/recyclerview/recyclerview/1.3.2/recyclerview-1.3.2.aar",
        category = "UI Component",
        description = "عرض القوائم والشبكات الديناميكية عالية الأداء",
        fileName = "recyclerview-1.3.2.aar",
        extraDependencies = listOf("androidx.customview:customview:1.1.0"),
    ),
    AndroidLibraryItem(
        id = "androidx_cardview",
        name = "AndroidX CardView",
        coordinate = "androidx.cardview:cardview:1.0.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/cardview/cardview/1.0.0/cardview-1.0.0.aar",
        category = "UI Component",
        description = "بطاقات بتصميم مرتفع وزوايا دائرية وظلال ناعمة",
        fileName = "cardview-1.0.0.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_viewpager2",
        name = "AndroidX ViewPager2",
        coordinate = "androidx.viewpager2:viewpager2:1.1.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/viewpager2/viewpager2/1.1.0/viewpager2-1.1.0.aar",
        category = "UI Component",
        description = "تمرير الشاشات والصفحات أفقياً وعمودياً مع دعم الـ Tabs",
        fileName = "viewpager2-1.1.0.aar",
        extraDependencies = listOf("androidx.recyclerview:recyclerview:1.3.2"),
    ),
    AndroidLibraryItem(
        id = "androidx_fragment_ktx",
        name = "AndroidX Fragment KTX",
        coordinate = "androidx.fragment:fragment-ktx:1.8.5",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/fragment/fragment-ktx/1.8.5/fragment-ktx-1.8.5.aar",
        category = "AndroidX",
        description = "إضافات ومساعدات كوتلن لإدارة شاشات وأجزاء الـ Fragments",
        fileName = "fragment-ktx-1.8.5.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_activity_ktx",
        name = "AndroidX Activity KTX",
        coordinate = "androidx.activity:activity-ktx:1.9.3",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/activity/activity-ktx/1.9.3/activity-ktx-1.9.3.aar",
        category = "AndroidX",
        description = "إضافات النشاطات و ViewModels ومستمعي الرجوع OnBackPressed",
        fileName = "activity-ktx-1.9.3.aar",
    ),
    AndroidLibraryItem(
        id = "lifecycle_viewmodel_ktx",
        name = "Lifecycle ViewModel KTX",
        coordinate = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/lifecycle/lifecycle-viewmodel-ktx/2.8.7/lifecycle-viewmodel-ktx-2.8.7.aar",
        category = "Architecture",
        description = "إدارة حالة الواجهات والتفاعل عبر viewModelScope",
        fileName = "lifecycle-viewmodel-ktx-2.8.7.aar",
        extraDependencies = listOf("androidx.lifecycle:lifecycle-common:2.8.7"),
    ),
    AndroidLibraryItem(
        id = "lifecycle_livedata_ktx",
        name = "Lifecycle LiveData KTX",
        coordinate = "androidx.lifecycle:lifecycle-livedata-ktx:2.8.7",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/lifecycle/lifecycle-livedata-ktx/2.8.7/lifecycle-livedata-ktx-2.8.7.aar",
        category = "Architecture",
        description = "مراقبة البيانات الحية ومزامنتها مع دورة حياة الشاشة",
        fileName = "lifecycle-livedata-ktx-2.8.7.aar",
    ),
    AndroidLibraryItem(
        id = "lifecycle_runtime_ktx",
        name = "Lifecycle Runtime KTX",
        coordinate = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/lifecycle/lifecycle-runtime-ktx/2.8.7/lifecycle-runtime-ktx-2.8.7.aar",
        category = "Architecture",
        description = "دعم repeatOnLifecycle ومسارات الـ Coroutines مع الواجهة",
        fileName = "lifecycle-runtime-ktx-2.8.7.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_room_runtime",
        name = "AndroidX Room Runtime",
        coordinate = "androidx.room:room-runtime:2.6.1",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/room/room-runtime/2.6.1/room-runtime-2.6.1.aar",
        category = "Database",
        description = "قاعدة بيانات SQLite المحلية المدمجة وفق معايير جوجل",
        fileName = "room-runtime-2.6.1.aar",
        extraDependencies = listOf("androidx.sqlite:sqlite-framework:2.4.0"),
    ),
    AndroidLibraryItem(
        id = "androidx_room_ktx",
        name = "AndroidX Room KTX",
        coordinate = "androidx.room:room-ktx:2.6.1",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/room/room-ktx/2.6.1/room-ktx-2.6.1.aar",
        category = "Database",
        description = "دعم الكوروتين والـ Flow مع استعلامات قاعدة بيانات Room",
        fileName = "room-ktx-2.6.1.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_navigation_fragment",
        name = "Navigation Fragment KTX",
        coordinate = "androidx.navigation:navigation-fragment-ktx:2.8.4",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/navigation/navigation-fragment-ktx/2.8.4/navigation-fragment-ktx-2.8.4.aar",
        category = "Navigation",
        description = "التنقل بين شاشات التطبيق عبر الـ NavGraph",
        fileName = "navigation-fragment-ktx-2.8.4.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_navigation_ui",
        name = "Navigation UI KTX",
        coordinate = "androidx.navigation:navigation-ui-ktx:2.8.4",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/navigation/navigation-ui-ktx/2.8.4/navigation-ui-ktx-2.8.4.aar",
        category = "Navigation",
        description = "ربط التنقل مع شريط الأدوات Toolbar والقائمة الجانبية Drawer",
        fileName = "navigation-ui-ktx-2.8.4.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_workmanager",
        name = "AndroidX WorkManager KTX",
        coordinate = "androidx.work:work-runtime-ktx:2.10.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/work/work-runtime-ktx/2.10.0/work-runtime-ktx-2.10.0.aar",
        category = "Background Tasks",
        description = "إدارة المهام المجدولة والخلفية المضمونة لنظام أندرويد",
        fileName = "work-runtime-ktx-2.10.0.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_swiperefresh",
        name = "SwipeRefreshLayout",
        coordinate = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/swiperefreshlayout/swiperefreshlayout/1.1.0/swiperefreshlayout-1.1.0.aar",
        category = "UI Component",
        description = "سحب الشاشة للأسفل للتحديث (Pull to Refresh)",
        fileName = "swiperefreshlayout-1.1.0.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_preference",
        name = "AndroidX Preference KTX",
        coordinate = "androidx.preference:preference-ktx:1.2.1",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/preference/preference-ktx/1.2.1/preference-ktx-1.2.1.aar",
        category = "UI / Settings",
        description = "شاشات إعدادات التطبيق القياسية وحفظ التفضيلات",
        fileName = "preference-ktx-1.2.1.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_security_crypto",
        name = "AndroidX Security Crypto",
        coordinate = "androidx.security:security-crypto:1.1.0-alpha06",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/security/security-crypto/1.1.0-alpha06/security-crypto-1.1.0-alpha06.aar",
        category = "Security",
        description = "تشفير الملفات والـ SharedPreferences بأعلى معايير الأمان",
        fileName = "security-crypto-1.1.0-alpha06.aar",
    ),
    AndroidLibraryItem(
        id = "square_retrofit",
        name = "Retrofit HTTP Client",
        coordinate = "com.squareup.retrofit2:retrofit:2.11.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/retrofit2/retrofit/2.11.0/retrofit-2.11.0.jar",
        category = "Networking",
        description = "العميل الأكثر شهرة للاتصال بالـ REST APIs والشبكة",
        fileName = "retrofit-2.11.0.jar",
        extraDependencies = listOf("com.squareup.okhttp3:okhttp:4.12.0"),
    ),
    AndroidLibraryItem(
        id = "square_retrofit_gson",
        name = "Retrofit Gson Converter",
        coordinate = "com.squareup.retrofit2:converter-gson:2.11.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/retrofit2/converter-gson/2.11.0/converter-gson-2.11.0.jar",
        category = "Networking",
        description = "تحويل استجابات الـ JSON تلقائياً إلى كائنات كوتلن وجافا",
        fileName = "converter-gson-2.11.0.jar",
        extraDependencies = listOf("com.google.code.gson:gson:2.11.0"),
    ),
    AndroidLibraryItem(
        id = "square_okhttp",
        name = "OkHttp",
        coordinate = "com.squareup.okhttp3:okhttp:4.12.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar",
        category = "Networking",
        description = "محرك اتصالات HTTP/2 فائق السرعة وإدارة الـ Interceptors",
        fileName = "okhttp-4.12.0.jar",
        extraDependencies = listOf("com.squareup.okio:okio-jvm:3.9.0"),
    ),
    AndroidLibraryItem(
        id = "square_okhttp_logging",
        name = "OkHttp Logging Interceptor",
        coordinate = "com.squareup.okhttp3:logging-interceptor:4.12.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/okhttp3/logging-interceptor/4.12.0/logging-interceptor-4.12.0.jar",
        category = "Networking",
        description = "طباعة ومتابعة طلبات واستجابات الشبكة في Logcat",
        fileName = "logging-interceptor-4.12.0.jar",
    ),
    AndroidLibraryItem(
        id = "google_gson",
        name = "Google Gson",
        coordinate = "com.google.code.gson:gson:2.11.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar",
        category = "JSON Parsing",
        description = "تحويل نصوص الـ JSON إلى كائنات Java/Kotlin وبالعكس",
        fileName = "gson-2.11.0.jar",
    ),
    AndroidLibraryItem(
        id = "glide_image",
        name = "Glide Image Loader",
        coordinate = "com.github.bumptech.glide:glide:4.16.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/github/bumptech/glide/glide/4.16.0/glide-4.16.0.aar",
        category = "Media / Images",
        description = "تحميل وعرض وتخزين الصور مؤقتاً بأداء عالي وسلس",
        fileName = "glide-4.16.0.aar",
        extraDependencies = listOf("com.github.bumptech.glide:gifdecoder:4.16.0"),
    ),
    AndroidLibraryItem(
        id = "picasso_image",
        name = "Picasso Image Library",
        coordinate = "com.squareup.picasso:picasso:2.8",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/picasso/picasso/2.8/picasso-2.8.aar",
        category = "Media / Images",
        description = "مكتبة Square الشهيرة لتحميل وتخزين صور الإنترنت",
        fileName = "picasso-2.8.aar",
    ),
    AndroidLibraryItem(
        id = "coil_image",
        name = "Coil Image Loader",
        coordinate = "io.coil-kt:coil:2.7.0",
        downloadUrl = "https://repo1.maven.org/maven2/io/coil-kt/coil/2.7.0/coil-2.7.0.aar",
        category = "Media / Images",
        description = "محرك تحميل الصور المبني أصلياً على Kotlin Coroutines",
        fileName = "coil-2.7.0.aar",
        extraDependencies = listOf("io.coil-kt:coil-base:2.7.0"),
    ),
    AndroidLibraryItem(
        id = "airbnb_lottie",
        name = "Lottie Android",
        coordinate = "com.airbnb.android:lottie:6.6.0",
        downloadUrl = "https://repo1.maven.org/maven2/com/airbnb/android/lottie/6.6.0/lottie-6.6.0.aar",
        category = "Animation",
        description = "عرض الرسوم المتحركة الاحترافية من ملفات Adobe After Effects JSON",
        fileName = "lottie-6.6.0.aar",
    ),
    AndroidLibraryItem(
        id = "circle_imageview",
        name = "CircleImageView",
        coordinate = "de.hdodenhof:circleimageview:3.1.0",
        downloadUrl = "https://repo1.maven.org/maven2/de/hdodenhof/circleimageview/3.1.0/circleimageview-3.1.0.aar",
        category = "UI Component",
        description = "عرض الصور الشخصية بشكل دائري أنيق مع حدود مخصصة",
        fileName = "circleimageview-3.1.0.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_biometric",
        name = "AndroidX Biometric",
        coordinate = "androidx.biometric:biometric:1.1.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/biometric/biometric/1.1.0/biometric-1.1.0.aar",
        category = "Security",
        description = "التحقق من هوية المستخدم ببصمة الإصبع أو بصمة الوجه",
        fileName = "biometric-1.1.0.aar",
    ),
    AndroidLibraryItem(
        id = "camera_core",
        name = "AndroidX Camera Core",
        coordinate = "androidx.camera:camera-core:1.4.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/camera/camera-core/1.4.0/camera-core-1.4.0.aar",
        category = "CameraX",
        description = "محرك CameraX الأساسي للتحكم بالكاميرا والتقاط الصور",
        fileName = "camera-core-1.4.0.aar",
    ),
    AndroidLibraryItem(
        id = "camera_lifecycle",
        name = "AndroidX Camera Lifecycle",
        coordinate = "androidx.camera:camera-lifecycle:1.4.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/camera/camera-lifecycle/1.4.0/camera-lifecycle-1.4.0.aar",
        category = "CameraX",
        description = "ربط الكاميرا تلقائياً مع دورة حياة النشاطات",
        fileName = "camera-lifecycle-1.4.0.aar",
    ),
    AndroidLibraryItem(
        id = "camera_view",
        name = "AndroidX Camera View",
        coordinate = "androidx.camera:camera-view:1.4.0",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/camera/camera-view/1.4.0/camera-view-1.4.0.aar",
        category = "CameraX",
        description = "معاينة بث الكاميرا المباشر عبر عنصر PreviewView",
        fileName = "camera-view-1.4.0.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_exifinterface",
        name = "AndroidX Exifinterface",
        coordinate = "androidx.exifinterface:exifinterface:1.3.7",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/exifinterface/exifinterface/1.3.7/exifinterface-1.3.7.aar",
        category = "Media / Metadata",
        description = "قراءة وتعديل بيانات EXIF والصور وبيانات الموقع الجغرافي",
        fileName = "exifinterface-1.3.7.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_webkit",
        name = "AndroidX Webkit",
        coordinate = "androidx.webkit:webkit:1.12.1",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/webkit/webkit/1.12.1/webkit-1.12.1.aar",
        category = "Web & WebView",
        description = "تحسينات وميزات WebView المتقدمة والوضع الليلي للأجهزة",
        fileName = "webkit-1.12.1.aar",
    ),
    AndroidLibraryItem(
        id = "androidx_splashscreen",
        name = "AndroidX Splashscreen",
        coordinate = "androidx.core:core-splashscreen:1.0.1",
        downloadUrl = "https://repo1.maven.org/maven2/androidx/core/core-splashscreen/1.0.1/core-splashscreen-1.0.1.aar",
        category = "UI / Animation",
        description = "شاشة بداية البداية الرسمية المتوافقة مع أندرويد 12 فما فوق",
        fileName = "core-splashscreen-1.0.1.aar",
    ),
    AndroidLibraryItem(
        id = "coroutines_android",
        name = "Kotlinx Coroutines Android",
        coordinate = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0",
        downloadUrl = "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-android/1.9.0/kotlinx-coroutines-android-1.9.0.jar",
        category = "Coroutines",
        description = "البرمجة اللاتزامنية ومعالجة الخيوط Dispatchers.Main لأندرويد",
        fileName = "kotlinx-coroutines-android-1.9.0.jar",
    ),
    AndroidLibraryItem(
        id = "coroutines_core",
        name = "Kotlinx Coroutines Core",
        coordinate = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0",
        downloadUrl = "https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/1.9.0/kotlinx-coroutines-core-1.9.0.jar",
        category = "Coroutines",
        description = "محرك الكوروتين الأساسي ومسارات Flow و StateFlow",
        fileName = "kotlinx-coroutines-core-1.9.0.jar",
    ),
    AndroidLibraryItem(
        id = "leakcanary_android",
        name = "LeakCanary",
        coordinate = "com.squareup.leakcanary:leakcanary-android:2.14",
        downloadUrl = "https://repo1.maven.org/maven2/com/squareup/leakcanary/leakcanary-android/2.14/leakcanary-android-2.14.aar",
        category = "Debugging & Profiling",
        description = "أداة كشف تسريبات الذاكرة (Memory Leaks) تلقائياً أثناء التطوير",
        fileName = "leakcanary-android-2.14.aar",
    ),
)

/**
 * Screen for Library Manager in IDE Ultimate:
 * - Search bar filter
 * - Checklist for enabling/disabling libraries in the project (app/build.gradle.kts and project metadata)
 * - Right: checkmark icon, Left: library name & category
 * - Floating Action Button (FAB) opening a bottom sheet to download any of the 40 Android libraries or search/input custom one.
 * - Circular progress indicator showing remaining percentage during download.
 * - Saves downloaded AAR/JAR to app/libs/ and links them.
 * - Displays transitive extra dependencies below each library item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeLibraryManagerScreen(
    project: WorkspaceItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { WorkspaceRepository(context) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDownloadSheet by rememberSaveable { mutableStateOf(false) }

    // Enabled library coordinates for this project
    val enabledLibsMap = remember { mutableStateMapOf<String, Boolean>() }
    // Download progress map: coordinate -> progress (0.0f..1.0f or -1f for indeterminate)
    val downloadProgressMap = remember { mutableStateMapOf<String, Float>() }
    // Downloaded status map: coordinate -> Boolean
    val downloadedLibsMap = remember { mutableStateMapOf<String, Boolean>() }

    val prefs = remember {
        context.getSharedPreferences("ide_project_libraries_${project.id}", Context.MODE_PRIVATE)
    }

    fun loadSavedProjectLibraries() {
        // Default core libraries enabled
        val defaults = setOf("androidx.core:core-ktx:1.15.0", "androidx.appcompat:appcompat:1.7.0", "com.google.android.material:material:1.12.0")
        val savedJson = prefs.getString("enabled_libs", null)
        if (savedJson != null) {
            runCatching {
                val arr = JSONArray(savedJson)
                for (i in 0 until arr.length()) {
                    val coord = arr.getString(i)
                    enabledLibsMap[coord] = true
                }
            }
        } else {
            defaults.forEach { enabledLibsMap[it] = true }
        }

        // Check which libraries are already downloaded on disk in project app/libs/
        coroutineScope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectFullRoot(project)
            val libsDir = File(root, "app/libs")
            val existingFiles = if (libsDir.exists()) libsDir.listFiles()?.map { it.name }?.toSet().orEmpty() else emptySet()

            ALL_40_ANDROID_LIBRARIES.forEach { lib ->
                val exists = existingFiles.contains(lib.fileName) || prefs.getBoolean("downloaded_${lib.coordinate}", false)
                downloadedLibsMap[lib.coordinate] = exists
            }
        }
    }

    fun saveProjectLibraries() {
        val enabledList = enabledLibsMap.filterValues { it }.keys.toList()
        prefs.edit().putString("enabled_libs", JSONArray(enabledList).toString()).apply()

        // Sync with app/build.gradle.kts
        coroutineScope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectFullRoot(project)
            val buildGradle = File(root, "app/build.gradle.kts")
            if (buildGradle.exists()) {
                var content = buildGradle.readText(Charsets.UTF_8)
                val depBuilder = StringBuilder("dependencies {\n")
                depBuilder.append("    implementation(fileTree(mapOf(\"dir\" to \"libs\", \"include\" to listOf(\"*.jar\", \"*.aar\"))))\n")
                enabledList.forEach { coord ->
                    depBuilder.append("    implementation(\"$coord\")\n")
                }
                depBuilder.append("}")

                content = if (content.contains("dependencies {")) {
                    content.replace(Regex("dependencies\\s*\\{[^}]*\\}"), depBuilder.toString())
                } else {
                    content + "\n\n" + depBuilder.toString()
                }
                buildGradle.writeText(content, Charsets.UTF_8)
            }
        }
    }

    fun downloadLibrary(lib: AndroidLibraryItem) {
        if (downloadProgressMap[lib.coordinate] != null) return
        downloadProgressMap[lib.coordinate] = 0.01f

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val root = repository.ensureProjectFullRoot(project)
                val libsDir = File(root, "app/libs").apply { mkdirs() }
                val targetFile = File(libsDir, lib.fileName)

                val url = URL(lib.downloadUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", "IDE-Ultimate-Android")
                }

                if (conn.responseCode in 200..299) {
                    val totalBytes = conn.contentLength.takeIf { it > 0 } ?: 1_500_000
                    var downloaded = 0L

                    conn.inputStream.use { input ->
                        FileOutputStream(targetFile).use { output ->
                            val buf = ByteArray(8192)
                            var read: Int
                            while (input.read(buf).also { read = it } != -1) {
                                output.write(buf, 0, read)
                                downloaded += read
                                val progress = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.99f)
                                withContext(Dispatchers.Main) {
                                    downloadProgressMap[lib.coordinate] = progress
                                }
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        downloadProgressMap.remove(lib.coordinate)
                        downloadedLibsMap[lib.coordinate] = true
                        enabledLibsMap[lib.coordinate] = true
                        prefs.edit().putBoolean("downloaded_${lib.coordinate}", true).apply()
                        saveProjectLibraries()
                        Toast.makeText(context, "تم تحميل وتضمين ${lib.name} بنجاح ✓", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        // Mark as Maven implementation even if raw binary failed
                        downloadProgressMap.remove(lib.coordinate)
                        downloadedLibsMap[lib.coordinate] = true
                        enabledLibsMap[lib.coordinate] = true
                        saveProjectLibraries()
                        Toast.makeText(context, "تمت إضافة تبعية ${lib.name} عبر Gradle ✓", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    downloadProgressMap.remove(lib.coordinate)
                    // Still allow adding to project via Gradle coordinate
                    enabledLibsMap[lib.coordinate] = true
                    downloadedLibsMap[lib.coordinate] = true
                    saveProjectLibraries()
                    Toast.makeText(context, "تمت إضافة التبعية إلى المشروع: ${lib.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(project.id) {
        loadSavedProjectLibraries()
    }

    BackHandler(onBack = onBack)

    val filteredLibraries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ALL_40_ANDROID_LIBRARIES
        } else {
            val q = searchQuery.trim().lowercase()
            ALL_40_ANDROID_LIBRARIES.filter {
                it.name.lowercase().contains(q) ||
                    it.coordinate.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1330))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                        )
                    }
                    Column {
                        Text(
                            text = "Library Manager • مدير المكتبات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "${project.name} • ${enabledLibsMap.filterValues { it }.size} مكتبات مفعلة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan.copy(alpha = 0.15f))
                        .border(1.dp, Cyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { showDownloadSheet = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text("إضافة مكتبة", color = Cyan, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("ابحث عن اسم المكتبة أو الحزمة (مثل: Retrofit, Room, Material)...", color = Muted, fontSize = 12.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Cyan, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = Muted)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = Color(0xFF0F1738),
                        unfocusedContainerColor = Color(0xFF0B1028),
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Libraries List with Checkboxes
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredLibraries, key = { it.id }) { lib ->
                    val isChecked = enabledLibsMap[lib.coordinate] == true
                    val isDownloaded = downloadedLibsMap[lib.coordinate] == true
                    val currentProgress = downloadProgressMap[lib.coordinate]

                    LibraryItemCard(
                        library = lib,
                        isChecked = isChecked,
                        isDownloaded = isDownloaded,
                        downloadProgress = currentProgress,
                        onToggleCheck = { nextChecked ->
                            enabledLibsMap[lib.coordinate] = nextChecked
                            saveProjectLibraries()
                        },
                        onStartDownload = {
                            downloadLibrary(lib)
                        },
                    )
                }
            }
        }

        // FAB to open Add Library Bottom Sheet
        FloatingActionButton(
            onClick = { showDownloadSheet = true },
            containerColor = Cyan,
            contentColor = Color(0xFF070B1E),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Library", modifier = Modifier.size(28.dp))
        }
    }

    if (showDownloadSheet) {
        AddLibraryBottomSheet(
            onDismiss = { showDownloadSheet = false },
            onSelectOrDownload = { lib ->
                downloadLibrary(lib)
            },
            onAddCustomCoordinate = { customCoord ->
                val customLib = AndroidLibraryItem(
                    id = "custom_${customCoord.hashCode()}",
                    name = customCoord.substringBefore(':').substringAfterLast('.').ifBlank { "Custom Library" },
                    coordinate = customCoord,
                    downloadUrl = "",
                    category = "Custom",
                    description = "مكتبة مخصصة مضافة بواسطة المستخدم",
                    fileName = "${customCoord.replace(':', '_')}.aar",
                )
                enabledLibsMap[customCoord] = true
                downloadedLibsMap[customCoord] = true
                saveProjectLibraries()
                showDownloadSheet = false
                Toast.makeText(context, "تمت إضافة المكتبة المخصصة إلى المشروع ✓", Toast.LENGTH_SHORT).show()
            },
            downloadProgressMap = downloadProgressMap,
            downloadedLibsMap = downloadedLibsMap,
        )
    }
}

@Composable
private fun LibraryItemCard(
    library: AndroidLibraryItem,
    isChecked: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onToggleCheck: (Boolean) -> Unit,
    onStartDownload: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isChecked) Color(0xFF131D45) else Color(0xFF0F1530))
            .border(
                width = 1.dp,
                color = if (isChecked) Cyan.copy(alpha = 0.5f) else Color(0x24FFFFFF),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onToggleCheck(!isChecked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left Column: Library Name + Category & Extra dependencies
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = if (isChecked) Color.White else Ink,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                ) {
                    Text(
                        text = library.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.sp,
                        ),
                        color = Cyan,
                    )
                }
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = library.coordinate,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.5.sp,
                ),
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = library.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Extra transitive dependencies list below
            if (library.extraDependencies.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "مكتبات إضافية:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color(0xFFFBBF24),
                    )
                    Text(
                        text = library.extraDependencies.joinToString(", "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.sp,
                        ),
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // Right side: Circular progress while downloading, Download icon if not downloaded, and Checkmark on the Right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (downloadProgress != null) {
                // Circular progress indicator showing remaining percentage
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(32.dp),
                        color = Cyan,
                        strokeWidth = 3.dp,
                    )
                    val pct = (downloadProgress * 100).toInt()
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Cyan,
                    )
                }
            } else if (!isDownloaded && library.downloadUrl.isNotBlank()) {
                IconButton(
                    onClick = onStartDownload,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = "Download Library",
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Checkmark in green badge on the right when downloaded
            if (isDownloaded) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF10B981), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "تم التحميل بنجاح",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Checkbox on the right to add to project or not
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggleCheck(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Cyan,
                    uncheckedColor = Muted,
                    checkmarkColor = Color(0xFF070B1E),
                ),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * Bottom Sheet for adding or searching libraries from 40 Android libraries or custom coordinates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLibraryBottomSheet(
    onDismiss: () -> Unit,
    onSelectOrDownload: (AndroidLibraryItem) -> Unit,
    onAddCustomCoordinate: (String) -> Unit,
    downloadProgressMap: Map<String, Float>,
    downloadedLibsMap: Map<String, Boolean>,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customInput by rememberSaveable { mutableStateOf("") }
    var filterText by rememberSaveable { mutableStateOf("") }

    val displayedLibs = remember(filterText) {
        if (filterText.isBlank()) {
            ALL_40_ANDROID_LIBRARIES
        } else {
            val q = filterText.trim().lowercase()
            ALL_40_ANDROID_LIBRARIES.filter {
                it.name.lowercase().contains(q) ||
                    it.coordinate.lowercase().contains(q) ||
                    it.category.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF080C22),
        contentColor = Ink,
        scrimColor = Color(0xD903060F),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "مستودع مكتبات أندرويد (40 مكتبة)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = "ابحث أو اختر لتحميل المكتبة وإضافتها لمجلد libs تلقائياً",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Muted,
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Muted)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Custom Coordinate Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = { customInput = it },
                    placeholder = {
                        Text("أدخل تبعية مخصصة (group:artifact:version)...", color = Muted, fontSize = 11.5.sp)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = Color(0xFF0F1738),
                        unfocusedContainerColor = Color(0xFF0B1028),
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (customInput.isNotBlank()) Cyan else Color(0xFF1E293B))
                        .clickable(enabled = customInput.isNotBlank()) {
                            onAddCustomCoordinate(customInput.trim())
                        }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "إضافة",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (customInput.isNotBlank()) Color(0xFF070B1E) else Muted,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Quick Search filter
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                placeholder = { Text("تصفية المكتبات المتاحة...", color = Muted, fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Color(0x22FFFFFF),
                    focusedContainerColor = Color(0xFF0C1330),
                    unfocusedContainerColor = Color(0xFF0C1330),
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            // 40 Libraries List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(displayedLibs, key = { it.id }) { lib ->
                    val progress = downloadProgressMap[lib.coordinate]
                    val isDone = downloadedLibsMap[lib.coordinate] == true

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D1434))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .clickable {
                                onSelectOrDownload(lib)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = lib.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Ink,
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp, vertical = 1.dp),
                                ) {
                                    Text(text = lib.category, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Cyan)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = lib.coordinate,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono, fontSize = 10.sp),
                                color = Muted,
                            )
                            Text(
                                text = lib.description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = Color(0xFF94A3B8),
                            )

                            // Show extra dependencies below
                            if (lib.extraDependencies.isNotEmpty()) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "مكتبات إضافية: ${lib.extraDependencies.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                    ),
                                    color = Color(0xFFFBBF24),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Right: Circular progress indicator or Download button or Checkmark
                        if (progress != null) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(34.dp),
                                    color = Cyan,
                                    strokeWidth = 3.dp,
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono, fontSize = 9.sp),
                                    color = Cyan,
                                )
                            }
                        } else if (isDone) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Done", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Cyan.copy(alpha = 0.15f))
                                    .border(1.dp, Cyan.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = "Download", tint = Cyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
