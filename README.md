<div align="center">

<img src="docs/banner.png" alt="IDE Ultimate" width="100%" />

# IDE Ultimate

**بيئة تطوير متكاملة أنيقة لنظام Android — مبنية بالكامل بـ Kotlin و Jetpack Compose.**

[![Build APK](https://github.com/AndroidEXL/IDE-Ultimate/actions/workflows/build-apk.yml/badge.svg)](https://github.com/AndroidEXL/IDE-Ultimate/actions/workflows/build-apk.yml)
[![Latest APK](https://img.shields.io/github/v/release/AndroidEXL/IDE-Ultimate?label=APK&color=7C3AED)](https://github.com/AndroidEXL/IDE-Ultimate/releases/latest)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.04-4285F4?logo=jetpackcompose&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)

</div>

---

## ✨ نظرة سريعة

| الافتتاحية (Splash) | مساحة العمل | تفاصيل المشروع |
| :---: | :---: | :---: |
| <img src="docs/splash_preview.png" width="240" /> | <img src="docs/workspace_preview.png" width="240" /> | <img src="docs/project_detail_preview.png" width="240" /> |

مشروع Android كامل بواجهة عربية (RTL) وثيم داكن فضائي، يجمع بين:

- 🌌 **شاشة Splash سينمائية** — سُحب سديمية متدرّجة، حقل نجوم متلألئ مع شهاب عابر، شعار سداسي يدور ببطء، ظهور متتابع للنصوص، وشريط تقدّم متدرّج مع نسبة مئوية تتغيّر حسب المرحلة.
- 🎬 **حركات سلسة** — كلها مبنية على `Animatable` و`rememberInfiniteTransition` و`AnimatedVisibility`، وتُشتغل داخل طور الرسم (draw phase) دون أي مكتبات خارجية.
- 🎨 **أيقونة متكيّفة (Adaptive Icon)** — تعمل على Android 8+ مع نسخة **Monochrome** لأندرويد 13+، وبدائل PNG للأجهزة الأقدم.
- 📱 **دعم كامل للعربية** — خط **Almarai** عربي مضمّن، تخطيط RTL تلقائي، ونصوص عربية/إنجليزية منفصلة.
- ⚡ **شاشة رئيسية Dashboard** — بطاقات زجاجية، معاينة كود ملوّنة، وزر لإعادة تشغيل الافتتاحية.
- 🧭 **شريط تنقّل سفلي** — **الرئيسية** و**مساحة العمل** على بعد ضغطة واحدة من أي مكان.
- 🗂️ **مساحة عمل كاملة** — مجلدات ومشاريع، إنشاء عبر نافذة سفلية، تعديل الاسم/الحذف بالضغط المطوّل، وتصدير المشروع **ZIP** جاهز لـ Android Studio.
- 🤖 **بناء APK تلقائي** على GitHub Actions مع رفع الناتج كـ Artifact وإصدار GitHub Release عند إنشاء Tag.

---

## 📲 كيف أحصل على ملف APK؟

**أسرع طريقة:** حمّل أحدث إصدار جاهز من صفحة
[**Releases**](https://github.com/AndroidEXL/IDE-Ultimate/releases/latest) — بدون تسجيل دخول:

```
IDE-Ultimate-1.6.0-release.apk   ← للاستخدام اليومي
IDE-Ultimate-1.6.0-debug.apk     ← للتصحيح والتجارب
```

**أو من خلال Actions:**

1. افتح تبويب **Actions** في المستودع واختر **Build APK**.
2. اضغط **Run workflow** (أو انتظر تشغيله تلقائيًا بعد أي Push).
3. بعد نجاح المهمة، حمّل الملف من قسم **Artifacts** باسم `IDE-Ultimate-APK`
   (يتطلب تسجيل الدخول إلى GitHub):
   - `IDE-Ultimate-1.6.0-debug.apk`
   - `IDE-Ultimate-1.6.0-release.apk` (مُصغّر بـ R8 ومُوقّع بمفتاح العرض الثابت)
   - `checksums.txt`
   - تقرير اختبارات الوحدة (`unit-test-report`)

> ⚠️ كل النسخ (debug و release) موقّعة بمفتاح العرض الثابت `keystore/ide-ultimate.p12`،
> لذا يمكن تثبيت أي إصدار جديد فوق القديم مباشرة.
>
> **إن كنت ثبّت نسخة أقدم من v1.4.0:** احذف التطبيق القديم مرة واحدة ثم ثبّت الجديد، لأن النسخ
> السابقة كانت تُوقّع بمفتاح debug مؤقت مختلف في كل بناء من GitHub Actions.
>
> للنشر على Google Play أنشئ Keystore خاصًا ومرّره عبر `IEU_STORE_FILE` وأخواتها
> (انظر [`keystore/README.md`](keystore/README.md)).

لإنشاء **Release** تلقائي مع ملفات APK مرفقة:

```bash
git tag v1.6.0
git push origin v1.6.0
```

---

## 🗂️ صفحة مساحة العمل

<img align="right" src="docs/workspace_sheet_preview.png" width="300" />

تفتح من **شريط التنقّل السفلي** (تبويب مساحة العمل) أو من زر **ابدأ الآن** في الرئيسية، وتعمل بنمط متصفح الملفات:

| الإجراء | النتيجة |
| --- | --- |
| زر **+** (FAB) | نافذة سفلية: **مجلد جديد** أو **مشروع جديد** في الموقع الحالي |
| الضغط على مجلد | فتح المجلد وعرض محتواه + Breadcrumbs للرجوع |
| الضغط على مشروع | **فتح صفحة المشروع**: اسم الحزمة، الملفات المضمّنة، وأزرار تصدير/تعديل/حذف/إضافة |
| الضغط المطوّل على مشروع | نافذة سفلية: تعديل الاسم · تصدير ZIP · إنشاء مشروع جديد · حذف |
| **ضغط مطوّل** على مجلد | نافذة سفلية: تعديل الاسم · حذف (مع تحذير بعدد العناصر داخله) |
| زر **تخطي/رجوع** داخل المشروع | يعود إلى شبكة المجلد والمشاريع |
| زر الرجوع | خطوة للخلف داخل المجلدات، ثم العودة للرئيسية |

تفاصيل تقنية:

- **مجلدات ومشاريع** بشكل مربّع (بطاقات `aspectRatio(1f)`) مع أيقونة مجلد مرسومة بـ `Canvas`.
- **حفظ دائم** في `SharedPreferences` عبر `WorkspaceRepository` (شجرة `parentId`) — تبقى بعد إغلاق التطبيق.
- **تحقق من الأسماء**: لا يُسمح باسم فارغ أو مكرر داخل نفس المجلد (حتى مع اختلاف حالة الأحرف).
- **حذف آمن**: حذف المجلد يشمل كل ما بداخله، مع نافذة تأكيد تعرض عدد العناصر المتأثرة.
- **صفحة تفاصيل المشروع**: تُفتح بالضغط على المشروع وتعرض اسم الحزمة (`applicationId`)،
  قائمة الملفات السبع التي ستُضمَّن في الأرشيف، وأزرار **تصدير ZIP**، **تعديل الاسم**، **حذف**، و**إنشاء مشروع جديد**.
- **تصدير ZIP حقيقي** (`ProjectZipExporter`): ملف قابل للفتح مباشرة في Android Studio يحتوي
  `settings.gradle.kts`، `build.gradle.kts`، `app/build.gradle.kts`، `AndroidManifest.xml`،
  `MainActivity.kt` (Compose)، الموارد، و`README.md` — مع اسم حزمة مشتق من اسم المشروع.
- يُستخدم **SAF** (`ActivityResultContracts.CreateDocument`) فيختار المستخدم مكان الحفظ دون أي صلاحيات تخزين.

<br clear="right" />

---

## 🎞️ تسلسل الحركة في الافتتاحية

| الزمن | الحدث |
| --- | --- |
| 0.00s | ظهور الخلفية مع تكبير بطيء للسديم (Ken Burns) خلال 7 ثوانٍ |
| 0.16s | ظهور اسم التطبيق بتدرّج لوني وانزلاق ناعم |
| 0.29s | ظهور العبارة التعريفية |
| 0.42s | ظهور شريط التقدّم + حالة التحميل المتغيّرة (تهيئة → Kotlin → معاينة) |
| 0.42–2.72s | امتلاء الشريط من اليمين إلى اليسار (RTL) بنسبة متزايدة |
| 3.06s | تلاشٍ + تكبير طفيف للمحتوى، ثم الانتقال إلى الواجهة الرئيسية |

كل الحركة تُنفَّذ على الـ UI thread دون أي مكتبات أنيميشن خارجية، ويمكن تخطّيها
بالضغط على زر **تخطي** أو زر الرجوع.

---

## 🧱 البنية

```
IDE-Ultimate/
├─ app/src/main/
│  ├─ java/com/androidexl/ideultimate/
│  │  ├─ MainActivity.kt                # توجيه Splash → Home → Workspace
│  │  ├─ data/
│  │  │  ├─ WorkspaceRepository.kt      # شجرة المجلدات/المشاريع + حفظ دائم
│  │  │  └─ ProjectZipExporter.kt       # توليد مشروع Gradle كامل داخل ZIP
│  │  └─ ui/
│  │     ├─ components/                 # LogoMark, Starfield, GlassCard, AppBottomBar
│  │     ├─ screens/SplashScreen.kt     # الافتتاحية المتحركة
│  │     ├─ screens/HomeScreen.kt       # الواجهة الرئيسية
│  │     ├─ screens/WorkspaceScreen.kt  # المتصفح + النوافذ السفلية
│  │     ├─ screens/ProjectDetailScreen.kt # صفحة المشروع وملفاته
│  │     └─ theme/                      # الألوان، الخطوط، Typography
│  ├─ res/
│  │  ├─ drawable/                    # شعارات Vector + AnimatedVector
│  │  ├─ drawable-nodpi/              # خلفية السديم (WebP)
│  │  ├─ font/                        # Almarai + JetBrains Mono
│  │  ├─ mipmap-*                     # أيقونات المتجر والأجهزة القديمة
│  │  └─ values*/                     # الثيمات والنصوص (ar/en)
│  └─ AndroidManifest.xml
├─ app/src/test/                      # اختبارات وحدة لتصدير ZIP
├─ keystore/ide-ultimate.p12          # مفتاح العرض الثابت (debug + release)
├─ design/nebula_source.png           # الخلفية الأصلية بدقة عالية
├─ docs/                              # صور المعاينة والبانر
├─ tools/generate_assets.py           # توليد كل الأصول النقطية آليًا
└─ .github/workflows/build-apk.yml    # CI لبناء APK + تشغيل الاختبارات
```

---

## 🚀 البناء محليًا

```bash
# المتطلبات: JDK 17 + Android SDK (compileSdk 35)
./gradlew assembleDebug           # APK للتجربة
./gradlew assembleRelease         # APK مُصغّر
./gradlew testDebugUnitTest       # اختبارات تصدير ZIP
```

المخرجات:

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

أو افتح المجلد مباشرة في **Android Studio** (Ladybug أو أحدث).

---

## ⚙️ التقنيات والإصدارات

| العنصر | الإصدار |
| --- | --- |
| Android Gradle Plugin | 8.9.2 |
| Kotlin + Compose Compiler Plugin | 2.1.20 |
| Gradle | 8.13 |
| Compose BOM | 2025.04.01 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 (Android 7.0) |
| Java | 17 |

المكتبات: `core-ktx`، `core-splashscreen`، `activity-compose`، `lifecycle-runtime-ktx`، `material3`، `material-icons-core`، `ui-tooling-preview`.

---

## 🎨 توليد الأصول

كل الصور النقطية (الخلفية، أيقونات mipmap، صور المعاينة) تُولَّد بسكربت واحد:

```bash
python -m venv .venv && source .venv/bin/activate
pip install -r tools/requirements.txt
python tools/generate_assets.py
```

- الشعار في الأيقونة وواجهة Splash مرسوم بـ **Vector Drawable** (نسخة Android) وبـ **Pillow** (نسخ PNG) بنفس الإحداثيات، لذا يبقى حادًا على كل الكثافات.
- النصوص العربية في الصور تُشكَّل عبر **HarfBuzz** وتُرسم بـ **FreeType** مع ترتيب ثنائي الاتجاه (RTL/LTR) مخصص، فتخرج متصلة وبترتيب صحيح.
- لتغيير الخلفية: استبدل `design/nebula_source.png` ثم أعد تشغيل السكربت.

---

## 🧩 تخصيص سريع

| ما تريد تغييره | المكان |
| --- | --- |
| مدة شاشة Splash | `SplashScreen.kt` → `progress.animateTo(..., tween(2300))` |
| الألوان | `ui/theme/Color.kt` |
| نصوص الافتتاحية | `res/values/strings.xml` و `res/values-en/strings.xml` |
| الشعار | `drawable/ic_splash_logo.xml` + `drawable/ic_launcher_foreground.xml` |
| شاشة النظام (Android 12+) | `res/values-v31/themes.xml` |
| مفتاح التوقيع | `keystore/ide-ultimate.p12` (انظر `keystore/README.md`) |
| محتوى البذرة الأولي | `WorkspaceRepository.seedDemoContent()` |
| محتوى مشروع ZIP | `ProjectZipExporter` |

---

## 🇬🇧 English

**IDE Ultimate** is a Kotlin/Jetpack Compose Android sample with a cinematic splash
experience: a nebula backdrop, a twinkling star field with an occasional shooting star,
a slowly rotating code-hexagon logo, staged text reveals, and a gradient loader with a
live status readout. It ships with a full adaptive launcher icon (including a monochrome
layer for Android 13+), RTL-first Arabic UI, and a GitHub Actions workflow that builds
debug + release APKs and uploads them as run artifacts. Version 1.4 pins a single committed signing key so every APK can be installed over the
previous one, adds a Workspace shortcut in the home header, and verifies in CI that the
debug and release builds share one certificate. Version 1.3 adds a bottom navigation bar that puts the Workspace page one tap away from
the home screen, and a Workspace page
with a folder/project grid, FAB bottom sheets, long-press rename/delete, a project detail page
opened on tap, and real ZIP export of a ready-to-open Gradle project (covered by JVM unit tests).

```bash
./gradlew assembleDebug
```

---

## 📄 License

MIT — see [LICENSE](LICENSE).
