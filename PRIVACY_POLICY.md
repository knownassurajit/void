# Privacy Policy for VOID

**Effective Date:** August 13, 2026  
**Last Updated:** August 13, 2026

## 1. Overview
VOID ("the Application") is a minimalist Android launcher with on-device AI notification summaries, developed by Surajit Das ("we", "us", or "our"). We believe privacy is a fundamental right. VOID is open-source (GPLv3) and designed around zero data collection.

## 2. Notification Data & On-Device AI Summarization
- **Notification Access:** If granted permission, VOID Launcher accesses incoming system notifications solely to display them on your home screen and generate AI summaries.
- **Strict On-Device Processing:** AI summarization is performed **100% locally on your device** using Android AICore / Gemini Nano (or local text parsing fallbacks).
- **No Cloud Uploads:** Notification titles, text, and contact names are **never** transmitted off your device or sent to any server.

## 3. Device Permissions & Purpose
- **`BIND_NOTIFICATION_LISTENER_SERVICE`**: Required to display and summarize notification cards on the notification drawer screen.
- **`QUERY_ALL_PACKAGES`**: Required to list installed applications in the app drawer and launch them upon request.
- **`PACKAGE_USAGE_STATS`** (Optional): Used strictly to display digital wellbeing screen time and unlock counters directly on your home screen.

## 4. Local Storage
User settings, pinned home screen apps, and quick notes are saved locally in private app `SharedPreferences`.

## 5. Third-Party Services, Tracking & Ads
- **No Tracking or Telemetry:** No analytics tools (e.g. Firebase Analytics) or telemetry services are included.
- **No Advertisements:** VOID Launcher is completely free of ads.

## 6. Open Source Transparency
The source code of VOID Launcher is published under the GNU General Public License v3.0, allowing complete public auditability of data safety practices.

## 7. Children's Privacy
VOID Launcher does not collect any data from any users, including children under 13.

## 8. Contact Us
For any questions regarding VOID Launcher or this Privacy Policy:
- **Developer:** Surajit Das
- **Email:** isurajit123@gmail.com
- **GitHub Repository:** [https://github.com/knownassurajit/void](https://github.com/knownassurajit/void)
