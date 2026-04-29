/**
 * Danjjak Sensor Service
 * Capacitor 플러그인을 통한 네이티브 센서 데이터 수집
 */
import { Capacitor } from '@capacitor/core';
import { Geolocation, type Position } from '@capacitor/geolocation';
import { PushNotifications } from '@capacitor/push-notifications';
import { LocalNotifications } from '@capacitor/local-notifications';
import { api } from './api';

// ─── Platform Check ───
const isNative = Capacitor.isNativePlatform();

// ─── GPS / Geolocation ───
export async function getCurrentPosition(): Promise<{ lat: number; lng: number } | null> {
  try {
    if (!isNative) {
      // 웹 브라우저 Geolocation API fallback
      return new Promise((resolve) => {
        navigator.geolocation.getCurrentPosition(
          (pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
          () => resolve(null)
        );
      });
    }

    const permission = await Geolocation.checkPermissions();
    if (permission.location !== 'granted') {
      await Geolocation.requestPermissions();
    }

    const pos: Position = await Geolocation.getCurrentPosition({
      enableHighAccuracy: true,
      timeout: 10000,
    });
    return { lat: pos.coords.latitude, lng: pos.coords.longitude };
  } catch {
    console.warn('[Sensor] Geolocation unavailable');
    return null;
  }
}

// ─── Push Notifications ───
export async function initPushNotifications(): Promise<void> {
  if (!isNative) {
    console.info('[Sensor] Push notifications not available on web');
    return;
  }

  try {
    const permResult = await PushNotifications.requestPermissions();
    if (permResult.receive !== 'granted') {
      console.warn('[Sensor] Push notification permission denied');
      return;
    }

    await PushNotifications.register();

    PushNotifications.addListener('registration', (token) => {
      console.info('[Sensor] FCM Token:', token.value);
      api.registerFcmToken(token.value);
    });

    PushNotifications.addListener('pushNotificationReceived', (notification) => {
      console.info('[Sensor] Push received:', notification);
    });

    PushNotifications.addListener('pushNotificationActionPerformed', (action) => {
      console.info('[Sensor] Push action:', action);
    });
  } catch (err) {
    console.error('[Sensor] Push init error:', err);
  }
}

// ─── Local Notifications (식사 리마인더 등) ───
export async function scheduleMealReminder(): Promise<void> {
  if (!isNative) return;

  try {
    const perm = await LocalNotifications.requestPermissions();
    if (perm.display !== 'granted') return;

    await LocalNotifications.schedule({
      notifications: [
        {
          title: '단짝 🤝',
          body: '오늘 점심은 무얼 드셨나요? 궁금해요!',
          id: 1001,
          schedule: { on: { hour: 13, minute: 30 }, repeats: true },
          channelId: 'danjjak_nudge',
        },
        {
          title: '단짝 🤝',
          body: '저녁 식사는 하셨나요? 기록해주세요 😊',
          id: 1002,
          schedule: { on: { hour: 19, minute: 30 }, repeats: true },
          channelId: 'danjjak_nudge',
        },
      ],
    });
  } catch (err) {
    console.error('[Sensor] Local notification error:', err);
  }
}

// ─── 센서 데이터 백엔드 전송 ───
export async function sendLocationToBackend(): Promise<void> {
  const pos = await getCurrentPosition();
  if (pos) {
    await api.sendSensorData('GPS', JSON.stringify(pos));
  }
}
