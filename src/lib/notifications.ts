// Class notification system using browser Notification API

export function requestNotificationPermission() {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
  }
}

export function isNotificationEnabled(): boolean {
  return 'Notification' in window && Notification.permission === 'granted';
}

export function sendNotification(title: string, body: string) {
  if (isNotificationEnabled()) {
    new Notification(title, {
      body,
      icon: '/pwa-192x192.png',
      badge: '/pwa-192x192.png',
    });
  }
}

// Parse time string like "09:50" to minutes since midnight
function parseTime(timeStr: string): number {
  const [hours, minutes] = timeStr.split(':').map(Number);
  return hours * 60 + minutes;
}

interface ClassInfo {
  subject: string;
  startTime: string; // "HH:MM" 24h format
  room: string;
}

let notificationIntervalId: ReturnType<typeof setInterval> | null = null;
const notifiedClasses = new Set<string>();

export function startClassNotifications(classes: ClassInfo[]) {
  stopClassNotifications();
  notifiedClasses.clear();

  notificationIntervalId = setInterval(() => {
    if (!isNotificationEnabled()) return;

    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    for (const cls of classes) {
      const classMinutes = parseTime(cls.startTime);
      const diff = classMinutes - currentMinutes;
      const key20 = `${cls.subject}_20`;
      const key5 = `${cls.subject}_5`;
      const keyNow = `${cls.subject}_now`;

      if (diff === 20 && !notifiedClasses.has(key20)) {
        notifiedClasses.add(key20);
        sendNotification('⏰ Class in 20 minutes', `${cls.subject} starts at ${cls.startTime} in ${cls.room}`);
      }
      if (diff === 5 && !notifiedClasses.has(key5)) {
        notifiedClasses.add(key5);
        sendNotification('🔔 Class in 5 minutes!', `${cls.subject} is about to start in ${cls.room}`);
      }
      if (diff === 0 && !notifiedClasses.has(keyNow)) {
        notifiedClasses.add(keyNow);
        sendNotification('📚 Class is starting now!', `${cls.subject} is ongoing in ${cls.room}`);
      }
    }
  }, 30000); // check every 30 seconds
}

export function stopClassNotifications() {
  if (notificationIntervalId) {
    clearInterval(notificationIntervalId);
    notificationIntervalId = null;
  }
}
