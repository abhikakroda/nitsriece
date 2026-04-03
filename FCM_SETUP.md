# Firebase Cloud Messaging (FCM) Setup & Usage

This document outlines the complete end-to-end setup and usage of FCM for the Timetable app. This covers Android integration and the backend implementation required to send messages via the FCM HTTP v1 API.

## 1. Firebase Console Setup
To use push notifications, you need a Firebase project:
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project or select an existing one.
3. Click the **Android icon** to add a new Android app.
4. Enter the Android package name: `com.example.timetable`.
5. Download the `google-services.json` file.

## 2. Placing `google-services.json`
Move the downloaded `google-services.json` file into the `app/` level directory of your project:
```
/Users/abhishekmeena/AndroidStudioProjects/Timetable/app/google-services.json
```
*(The app is already configured with the Google Services Gradle plugin and Firebase BOM to pick this up automatically during build).*

## 3. Retrieving the Device Token
On app startup, the app automatically fetches the FCM registration token and subscribes the user to the `all` topic. 
You can view this token in your IDE logcat by filtering for `MainActivity`:
```log
D/MainActivity: FCM token: eXy...<your-token>...
D/MainActivity: Subscribed to FCM topic: all
```
*(Note: A `TODO` is left in `AppFirebaseMessagingService.kt:onNewToken()` where you can write logic to send refreshed tokens back to your database).*

## 4. Sending Notifications (Backend / HTTP v1)
FCM now requires OAuth2/Service Account authorization (the legacy Server Key method is deprecated). You must trigger notifications from a protected backend environment using the FCM HTTP v1 API, or simpler, using the Firebase Admin SDK.

**Requirements:**
1. In Firebase Console, go to **Project Settings** -> **Service accounts**.
2. Click **Generate new private key**. Keep this JSON file secure on your server.

### Sending Endpoint (HTTP v1 Example)
URL: `POST https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send`
*(Required Header: `Authorization: Bearer <OAuth2_Token>`)*

## 5. Example Payloads

### 1) Generic Alert
Send a simple text notification to all users:
```json
{
  "message": {
    "topic": "all",
    "notification": {
      "title": "Timetable Update",
      "body": "Classes are suspended for tomorrow."
    }
  }
}
```

### 2) Cancel Class Notification (by ID)
If you want to cancel a specific scheduled alarm silently from the backend without showing a notification to the user, send a data payload using `cancel_class_notification`:
```json
{
  "message": {
    "topic": "all",
    "data": {
      "action": "cancel_class_notification",
      "id": "class:Mon:09:00:Maths"
    }
  }
}
```

### 3) Cancel Class Notification (by Subject/Time)
Alternatively, you can cancel by passing the class details and the app will construct the ID locally:
```json
{
  "message": {
    "topic": "all",
    "data": {
      "action": "cancel_class_notification",
      "day": "Mon",
      "start_time": "09:00",
      "subject": "Maths"
    }
  }
}
```

## 6. Troubleshooting
* **Missing google-services.json:** Ensure the JSON file is directly under `/app/` and contains the correct package name.
* **Notifications missing on Android 13+:** Ensure you grant the Notification permission when prompted. If dismissed, you need to manually enable it from Android Settings -> Apps -> Timetable.
* **Topic subscription failing:** Check logcat for `Failed to subscribe to FCM topic`. It usually means there is no active internet connection or the Firebase setup doesn't match the current app footprint.
