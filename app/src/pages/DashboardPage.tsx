import { useState, useEffect } from 'react';
import { IonContent, IonPage, IonIcon } from '@ionic/react';
import { thumbsUpOutline, thumbsDownOutline, refreshOutline } from 'ionicons/icons';
import { api } from '../services/api';
import './DashboardPage.css';

const DashboardPage: React.FC = () => {
  const [steps, _setSteps] = useState(5234);
  const [advice, setAdvice] = useState<string | null>(null);
  const [isLoadingAdvice, setIsLoadingAdvice] = useState(true);
  const [energy] = useState(85);
  const [stress] = useState('낮음');

  // 7일간 활동량 데이터 (mock)
  const weeklyData = [3200, 7800, 5100, 4600, 8200, 6500, steps];
  const weekDays = ['월', '화', '수', '목', '금', '토', '일'];
  const maxSteps = Math.max(...weeklyData);

  useEffect(() => {
    loadAdvice();
  }, []);

  const loadAdvice = async () => {
    setIsLoadingAdvice(true);
    try {
      const res = await api.getAdvice();
      setAdvice(res.advice || '오늘도 좋은 하루 보내세요! 🌟');
    } catch {
      setAdvice('단짝이 당신의 일상을 분석하고 있어요. 잠시만 기다려주세요.');
    } finally {
      setIsLoadingAdvice(false);
    }
  };

  const handleFeedback = async (reaction: 'LIKE' | 'DISLIKE') => {
    try {
      await api.sendFeedback(reaction);
    } catch { /* silent */ }
  };

  return (
    <IonPage>
      <IonContent fullscreen>
        <div className="dashboard-container">
          {/* 헤더 */}
          <div className="dashboard-header animate-fade-in">
            <div>
              <p className="dashboard-greeting">안녕하세요 👋</p>
              <h1 className="dashboard-title">오늘의 단짝</h1>
            </div>
            <div className="dashboard-date">
              {new Date().toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })}
            </div>
          </div>

          {/* 디지털 트윈 카드 */}
          <div className="twin-card glass-card-bright animate-fade-in" style={{ animationDelay: '0.1s' }}>
            <div className="twin-visual">
              <div className="twin-silhouette animate-breathe">
                <div className="twin-glow" />
                <div className="twin-body">🧘</div>
                <div className="twin-scan-line" />
              </div>
            </div>
            <div className="twin-stats">
              <div className="twin-stat">
                <span className="twin-stat-value">{energy}%</span>
                <span className="twin-stat-label">에너지</span>
              </div>
              <div className="twin-stat-divider" />
              <div className="twin-stat">
                <span className="twin-stat-value">{stress}</span>
                <span className="twin-stat-label">스트레스</span>
              </div>
              <div className="twin-stat-divider" />
              <div className="twin-stat">
                <span className="twin-stat-value">{steps.toLocaleString()}</span>
                <span className="twin-stat-label">걸음</span>
              </div>
            </div>
          </div>

          {/* 주간 활동 차트 */}
          <div className="chart-section animate-fade-in" style={{ animationDelay: '0.2s' }}>
            <h2 className="section-title">주간 활동량</h2>
            <div className="chart-card glass-card">
              <div className="bar-chart">
                {weeklyData.map((val, idx) => (
                  <div key={idx} className="bar-col">
                    <div className="bar-wrapper">
                      <div
                        className="bar-fill"
                        style={{
                          height: `${(val / maxSteps) * 100}%`,
                          background: idx === 6
                            ? 'linear-gradient(180deg, var(--primary-light), var(--primary))'
                            : 'rgba(255,255,255,0.12)',
                        }}
                      />
                    </div>
                    <span className={`bar-label ${idx === 6 ? 'bar-label--active' : ''}`}>
                      {weekDays[idx]}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* AI 조언 카드 */}
          <div className="advice-section animate-fade-in" style={{ animationDelay: '0.3s' }}>
            <h2 className="section-title">단짝의 맞춤 조언</h2>
            <div className="advice-card glass-card">
              {isLoadingAdvice ? (
                <div className="advice-loading">
                  <div className="shimmer-loading" style={{ height: 16, width: '90%', marginBottom: 8 }} />
                  <div className="shimmer-loading" style={{ height: 16, width: '75%', marginBottom: 8 }} />
                  <div className="shimmer-loading" style={{ height: 16, width: '60%' }} />
                </div>
              ) : (
                <>
                  <p className="advice-text">{advice}</p>
                  <div className="advice-actions">
                    <button className="advice-btn" onClick={() => handleFeedback('LIKE')} id="btn-feedback-like">
                      <IonIcon icon={thumbsUpOutline} /> 도움돼요
                    </button>
                    <button className="advice-btn" onClick={() => handleFeedback('DISLIKE')} id="btn-feedback-dislike">
                      <IonIcon icon={thumbsDownOutline} /> 별로예요
                    </button>
                    <button className="advice-btn advice-btn--refresh" onClick={loadAdvice} id="btn-refresh-advice">
                      <IonIcon icon={refreshOutline} />
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default DashboardPage;
