import { useState, useMemo } from 'react';
import { IonContent, IonPage } from '@ionic/react';
import './TimelinePage.css';

interface TimelineEvent {
  time: string;
  title: string;
  description: string;
  category: 'Health' | 'Media' | 'Location' | 'Personal' | 'Study';
}

// Mock 데이터
const mockEvents: Record<number, TimelineEvent[]> = {
  29: [
    { time: '07:30', title: '아침 러닝', description: '한강공원에서 5km 러닝 완료', category: 'Health' },
    { time: '12:00', title: '사이제리야 점심', description: '치킨이랑 도리아를 먹었어', category: 'Personal' },
    { time: '14:00', title: '스터디 카페', description: '강남 토즈에서 React 공부', category: 'Study' },
    { time: '19:30', title: 'Netflix 시청', description: '기묘한 이야기 시즌5 2편 시청', category: 'Media' },
  ],
  28: [
    { time: '09:00', title: '출근', description: '지하철 2호선 이용', category: 'Location' },
    { time: '18:30', title: '퇴근 후 산책', description: '선릉역 주변 30분 산책', category: 'Health' },
  ],
  27: [
    { time: '10:00', title: '재택근무', description: '집에서 화상회의 3건', category: 'Personal' },
    { time: '20:00', title: '홈트레이닝', description: '스쿼트 50개, 플랭크 3세트', category: 'Health' },
  ],
};

const categoryBadgeClass: Record<string, string> = {
  Health: 'badge-health',
  Media: 'badge-media',
  Location: 'badge-location',
  Personal: 'badge-personal',
  Study: 'badge-study',
};

const categoryLabel: Record<string, string> = {
  Health: '건강',
  Media: '미디어',
  Location: '장소',
  Personal: '개인',
  Study: '학습',
};

const TimelinePage: React.FC = () => {
  const today = new Date();
  const [selectedDay, setSelectedDay] = useState(today.getDate());
  const [currentMonth] = useState(today.getMonth());
  const [currentYear] = useState(today.getFullYear());

  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const firstDayOfWeek = new Date(currentYear, currentMonth, 1).getDay();

  const calendarDays = useMemo(() => {
    const days: (number | null)[] = [];
    // 빈 칸 (요일 맞추기)
    for (let i = 0; i < firstDayOfWeek; i++) days.push(null);
    for (let d = 1; d <= daysInMonth; d++) days.push(d);
    return days;
  }, [daysInMonth, firstDayOfWeek]);

  const events = mockEvents[selectedDay] || [];
  const weekLabels = ['일', '월', '화', '수', '목', '금', '토'];

  return (
    <IonPage>
      <IonContent fullscreen>
        <div className="timeline-container">
          {/* 헤더 */}
          <div className="timeline-header animate-fade-in">
            <h1 className="timeline-title">타임라인</h1>
            <p className="timeline-month">
              {currentYear}년 {currentMonth + 1}월
            </p>
          </div>

          {/* 캘린더 */}
          <div className="calendar-card glass-card animate-fade-in" style={{ animationDelay: '0.1s' }}>
            <div className="calendar-week-labels">
              {weekLabels.map((w) => (
                <span key={w} className="calendar-week-label">{w}</span>
              ))}
            </div>
            <div className="calendar-grid">
              {calendarDays.map((day, idx) => (
                <button
                  key={idx}
                  className={`calendar-day ${day === selectedDay ? 'calendar-day--selected' : ''} ${day === today.getDate() ? 'calendar-day--today' : ''} ${!day ? 'calendar-day--empty' : ''} ${day && mockEvents[day] ? 'calendar-day--has-events' : ''}`}
                  onClick={() => day && setSelectedDay(day)}
                  disabled={!day}
                >
                  {day || ''}
                </button>
              ))}
            </div>
          </div>

          {/* 이벤트 목록 */}
          <div className="events-section animate-fade-in" style={{ animationDelay: '0.2s' }}>
            <h2 className="section-title">
              {currentMonth + 1}월 {selectedDay}일의 기록
            </h2>

            {events.length === 0 ? (
              <div className="events-empty glass-card">
                <span className="events-empty-icon">📝</span>
                <p className="events-empty-text">이 날의 기록이 없어요</p>
                <p className="events-empty-sub">기록 탭에서 오늘의 일상을 남겨보세요</p>
              </div>
            ) : (
              <div className="events-list">
                {events.map((event, idx) => (
                  <div key={idx} className="event-item animate-fade-in" style={{ animationDelay: `${0.3 + idx * 0.08}s` }}>
                    <div className="event-timeline-dot">
                      <div className="event-dot" />
                      {idx < events.length - 1 && <div className="event-line" />}
                    </div>
                    <div className="event-card glass-card">
                      <div className="event-card-top">
                        <span className="event-time">{event.time}</span>
                        <span className={`category-badge ${categoryBadgeClass[event.category]}`}>
                          {categoryLabel[event.category]}
                        </span>
                      </div>
                      <h3 className="event-title">{event.title}</h3>
                      <p className="event-desc">{event.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default TimelinePage;
