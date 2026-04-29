/**
 * Danjjak Health Service
 * Health Connect (Android) / HealthKit (iOS) 통합
 * @capgo/capacitor-health 플러그인 사용
 */
import { Capacitor } from '@capacitor/core';

// Health 플러그인 동적 임포트 (네이티브 환경에서만 로드)
let HealthPlugin: any = null;

async function getHealthPlugin() {
  if (!HealthPlugin) {
    try {
      const module = await import('@capgo/capacitor-health');
      HealthPlugin = module.Health;
    } catch {
      console.warn('[Health] Plugin not available');
    }
  }
  return HealthPlugin;
}

// ─── Types ───
export interface StepData {
  date: string;
  steps: number;
}

export interface HealthSummary {
  todaySteps: number;
  weeklySteps: StepData[];
  isAvailable: boolean;
}

// ─── Health Connect 초기화 ───
export async function initHealth(): Promise<boolean> {
  if (!Capacitor.isNativePlatform()) {
    console.info('[Health] Not available on web, using mock data');
    return false;
  }

  const plugin = await getHealthPlugin();
  if (!plugin) return false;

  try {
    const available = await plugin.isAvailable();
    if (!available.available) {
      console.warn('[Health] Health Connect is not available on this device');
      return false;
    }

    const authResult = await plugin.requestAuthorization({
      read: ['steps', 'calories.active'],
      write: [],
    });

    return authResult.granted ?? false;
  } catch (err) {
    console.error('[Health] Init error:', err);
    return false;
  }
}

// ─── 오늘의 걸음수 조회 ───
export async function getTodaySteps(): Promise<number> {
  if (!Capacitor.isNativePlatform()) {
    // 웹 환경 mock 데이터
    return Math.floor(3000 + Math.random() * 8000);
  }

  const plugin = await getHealthPlugin();
  if (!plugin) return 0;

  try {
    const now = new Date();
    const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    const result = await plugin.queryAggregated({
      startDate: startOfDay.toISOString(),
      endDate: now.toISOString(),
      dataType: 'steps',
    });

    return result?.value ?? 0;
  } catch (err) {
    console.error('[Health] Error fetching steps:', err);
    return 0;
  }
}

// ─── 주간 걸음수 조회 ───
export async function getWeeklySteps(): Promise<StepData[]> {
  if (!Capacitor.isNativePlatform()) {
    // 웹 환경 mock 데이터
    const days: StepData[] = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      days.push({
        date: date.toISOString().split('T')[0],
        steps: Math.floor(3000 + Math.random() * 8000),
      });
    }
    return days;
  }

  const plugin = await getHealthPlugin();
  if (!plugin) return [];

  try {
    const now = new Date();
    const weekAgo = new Date();
    weekAgo.setDate(now.getDate() - 7);

    const result = await plugin.query({
      startDate: weekAgo.toISOString(),
      endDate: now.toISOString(),
      dataType: 'steps',
      limit: 7,
    });

    return (result?.data || []).map((item: any) => ({
      date: item.startDate?.split('T')[0] || '',
      steps: item.value || 0,
    }));
  } catch (err) {
    console.error('[Health] Error fetching weekly steps:', err);
    return [];
  }
}

// ─── 전체 건강 요약 ───
export async function getHealthSummary(): Promise<HealthSummary> {
  const isAvailable = await initHealth();
  const todaySteps = await getTodaySteps();
  const weeklySteps = await getWeeklySteps();

  return {
    todaySteps,
    weeklySteps,
    isAvailable,
  };
}
