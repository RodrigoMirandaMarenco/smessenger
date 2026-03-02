# Project Initiation Document (PID): SMS Bridge

## 1. Project Overview
**Project Name:** SMS Bridge (Secure Expat Forwarder)  
**Project Lead:** User (Expat / Home Lab Enthusiast)  
**Primary Goal:** To create a private, native Android application that intercepts incoming SMS messages (specifically 2FA and banking alerts) on a remote "host" device and forwards them to a configured email address.

---

## 2. Problem Statement
Living abroad often disconnects users from their home-country mobile numbers, which are required for banking 2FA. Existing third-party solutions introduce privacy risks and potential points of failure. This project aims to provide a **sovereign, low-cost, and high-reliability** bridge for sensitive communications.

---

## 3. Project Scope

### **In-Scope (Functional Requirements)**
* **SMS Interception:** Automated reading of incoming SMS via `BroadcastReceiver`.
* **Background Persistence:** Implementation of a `Foreground Service` to ensure the app remains active 24/7.
* **Email Relay:** Direct SMTP integration to forward SMS content to a user-defined email.
* **Device Autonomy:** Automated start-on-boot logic (`RECEIVE_BOOT_COMPLETED`).
* **Configuration UI:** Simple Jetpack Compose UI for SMTP settings and destination email.

### **Out-of-Scope**
* **Google Play Store Compliance:** No adherence to Play Store SMS permission restrictions (Sideloading only).
* **Bi-directional Messaging:** No capability to send/reply to SMS from the email client.
* **Cloud Infrastructure:** No external servers, databases, or analytics.

---

## 4. Technical Strategy
The following stack and architectural patterns are mandated for this project:

* **Language:** Kotlin (Native)
* **UI:** Jetpack Compose (Minimalist)
* **Concurrency:** Kotlin Coroutines
* **Data Persistence:** Jetpack DataStore (for settings/credentials)
* **Reliability Mechanism:**
    * `Foreground Service` with a persistent notification.
    * `WAKE_LOCK` for processing during Doze mode.
    * Requesting "Ignore Battery Optimizations" via System Intent.

---

## 5. Success Metrics
* **Zero-Loss Delivery:** 100% of SMS received by the device must be forwarded to the email relay.
* **Latency:** Forwarding must occur within **60 seconds** of SMS receipt.
* **Uptime:** The app must survive device reboots and aggressive OS memory management without manual user intervention.

---

## 6. Constraints & Risks

| Category | Constraint / Risk | Mitigation |
| :--- | :--- | :--- |
| **Budget** | Minimal to zero running costs. | Use existing email provider (SMTP) and local device hardware. |
| **Security** | Sensitive 2FA data transit. | Local-only processing; no third-party SDKs; encrypted local storage for SMTP credentials. |
| **Stability** | Android OS killing background processes. | Use Foreground Service + manual battery optimization whitelisting. |
| **Reliability** | Internet outage on the host device. | (Future Phase) Implement local queuing/retry logic. |

---

## 7. Next Steps for Agentic AI
1. **Requirement Analysis:** Review the `AndroidManifest.xml` requirements for high-priority background tasks.
2. **Architecture Design:** Propose a class structure for the `SmsReceiver` -> `ForwardingService` -> `EmailProvider` pipeline.
3. **Environment Setup:** Initialize a standard Kotlin Android project using the latest Gradle/BOM versions.
