import { useState } from 'react';
import { IonContent, IonPage, IonToggle } from '@ionic/react';
import { useHistory } from 'react-router-dom';
import './ConsentPage.css';

interface ConsentItem {
  key: string;
  title: string;
  description: string;
  icon: string;
}

const consentItems: ConsentItem[] = [
  {
    key: 'l0',
    title: 'L0 — 원시 데이터 수집',
    description: 'GPS 위치, 걸음 수, 앱 사용 기록 등 기기 센서 데이터를 수집합니다. 모든 데이터는 기기 내에 안전하게 저장됩니다.',
    icon: '📡',
  },
  {
    key: 'l1',
    title: 'L1 — 자연어 활동 분석',
    description: '수집된 데이터를 "오후 3시에 카페에서 공부"와 같은 자연어 이벤트로 변환합니다. 민감 정보는 토큰화되어 비식별 처리됩니다.',
    icon: '🧠',
  },
  {
    key: 'l2',
    title: 'L2 — AI 개인화 학습',
    description: '피드백 기반으로 AI 단짝의 성격과 조언 스타일을 당신에게 맞춰 개인화합니다. 언제든 초기화할 수 있습니다.',
    icon: '✨',
  },
];

const ConsentPage: React.FC = () => {
  const history = useHistory();
  const [consents, setConsents] = useState<Record<string, boolean>>({
    l0: false,
    l1: false,
    l2: false,
  });

  const allConsented = Object.values(consents).every(Boolean);

  const handleToggle = (key: string) => {
    setConsents((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const handleContinue = () => {
    // TODO: 서버에 동의 상태 전송
    history.push('/app/dashboard');
  };

  return (
    <IonPage>
      <IonContent fullscreen>
        <div className="consent-container">
          <div className="consent-header animate-fade-in">
            <div className="consent-icon">🔒</div>
            <h1 className="consent-title">프라이버시 설정</h1>
            <p className="consent-desc">
              단짝이 당신을 이해하기 위해 아래 데이터 수집에 동의해주세요.
              <br />모든 데이터는 <strong>Privacy by Design</strong> 원칙에 따라 처리됩니다.
            </p>
          </div>

          <div className="consent-items">
            {consentItems.map((item, idx) => (
              <div
                key={item.key}
                className={`consent-card glass-card animate-fade-in ${consents[item.key] ? 'consent-card--active' : ''}`}
                style={{ animationDelay: `${0.1 + idx * 0.1}s` }}
              >
                <div className="consent-card-header">
                  <div className="consent-card-left">
                    <span className="consent-card-icon">{item.icon}</span>
                    <span className="consent-card-title">{item.title}</span>
                  </div>
                  <IonToggle
                    checked={consents[item.key]}
                    onIonChange={() => handleToggle(item.key)}
                    id={`toggle-consent-${item.key}`}
                  />
                </div>
                <p className="consent-card-desc">{item.description}</p>
              </div>
            ))}
          </div>

          <div className="consent-footer animate-fade-in" style={{ animationDelay: '0.5s' }}>
            <button
              className={`consent-btn ${allConsented ? 'consent-btn--active' : ''}`}
              disabled={!allConsented}
              onClick={handleContinue}
              id="btn-consent-continue"
            >
              동의하고 시작하기
            </button>
          </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default ConsentPage;
