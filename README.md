# BD AutoPay SMS Bridge (Custom Android App)

আপনার `elmabd.com` WordPress অটো পেমেন্ট গেটওয়ের জন্য সম্পূর্ণ ফ্রি, অ্যাড-ফ্রি ও আনলিমিটেড কাস্টম Android SMS Forwarder অ্যাপ।

## বৈশিষ্ট্যসমূহ
- কোনো অ্যাড, সাবস্ক্রিপশন বা টাইম লিমিট নেই।
- bKash, NAGAD, Rocket (`16216`), Upay-এর SMS অটোমেটিক ফিল্টার করে আপনার WordPress Webhook-এ পাঠায়।
- নেটওয়ার্ক সাময়িক দুর্বল থাকলে অটোমেটিক ৩ বার রিট্রাই করে।
- অ্যাপের ভেতরেই **টেস্ট SMS পাঠান** বাটন এবং সাম্প্রতিক ডেলিভারি লগ দেখার সুবিধা।
- ব্যাকগ্রাউন্ডে সচল থাকার জন্য Foreground Service ও সরাসরি Battery Settings শর্টকাট।

## ফ্রি অনলাইনে `.apk` বানানোর নিয়ম (পিসিতে Android Studio ছাড়াই)
1. [GitHub.com](https://github.com)-এ একটি ফ্রি অ্যাকাউন্টে লগইন করে **New Repository** তৈরি করুন (যেমন: `bd-autopay-sms-app`, চাইলে Private রাখুন)।
2. এই জিপ ফাইলটি আনজিপ করে ভেতরের সব ফাইল ও ফোল্ডার (বিশেষ করে `.github/workflows/build-apk.yml` সহ) ওই রিপোজিটরিতে আপলোড (Upload files → Commit) করুন।
3. রিপোজিটরির উপরের **Actions** ট্যাবে ক্লিক করুন—**Build Android APK** অটোমেটিক চালু হবে (২–৩ মিনিট লাগবে)।
4. বিল্ড সবুজ (✓) হয়ে গেলে ওই রানটিতে ক্লিক করে নিচের **Artifacts** থেকে **`BD-AutoPay-SMS-Bridge-APK`** ডাউনলোড করে আনজিপ করলেই `app-debug.apk` পেয়ে যাবেন!

## ফোনে সেটআপ করার নিয়ম
1. `app-debug.apk` ফোনে ইনস্টল করে **SMS** ও **Notification** পারমিশন দিন।
2. **Webhook URL** ঘরে আপনার WordPress-এর **BD Auto Pay → Webhook ও Android সেটআপ** ট্যাবের পুরো লিংক (API Key সহ) পেস্ট করুন।
3. **Sender Filter** ঘরে ডিফল্ট `bKash,NAGAD,16216,upay` রাখুন এবং **সেভ করুন** বাটনে ট্যাপ করুন।
4. **ব্যাটারি অপটিমাইজেশন বন্ধ করুন** বাটনে ট্যাপ করে অ্যাপটিকে **Unrestricted** করে দিন।
5. **টেস্ট SMS পাঠান** বাটনে ট্যাপ করে নিশ্চিত হোন ড্যাশবোর্ডে টেস্ট SMS পৌঁছাচ্ছে।
