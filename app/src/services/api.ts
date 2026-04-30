/**
 * Danjjak API Service
 * 기존 Node.js/Express 백엔드와 통신하는 클라이언트
 */

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000';

function getToken(): string | null {
  return localStorage.getItem('danjjak_jwt');
}

function setToken(token: string): void {
  localStorage.setItem('danjjak_jwt', token);
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> || {}),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

export const api = {
  // ─── Auth ───
  login: async (provider: 'google' | 'kakao', idToken: string) => {
    const res = await request<{ success: boolean; token: string; user: { name: string; email: string } }>(
      '/api/auth/login',
      { method: 'POST', body: JSON.stringify({ provider, idToken }) }
    );
    if (res.token) setToken(res.token);
    return res;
  },

  // ─── Journal ───
  saveJournal: (text: string, tags: string[]) =>
    request<{ success: boolean; id: string }>('/api/journal', {
      method: 'POST',
      body: JSON.stringify({ text, tags, timestamp: Date.now() }),
    }),

  getHistory: () =>
    request<{ success: boolean; history: any[] }>('/api/journal'),

  // ─── AI Advice ───
  getAdvice: () =>
    request<{ advice: string }>('/api/nudge'),

  // ─── Feedback ───
  sendFeedback: (reaction: 'LIKE' | 'DISLIKE') =>
    request<{ success: boolean }>('/api/feedback', {
      method: 'POST',
      body: JSON.stringify({ reaction }),
    }),

  // ─── Sensor ───
  sendSensorData: (type: string, value: string) =>
    request<{ success: boolean }>('/api/sensor', {
      method: 'POST',
      body: JSON.stringify({ type, value }),
    }),

  // ─── FCM Token ───
  registerFcmToken: (token: string) =>
    request<{ success: boolean }>('/api/auth/fcm-token', {
      method: 'POST',
      body: JSON.stringify({ fcmToken: token }),
    }),
};
