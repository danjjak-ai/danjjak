import admin from 'firebase-admin';
import fs from 'fs';
import path from 'path';

// Firebase Admin 초기화 (서비스 계정 키가 존재할 때만 실행)
const serviceAccountPath = path.resolve(process.cwd(), 'firebase-service-account.json');
if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
    console.log('[Firebase Admin] Initialized successfully.');
} else {
    console.log('[Firebase Admin] service-account key not found. Using Mock mode.');
}

export class PushService {
    async sendNudge(fcmToken: string, title: string, body: string): Promise<void> {
        if (!admin.apps.length) {
            console.log(`[PushService(Mock)] -> To: ${fcmToken}, Title: ${title}, Body: ${body}`);
            return;
        }

        try {
            await admin.messaging().send({
                token: fcmToken,
                notification: { title, body },
                android: {
                    priority: 'high',
                    notification: {
                        channelId: 'SensorServiceChannel',
                        color: '#6750A4'
                    }
                }
            });
            console.log('[PushService] FCM sent successfully.');
        } catch (error) {
            console.error('[PushService] FCM send error:', error);
        }
    }
}

export const pushService = new PushService();
