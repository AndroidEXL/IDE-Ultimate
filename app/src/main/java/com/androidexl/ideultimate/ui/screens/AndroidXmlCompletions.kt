package com.androidexl.ideultimate.ui.screens

import androidx.compose.ui.graphics.Color

object AndroidXmlCompletions {

    val ALL_ANDROID_PERMISSIONS: List<Pair<String, String>> = listOf(
        // Connectivity & Network
        "android.permission.INTERNET" to "الوصول الكامل إلى شبكة الإنترنت",
        "android.permission.ACCESS_NETWORK_STATE" to "معرفة حالة الاتصال بالشبكة والإنترنت",
        "android.permission.ACCESS_WIFI_STATE" to "الوصول لمعلومات وتفاصيل شبكات Wi-Fi",
        "android.permission.CHANGE_WIFI_STATE" to "تغيير حالة اتصال Wi-Fi وتوصيل الشبكات",
        "android.permission.CHANGE_NETWORK_STATE" to "تغيير حالة اتصال شبكات البيانات",
        "android.permission.CHANGE_WIFI_MULTICAST_STATE" to "استقبال حزم البث المتعدد عبر Wi-Fi Multicast",
        "android.permission.ACCESS_FINE_LOCATION" to "الوصول الدقيق لموقع الجهاز عبر GPS",
        "android.permission.ACCESS_COARSE_LOCATION" to "الوصول التقريبي لموقع الجهاز عبر الشبكة",
        "android.permission.ACCESS_BACKGROUND_LOCATION" to "الوصول لموقع الجهاز أثناء تشغيل التطبيق في الخلفية",

        // Storage & Media (Scoped Storage, Full Storage & Media)
        "android.permission.READ_EXTERNAL_STORAGE" to "قراءة الملفات من الذاكرة الخارجية",
        "android.permission.WRITE_EXTERNAL_STORAGE" to "كتابة وتعديل الملفات على الذاكرة الخارجية",
        "android.permission.MANAGE_EXTERNAL_STORAGE" to "إدارة الذاكرة بالكامل وتصفح جميع الملفات (Android 11+)",
        "android.permission.READ_MEDIA_IMAGES" to "الوصول للصور والمعرض (Android 13+)",
        "android.permission.READ_MEDIA_VIDEO" to "الوصول لمقاطع الفيديو (Android 13+)",
        "android.permission.READ_MEDIA_AUDIO" to "الوصول للملفات الصوتية والموسيقى (Android 13+)",
        "android.permission.READ_MEDIA_VISUAL_USER_SELECTED" to "الوصول للوسائط التي يحددها المستخدم فقط (Android 14+)",
        "android.permission.ACCESS_MEDIA_LOCATION" to "قراءة بيانات الموقع الجغرافي EXIF داخل الصور",

        // Camera, Audio & Recording
        "android.permission.CAMERA" to "استخدام الكاميرا والتقاط الصور وتسجيل الفيديو",
        "android.permission.RECORD_AUDIO" to "تسجيل الصوت من الميكروفون",
        "android.permission.MODIFY_AUDIO_SETTINGS" to "تعديل إعدادات ومستويات الصوت العالمية",

        // Notifications & Foreground Services (Android 13, 14, 15)
        "android.permission.POST_NOTIFICATIONS" to "إرسال الإشعارات والتنبيهات للمستخدم (Android 13+)",
        "android.permission.FOREGROUND_SERVICE" to "تشغيل الخدمات في الواجهة الأمامية Foreground Service",
        "android.permission.FOREGROUND_SERVICE_DATA_SYNC" to "خدمة المزامنة ونقل البيانات في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" to "خدمة تشغيل الصوت والوسائط في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_LOCATION" to "خدمة تتبع الموقع الجغرافي في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" to "خدمة الاتصال بالأجهزة الخارجية في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_CAMERA" to "خدمة استخدام الكاميرا في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_MICROPHONE" to "خدمة الميكروفون في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_PHONE_CALL" to "خدمة المكالمات الهاتفية في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING" to "خدمة الرسائل عن بعد في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_SPECIAL_USE" to "خدمة ذات استخدام خاص ومحدد في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_SYSTEM_EXEMPTED" to "خدمة معفاة للنظام في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" to "خدمة تسجيل ومشاركة الشاشة في الواجهة الأمامية",
        "android.permission.FOREGROUND_SERVICE_HEALTH" to "خدمة الصحة وتتبع النشاط في الواجهة الأمامية",
        "android.permission.WAKE_LOCK" to "إبقاء المعالج نشطاً ومنع الشاشة/الهاتف من النوم",

        // Phone, Calls & SIM
        "android.permission.READ_PHONE_STATE" to "قراءة حالة الهاتف وهوية الشريحة والمكالمات الواردة",
        "android.permission.READ_PHONE_NUMBERS" to "قراءة رقم هاتف الجهاز المسجل على الشريحة",
        "android.permission.CALL_PHONE" to "إجراء المكالمات الهاتفية مباشرة دون فتح واجهة الاتصال",
        "android.permission.ANSWER_PHONE_CALLS" to "الرد التلقائي على المكالمات الهاتفية الواردة",
        "android.permission.MANAGE_OWN_CALLS" to "إدارة وتوجيه المكالمات الخاصة بالتطبيق",
        "android.permission.READ_CALL_LOG" to "قراءة سجل المكالمات الهاتفية الصادرة والواردة",
        "android.permission.WRITE_CALL_LOG" to "تعديل وحذف سجل المكالمات الهاتفية",
        "android.permission.PROCESS_OUTGOING_CALLS" to "اعتراض ومعالجة المكالمات الصادرة (قديم)",
        "android.permission.USE_SIP" to "إجراء واستقبال مكالمات بروتوكول الإنترنت SIP",

        // Contacts, Calendar & Accounts
        "android.permission.READ_CONTACTS" to "قراءة جهات الاتصال ودفتر العناوين في الهاتف",
        "android.permission.WRITE_CONTACTS" to "إضافة وتعديل وحذف جهات الاتصال في الهاتف",
        "android.permission.GET_ACCOUNTS" to "الوصول لقائمة الحسابات المسجلة في الهاتف",
        "android.permission.READ_CALENDAR" to "قراءة أحداث وتقويم ومواعيد المستخدم",
        "android.permission.WRITE_CALENDAR" to "إضافة وتعديل وحذف الأحداث والمواعيد في التقويم",

        // SMS & MMS
        "android.permission.SEND_SMS" to "إرسال رسائل SMS النصية القصيرة",
        "android.permission.RECEIVE_SMS" to "استقبال واعتراض رسائل SMS النصية الواردة",
        "android.permission.READ_SMS" to "قراءة رسائل SMS المحفوظة في صندوق الوارد",
        "android.permission.RECEIVE_WAP_PUSH" to "استقبال رسائل WAP الدفعية",
        "android.permission.RECEIVE_MMS" to "استقبال رسائل الوسائط المتعددة MMS",

        // Bluetooth & Nearby Devices
        "android.permission.BLUETOOTH" to "الاتصال بأجهزة البلوتوث المقترنة",
        "android.permission.BLUETOOTH_ADMIN" to "البحث عن أجهزة بلوتوث واقترانها وإدارتها",
        "android.permission.BLUETOOTH_CONNECT" to "الاتصال والتواصل مع أجهزة بلوتوث المقترنة (Android 12+)",
        "android.permission.BLUETOOTH_SCAN" to "البحث واستكشاف أجهزة بلوتوث المحيطة (Android 12+)",
        "android.permission.BLUETOOTH_ADVERTISE" to "إعلان وبث هوية الجهاز عبر البلوتوث (Android 12+)",
        "android.permission.NEARBY_WIFI_DEVICES" to "استكشاف أجهزة Wi-Fi القريبة المجاورة دون موقع (Android 13+)",
        "android.permission.UWB_RANGING" to "تحديد المدى والمسافة الدقيقة عبر النطاق فائق العرض Ultra-Wideband",
        "android.permission.NFC" to "الاتصال وقراءة بطاقات المدى القريب NFC",
        "android.permission.NFC_TRANSACTION_EVENT" to "استقبال أحداث معاملات الدفع عبر بطاقة NFC",
        "android.permission.NFC_PREFERRED_PAYMENT_INFO" to "معرفة معلومات خدمة الدفع المفضلة عبر NFC",

        // Sensors, Fitness & Biometrics
        "android.permission.BODY_SENSORS" to "قراءة مستشعرات الجسم الحيوية مثل معدل نبضات القلب",
        "android.permission.BODY_SENSORS_BACKGROUND" to "قراءة مستشعرات الجسم أثناء عمل التطبيق في الخلفية",
        "android.permission.ACTIVITY_RECOGNITION" to "التعرف على النشاط الحركي والمشي والركض والقيادة",
        "android.permission.HIGH_SAMPLING_RATE_SENSORS" to "قراءة بيانات المستشعرات بمعدل عينات فائق السرعة",
        "android.permission.USE_BIOMETRIC" to "التحقق بواسطة البصمة والتعرف على الوجه والمستشعرات الحيوية",
        "android.permission.USE_FINGERPRINT" to "التحقق بواسطة بصمة الإصبع (قديم)",

        // System, Windows & Startup
        "android.permission.SYSTEM_ALERT_WINDOW" to "الرسم والظهور فوق التطبيقات الأخرى (نافذة عائمة)",
        "android.permission.RECEIVE_BOOT_COMPLETED" to "بدء تشغيل التطبيق تلقائياً عند إقلاع وإعادة تشغيل الهاتف",
        "android.permission.REQUEST_INSTALL_PACKAGES" to "طلب تثبيت تطبيقات وحزم APK خارجية غير معروفة",
        "android.permission.REQUEST_DELETE_PACKAGES" to "طلب حذف وإلغاء تثبيت التطبيقات",
        "android.permission.QUERY_ALL_PACKAGES" to "فحص وقراءة جميع التطبيقات المثبتة في الهاتف (Android 11+)",
        "android.permission.VIBRATE" to "التحكم في محرك الاهتزاز وهزاز الهاتف",
        "android.permission.SCHEDULE_EXACT_ALARM" to "جدولة المنبهات الدقيقة في الوقت المحدد (Android 12+)",
        "android.permission.USE_EXACT_ALARM" to "استخدام المنبهات الدقيقة للمنبه والتقويم دون إذن يدوي",
        "android.permission.ACCESS_NOTIFICATION_POLICY" to "التحكم في وضع عدم الإزعاج وتنبيهات الإشعارات",
        "android.permission.KILL_BACKGROUND_PROCESSES" to "إيقاف وإنهاء العمليات والتطبيقات في الخلفية",
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" to "طلب استثناء التطبيق من تحسينات توفير طاقة البطارية",
        "android.permission.PACKAGE_USAGE_STATS" to "الوصول لإحصائيات وقت استخدام التطبيقات والشاشة",
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to "ربط خدمة إمكانية الوصول والتفاعل لذوي الاحتياجات",
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE" to "ربط خدمة قراءة والتقاط إشعارات النظام الواردة",
        "android.permission.BIND_DEVICE_ADMIN" to "تفعيل صلاحية مدير الجهاز وقفل الشاشة",
        "android.permission.INSTALL_SHORTCUT" to "تثبيت اختصارات التطبيق على الشاشة الرئيسية",
        "android.permission.UNINSTALL_SHORTCUT" to "حذف الاختصارات من الشاشة الرئيسية",
        "android.permission.SET_WALLPAPER" to "تعيين وضبط خلفية الشاشة",
        "android.permission.SET_WALLPAPER_HINTS" to "تعيين تلميحات أبعاد خلفية الشاشة",
        "android.permission.EXPAND_STATUS_BAR" to "توسيع وطي شريط الإشعارات والحالة برمجياً",
        "android.permission.REORDER_TASKS" to "إعادة ترتيب مهام ونوافذ التطبيقات",
        "android.permission.TRANSMIT_IR" to "إرسال إشارات الأشعة تحت الحمراء IR Remote",
        "android.permission.FLASHLIGHT" to "التحكم في كشاف وفلاش الهاتف",
        "android.permission.BATTERY_STATS" to "قراءة تفاصيل وإحصائيات استهلاك البطارية",
        "android.permission.DUMP" to "استخراج بيانات تصحيح الأخطاء من خدمات النظام",
        "android.permission.READ_SYNC_SETTINGS" to "قراءة إعدادات مزامنة الحسابات",
        "android.permission.WRITE_SYNC_SETTINGS" to "تعديل إعدادات مزامنة الحسابات",
        "android.permission.AUTHENTICATE_ACCOUNTS" to "العمل كموثق حسابات لحساب جديد في النظام",
        "android.permission.MANAGE_ACCOUNTS" to "إدارة وتعديل قائمة الحسابات المسجلة",
        "android.permission.USE_CREDENTIALS" to "طلب رموز المصادقة والاعتماد للحسابات المسجلة",
    )

    private val MANIFEST_XML_TAGS = listOf(
        Triple("manifest", "<manifest package=\"\" xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    \n</manifest>", "الجذر الرئيسي لملف AndroidManifest.xml"),
        Triple("application", "<application\n    android:allowBackup=\"true\"\n    android:icon=\"@mipmap/ic_launcher\"\n    android:label=\"@string/app_name\"\n    android:supportsRtl=\"true\"\n    android:theme=\"@style/AppTheme\">\n    \n</application>", "وسم تطبيق أندرويد الرئيسي"),
        Triple("activity", "<activity\n    android:name=\".\"\n    android:exported=\"false\" />", "تعريف صفحة Activity"),
        Triple("service", "<service\n    android:name=\".\"\n    android:exported=\"false\" />", "تعريف خدمة Service"),
        Triple("receiver", "<receiver\n    android:name=\".\"\n    android:exported=\"false\">\n</receiver>", "مستقبل بث BroadcastReceiver"),
        Triple("provider", "<provider\n    android:name=\"androidx.core.content.FileProvider\"\n    android:authorities=\"\${applicationId}.fileprovider\"\n    android:exported=\"false\"\n    android:grantUriPermissions=\"true\">\n</provider>", "مزود محتوى ContentProvider"),
        Triple("uses-permission", "<uses-permission android:name=\"android.permission.\" />", "طلب إذن أو صلاحية"),
        Triple("uses-feature", "<uses-feature android:name=\"android.hardware.camera\" android:required=\"false\" />", "طلب متطلب عتادي أو تقني"),
        Triple("intent-filter", "<intent-filter>\n    <action android:name=\"android.intent.action.MAIN\" />\n    <category android:name=\"android.intent.category.LAUNCHER\" />\n</intent-filter>", "مرشح نوايا Intent Filter"),
        Triple("action", "<action android:name=\"android.intent.action.VIEW\" />", "إجراء Intent Action"),
        Triple("category", "<category android:name=\"android.intent.category.DEFAULT\" />", "فئة Intent Category"),
        Triple("data", "<data android:scheme=\"https\" android:host=\"\" />", "بيانات Intent Filter"),
        Triple("meta-data", "<meta-data android:name=\"\" android:value=\"\" />", "بيانات وصفية Meta-Data"),
    )

    private val LAYOUT_XML_TAGS = listOf(
        Triple("LinearLayout", "<LinearLayout\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"wrap_content\"\n    android:orientation=\"vertical\">\n    \n</LinearLayout>", "تخطيط خطي رأسي أو أفقي"),
        Triple("RelativeLayout", "<RelativeLayout\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n    \n</RelativeLayout>", "تخطيط نسبي حسب مواضع العناصر"),
        Triple("FrameLayout", "<FrameLayout\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n    \n</FrameLayout>", "إطار تراكبي للعناصر"),
        Triple("ScrollView", "<ScrollView\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n    <LinearLayout\n        android:layout_width=\"match_parent\"\n        android:layout_height=\"wrap_content\"\n        android:orientation=\"vertical\" />\n</ScrollView>", "حاوية تمرير رأسي"),
        Triple("HorizontalScrollView", "<HorizontalScrollView\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"wrap_content\">\n    \n</HorizontalScrollView>", "حاوية تمرير أفقي"),
        Triple("androidx.constraintlayout.widget.ConstraintLayout", "<androidx.constraintlayout.widget.ConstraintLayout\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\">\n    \n</androidx.constraintlayout.widget.ConstraintLayout>", "تخطيط قيود حديث ومرن"),
        Triple("androidx.recyclerview.widget.RecyclerView", "<androidx.recyclerview.widget.RecyclerView\n    android:id=\"@+id/recycler_view\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\" />", "قائمة عناصر قابلة لإعادة الاستخدام"),
        Triple("TextView", "<TextView\n    android:id=\"@+id/text_view\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"Hello World\"\n    android:textSize=\"16sp\" />", "نص توضيحي"),
        Triple("EditText", "<EditText\n    android:id=\"@+id/edit_text\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"wrap_content\"\n    android:hint=\"أدخل النص هنا\"\n    android:inputType=\"text\" />", "حقل إدخال نصوص"),
        Triple("Button", "<Button\n    android:id=\"@+id/btn_action\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"موافق\" />", "زر تفاعلي"),
        Triple("ImageView", "<ImageView\n    android:id=\"@+id/image_view\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:src=\"@drawable/ic_launcher\" />", "عرض صورة"),
        Triple("ProgressBar", "<ProgressBar\n    android:id=\"@+id/progress_bar\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\" />", "شريط ومؤشر تحميل"),
        Triple("CheckBox", "<CheckBox\n    android:id=\"@+id/check_box\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"خيار\" />", "مربع تحديد صح"),
        Triple("Switch", "<androidx.appcompat.widget.SwitchCompat\n    android:id=\"@+id/switch_toggle\"\n    android:layout_width=\"wrap_content\"\n    android:layout_height=\"wrap_content\"\n    android:text=\"تفعيل\" />", "مفتاح تشغيل وتبديل"),
        Triple("com.google.android.material.card.MaterialCardView", "<com.google.android.material.card.MaterialCardView\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"wrap_content\"\n    app:cardCornerRadius=\"16dp\"\n    app:cardElevation=\"4dp\">\n    \n</com.google.android.material.card.MaterialCardView>", "بطاقة Material عصرية"),
    )

    private val COMMON_XML_ATTRIBUTES = listOf(
        Pair("android:id=\"@+id/\"", "المعرف الفريد للعنصر"),
        Pair("android:layout_width=\"match_parent\"", "عرض العنصر ملء الشاشة"),
        Pair("android:layout_width=\"wrap_content\"", "عرض العنصر حسب المحتوى"),
        Pair("android:layout_height=\"match_parent\"", "طول العنصر ملء الشاشة"),
        Pair("android:layout_height=\"wrap_content\"", "طول العنصر حسب المحتوى"),
        Pair("android:text=\"\"", "نص العنصر"),
        Pair("android:hint=\"\"", "تلميح حقل الإدخال"),
        Pair("android:textColor=\"#FFFFFF\"", "لون النص"),
        Pair("android:textSize=\"16sp\"", "حجم الخط"),
        Pair("android:textStyle=\"bold\"", "سمك الخط عريض"),
        Pair("android:gravity=\"center\"", "محاذاة المحتوى الداخلي"),
        Pair("android:layout_gravity=\"center\"", "محاذاة العنصر داخل الحاوية"),
        Pair("android:orientation=\"vertical\"", "اتجاه التخطيط رأسي"),
        Pair("android:orientation=\"horizontal\"", "اتجاه التخطيط أفقي"),
        Pair("android:padding=\"16dp\"", "الهامش الداخلي"),
        Pair("android:layout_margin=\"16dp\"", "الهامش الخارجي"),
        Pair("android:background=\"\"", "خلفية العنصر أو لونه"),
        Pair("android:visibility=\"visible\"", "ظهور العنصر (visible/gone/invisible)"),
        Pair("android:name=\"\"", "اسم العنصر أو الخاصية"),
        Pair("android:exported=\"false\"", "تصدير المكون خارج التطبيق"),
        Pair("android:theme=\"@style/\"", "ثيم أو ستايل العنصر"),
        Pair("android:icon=\"@mipmap/ic_launcher\"", "أيقونة التطبيق أو المكون"),
        Pair("android:label=\"@string/app_name\"", "عنوان التطبيق أو الصفحة"),
        Pair("android:inputType=\"text\"", "نوع لوحة المفاتيح والإدخال"),
        Pair("android:onClick=\"\"", "دالة الضغط المباشرة"),
    )

    internal fun computeXmlCompletions(
        textBeforeCursor: String,
        isManifest: Boolean = false,
    ): List<IdeCodeCompletionSuggestion> {
        val tail = textBeforeCursor.takeLast(80).trimStart()
        val suggestions = mutableListOf<IdeCodeCompletionSuggestion>()

        // 1. Tag completion: e.g. "<", "<uses", "<act", "<Text", "<Linear"
        val tagMatch = Regex("<([A-Za-z0-9_.:-]*)$").find(tail)
        if (tagMatch != null) {
            val query = tagMatch.groupValues[1].lowercase()
            val tagsPool = if (isManifest) MANIFEST_XML_TAGS else (LAYOUT_XML_TAGS + MANIFEST_XML_TAGS)
            tagsPool.forEach { (tag, snippet, doc) ->
                if (query.isEmpty() || tag.lowercase().contains(query)) {
                    suggestions.add(
                        IdeCodeCompletionSuggestion(
                            label = "<$tag>",
                            insertText = snippet.removePrefix("<"),
                            detail = doc,
                            category = IdeAutoCompleteCategory.ANDROID_XML_TAG,
                        ),
                    )
                }
            }
            if (isManifest || query.startsWith("perm") || query.startsWith("uses")) {
                ALL_ANDROID_PERMISSIONS.forEach { (perm, desc) ->
                    val shortPerm = perm.substringAfterLast('.')
                    if (query.isEmpty() || shortPerm.lowercase().contains(query) || perm.lowercase().contains(query)) {
                        suggestions.add(
                            IdeCodeCompletionSuggestion(
                                label = "uses-permission • $shortPerm",
                                insertText = "uses-permission android:name=\"$perm\" />",
                                detail = desc,
                                category = IdeAutoCompleteCategory.ANDROID_PERMISSION,
                            ),
                        )
                    }
                }
            }
            return suggestions.take(15)
        }

        // 2. Permission completion inside android:name="..."
        val permMatch = Regex("(?:android:name=|permission:?)\\s*\"([A-Za-z0-9_.]*)$").find(tail)
        if (permMatch != null) {
            val query = permMatch.groupValues[1].lowercase()
            ALL_ANDROID_PERMISSIONS.forEach { (perm, desc) ->
                val shortPerm = perm.substringAfterLast('.')
                if (query.isEmpty() || perm.lowercase().contains(query) || shortPerm.lowercase().contains(query)) {
                    suggestions.add(
                        IdeCodeCompletionSuggestion(
                            label = shortPerm,
                            insertText = perm,
                            detail = desc,
                            category = IdeAutoCompleteCategory.ANDROID_PERMISSION,
                        ),
                    )
                }
            }
            return suggestions.take(15)
        }

        // 3. Attribute completion: e.g. "android:", "layout_", "app:"
        val attrMatch = Regex("([a-zA-Z0-9_:]+)$").find(tail)
        val query = attrMatch?.groupValues?.get(1)?.lowercase().orEmpty()
        COMMON_XML_ATTRIBUTES.forEach { (attr, desc) ->
            if (query.isEmpty() || attr.lowercase().contains(query)) {
                val insert = if (query.isNotEmpty() && attr.startsWith(query, ignoreCase = true)) {
                    attr.drop(query.length)
                } else {
                    attr
                }
                suggestions.add(
                    IdeCodeCompletionSuggestion(
                        label = attr.substringBefore('='),
                        insertText = insert,
                        detail = desc,
                        category = IdeAutoCompleteCategory.ANDROID_XML_ATTR,
                    ),
                )
            }
        }

        return suggestions.take(15)
    }
}
