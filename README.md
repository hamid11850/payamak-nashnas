# پیامک ناشناس

اپلیکیشن اندرویدی فارسی و راست‌چین برای ارسال **پیام ناشناس درون‌برنامه‌ای** بین کاربران عضو.

> **معنای «ناشناس» در این پروژه:** ناشناس در برابر **دریافت‌کننده**، نه در برابر مدیر سامانه.
> این برنامه شماره فرستنده را جعل نمی‌کند و از خط مخفی برای ارسال SMS واقعی استفاده نمی‌کند.
> پیام‌ها فقط بین کاربران ثبت‌نام‌شده داخل خود برنامه جابه‌جا می‌شوند و شماره موبایل هیچ کاربری برای کاربر دیگر نمایش داده نمی‌شود.

---

## ۱. وضعیت فعلی پروژه (صادقانه)

| بخش | وضعیت |
|---|---|
| سورس کامل Kotlin + Jetpack Compose (۱۱ صفحه) | ✅ نوشته و کامپایل شده |
| قوانین امنیتی Firestore (رضایت، مسدودسازی، سقف ارسال) | ✅ نوشته شده |
| Workflow ساخت APK در GitHub Actions | ✅ نوشته شده — **اجرا نشده** (نیازمند مخزن شما) |
| اتصال Firebase با `google-services.json` واقعی | ⛔ **انجام نشده** — فایل ساختگی ساخته نمی‌شود؛ باید از کنسول خودتان دانلود شود |
| ارسال واقعی پیامک احراز هویت | ⛔ تست نشده — نیازمند پروژه Firebase با شماره تأییدشده |
| انتشار در کافه‌بازار | ⛔ انجام نشده — نیازمند کلید امضا و حساب ناشر شما |
| APK نصب‌شدنی روی گوشی | 🟡 در این محیط ساخته شد (دیباگ، **حالت نمایشی**، بدون Firebase). با تزریق `google-services.json` همان کد به نسخه واقعی وصل می‌شود |

**حالت نمایشی:** تا زمانی که `app/google-services.json` وجود نداشته باشد، برنامه با دادهٔ محلی (`DemoRepo`) اجرا می‌شود؛ ورود با هر شماره‌ای و کد ثابت `123456` انجام می‌شود و پیام‌ها با بستن برنامه پاک می‌شوند. این حالت فقط برای بررسی ظاهر و جریان صفحات است و برای انتشار نیست.

---

## ۲. صفحات برنامه

| # | صفحه | فایل |
|---|---|---|
| ۱ | ورود و ثبت‌نام (شماره → کد پیامکی → نام مستعار) | `ui/auth/AuthScreen.kt` |
| ۲ | صفحه اصلی | `ui/home/HomeScreen.kt` |
| ۳ | ارسال پیام | `ui/send/ComposeMessageScreen.kt` |
| ۴ | جست‌وجوی کاربران | `ui/search/SearchScreen.kt` |
| ۵ | صندوق پیام‌های دریافتی | `ui/inbox/InboxScreen.kt` |
| ۶ | گفت‌وگو | `ui/chat/ChatScreen.kt` |
| ۷ | پیام‌های ارسال‌شده | `ui/sent/SentScreen.kt` |
| ۸ | تنظیمات حریم خصوصی | `ui/settings/SettingsScreen.kt` |
| ۹ | مسدودسازی کاربران | `ui/blocked/BlockedScreen.kt` |
| ۱۰ | گزارش تخلف | `ui/report/ReportScreen.kt` |
| ۱۱ | پروفایل و تغییر نام مستعار | `ui/profile/ProfileScreen.kt` |

طراحی: تم تیرهٔ «مرکب + طلایی` (Ink `#07090D` / Gold `#D4AF6A`)، گوشه‌های گرد ۱۶–۲۸dp، فونت **وزیرمتن** جاسازی‌شده، انیمیشن‌های `AnimatedContent`، راست‌چین سراسری با `LocalLayoutDirection = Rtl`.

---

## ۳. مراحل راه‌اندازی (به ترتیب)

### گام ۱ — ساخت پروژه Firebase
1. به <https://console.firebase.google.com> بروید و **Add project** بزنید.
2. از منوی چپ **Build → Authentication → Get started** و تب **Sign-in method** گزینهٔ **Phone** را فعال کنید.
3. **Build → Firestore Database → Create database** (حالت Production).
4. ⚠️ برای ارسال واقعی SMS، در Authentication → Settings → **Phone numbers for testing** یک/چند شماره و کد ثابت اضافه کنید، یا پروژه را روی **Blaze** ببرید. طرح رایگان (Spark) سهمیهٔ محدودی برای SMS دارد.

### گام ۲ — ثبت اپ اندروید و دریافت `google-services.json`
1. در **Project settings → Your apps → Add app → Android**.
2. **Package name** را دقیقاً این بگذارید: `com.payamaknashnas.app`
3. **SHA-1** و **SHA-256** را اضافه کنید (برای Phone Auth روی دستگاه‌های واقعی لازم است):
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey \
     -storepass android -keypass android
   ```
   برای نسخهٔ انتشار، SHA-1 کلید Release خودتان را هم اضافه کنید.
4. فایل `google-services.json` را دانلود و در مسیر `app/google-services.json` قرار دهید.

> این تنها فایلی است که من نمی‌توانم بسازم؛ ساختگی بودن آن باعث خطای `API key not valid` یا نصب‌نشدن اپ می‌شود.

### گام ۳ — انتشار قوانین امنیتی
```bash
npm i -g firebase-tools
firebase login
firebase use --add            # پروژه خودتان را انتخاب کنید
firebase deploy --only firestore:rules,firestore:indexes
```
فایل‌های `firestore.rules` و `firestore.indexes.json` در ریشهٔ پروژه هستند.

### گام ۴ — ساخت APK در GitHub Actions
1. مخزن را بسازید و کد را push کنید:
   ```bash
   git init && git add . && git commit -m "پیامک ناشناس: نسخه اولیه"
   git branch -M main
   git remote add origin https://github.com/USERNAME/payamak-nashnas.git
   git push -u origin main
   ```
2. در GitHub → **Settings → Secrets and variables → Actions** این Secretها را بسازید:

   | نام Secret | مقدار |
   |---|---|
   | `GOOGLE_SERVICES_JSON_BASE64` | خروجی `base64 -w0 app/google-services.json` |
   | `KEYSTORE_BASE64` | خروجی `base64 -w0 release.jks` |
   | `KEYSTORE_PASSWORD` | رمز keystore |
   | `KEY_ALIAS` | نام alias کلید |
   | `KEY_PASSWORD` | رمز کلید |

3. ساخت کلید امضا (یک‌بار برای همیشه، فایل را گم نکنید):
   ```bash
   keytool -genkeypair -v -keystore release.jks -alias payamak \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
4. تب **Actions** → workflow «Build Release APK» → **Run workflow**. APK در بخش **Artifacts** پایین صفحهٔ اجرا قابل دانلود است.

### گام ۵ — انتشار در کافه‌بازار
- ورود به <https://pishkhan.cafebazaar.ir>، ساخت اپ با نام «پیامک ناشناس»، دستهٔ «اجتماعی» یا «ارتباطات».
- آپلود همان APK امضاشدهٔ Release، آیکون ۵۱۲×۵۱۲، اسکرین‌شات‌ها و توضیحات فارسی.
- در توضیحات صریح بنویسید: «پیام‌ها فقط بین کاربران ثبت‌نام‌شدهٔ همین برنامه رد و بدل می‌شوند و جایگزین پیامک معمولی نیستند» — کافه‌بازار به اپ‌هایی که ادعای جعل شماره دارند حساس است.

---

## ۴. مدل داده در Firestore

```
users/{uid}            → uid, alias, aliasLower, allowMessages, bio, createdAt, colorIndex, banned, lastSeen
private/{uid}          → phone, updatedAt            (فقط صاحب حساب می‌خواند)
messages/{autoId}      → fromUid, fromAlias, toUid, toAlias, text, createdAt,
                         readAt, threadId, replyToId, replyToSnippet,
                         deletedForFrom, deletedForTo
blocks/{blockerUid}__{blockedUid} → blockerUid, blockedUid, blockedAlias, createdAt
rateLimits/{uid}       → windowStart, count
reports/{autoId}       → reporterUid, targetUid, targetAlias, messageId, reason, note, status, createdAt
```

---

## ۵. امنیت — چه چیزی واقعاً سمت سرور اعمال می‌شود؟

قوانین `firestore.rules` (نه رابط کاربری) این‌ها را رد می‌کنند:

1. **فقط کاربر احراز هویت‌شده** می‌تواند پیام بسازد (`request.auth != null`).
2. **رضایت گیرنده:** اگر `users/{toUid}.allowMessages == false` باشد، نوشتن پیام رد می‌شود.
3. **مسدودسازی دوطرفه:** اگر سند `blocks/{toUid}__{fromUid}` وجود داشته باشد (گیرنده فرستنده را مسدود کرده) یا برعکس، پیام رد می‌شود.
4. **سقف ارسال:** اگر `rateLimits/{uid}.count >= 20` و پنجرهٔ ۶۰ ثانیه‌ای منقضی نشده باشد، پیام رد می‌شود.
5. **حریم شماره:** سند `private/{uid}` فقط برای `request.auth.uid == uid` خواندنی است؛ هیچ کاربری به شمارهٔ دیگری دسترسی ندارد.
6. **تغییرناپذیری هویت پیام:** فرستنده نمی‌تواند `fromUid` را جعل کند؛ فقط `readAt` و پرچم‌های حذف نرم قابل تغییرند.
7. **گزارش‌ها** فقط نوشته می‌شوند و از اپ خواندنی نیستند (`allow read: if false`).

### محدودیت‌های صادقانه
- **قوانین Firestore شمارندهٔ اتمیک نیستند:** `rateLimits` توسط خود کلاینت در `runTransaction` افزایش می‌یابد و قانون فقط سقف را بررسی می‌کند. برای سقف **ضدتقلب و قطعی** باید Cloud Functions روی طرح **Blaze** نوشته شود.
- **حذف کامل حساب** نیازمند Cloud Functions است: کلاینت پروفایل و دادهٔ خصوصی را پاک می‌کند و پیام‌ها را «حذف‌شده» علامت می‌زند، اما سند احراز هویت و پیام‌های طرف مقابل از سرور حذف نمی‌شوند. حذف کامل هویت را از کنسول (Authentication → حذف کاربر) یا با Cloud Function انجام دهید.
- **جست‌وجو** جست‌وجوی پیشوندی روی `aliasLower` است (نه جست‌وجوی کامل متن).
- **اعلان‌ها (Push)** پیاده‌سازی نشده‌اند؛ پیام‌ها فقط داخل برنامه دیده می‌شوند.
- **Cloud Functions و اعلان‌ها و اشتراک ویژه** در این نسخه وجود ندارند.

---

## ۶. ساخت محلی

```bash
export ANDROID_HOME=/path/to/android-sdk       # platform 34 + build-tools 34.0.0
./gradlew assembleDebug                         # APK: app/build/outputs/apk/debug/
./gradlew testDebugUnitTest                     # تست‌های واحد
```

نیازمندی‌ها: JDK 17، Android SDK 34، Gradle 8.7 (wrapper موجود است).

بدون `app/google-services.json` پروژه هم کامپایل می‌شود و در **حالت نمایشی** اجرا می‌شود — این طراحی عمدی است تا هر کسی بتواند سورس را بیلد و ظاهر را ببیند.

---

## ۷. ساختار پوشه‌ها

```
app/src/main/java/com/payamaknashnas/app/
├── MainActivity.kt · PayamakApp.kt
├── data/           Models · Repo (اینترفیس) · FirestoreRepo · DemoRepo · AuthApi · PersianDate · AppContainer
└── ui/
    ├── RootScreen.kt · AppState.kt
    ├── theme/Theme.kt
    ├── components/Common.kt
    ├── auth · home · search · send · inbox · chat · sent · settings · blocked · report · profile
app/src/main/res/   font (وزیرمتن) · mipmap (آیکون) · values · xml
firestore.rules · firestore.indexes.json · firebase.json
.github/workflows/build-apk.yml
```

---

## ۸. نقشهٔ راه نسخه‌های بعدی

- Cloud Functions: حذف کامل حساب، سقف ارسال قطعی، شمارش گزارش‌ها و بن خودکار.
- FCM برای اعلان پیام جدید.
- جست‌وجوی بهتر (Typeahead روی Firestore یا Algolia).
- اشتراک ویژه: نشان پروفایل، تم اختصاصی، پیام بلندتر.
- گزارش‌گیری پنل مدیر روی مجموعهٔ `reports`.
