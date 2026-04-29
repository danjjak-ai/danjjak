import { useState } from 'react';
import { IonContent, IonPage, useIonToast } from '@ionic/react';
import { api } from '../services/api';
import './RegistrationPage.css';

interface Suggestion {
  emoji: string;
  label: string;
  prompt: string;
  color: string;
}

const suggestions: Suggestion[] = [
  { emoji: '📷', label: '어제의 사진', prompt: '어제 찍은 사진에 대해 이야기해볼까요?', color: '#FFE0B2' },
  { emoji: '👤', label: '누구와 함께?', prompt: '오늘 누구와 시간을 보냈나요?', color: '#E1BEE7' },
  { emoji: '🏃', label: '운동 완료', prompt: '오늘 어떤 운동을 했나요?', color: '#C8E6C9' },
  { emoji: '🍽️', label: '식사 기록', prompt: '오늘 무얼 드셨나요?', color: '#FFCCBC' },
  { emoji: '📚', label: '배운 것', prompt: '오늘 새로 배운 것이 있나요?', color: '#B3E5FC' },
  { emoji: '💭', label: '자유 기록', prompt: '', color: '#F0F4C3' },
];

const RegistrationPage: React.FC = () => {
  const [text, setText] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [present] = useIonToast();

  const handleSuggestionClick = (suggestion: Suggestion) => {
    if (suggestion.prompt) {
      setText(suggestion.prompt + '\n');
    }
  };

  const handleSave = async () => {
    if (!text.trim()) return;
    setIsSaving(true);
    try {
      await api.saveJournal(text, ['daily', 'manual']);
      present({
        message: '기록이 저장되었습니다 ✨',
        duration: 2000,
        position: 'bottom',
        color: 'success',
      });
      setText('');
    } catch {
      present({
        message: '저장에 실패했습니다. 다시 시도해주세요.',
        duration: 2000,
        position: 'bottom',
        color: 'danger',
      });
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <IonPage>
      <IonContent fullscreen>
        <div className="reg-container">
          <div className="reg-header animate-fade-in">
            <h1 className="reg-title">기록하기</h1>
            <p className="reg-subtitle">오늘 하루를 단짝에게 들려주세요</p>
          </div>

          {/* AI 추천 제안 카드 */}
          <div className="reg-suggestions animate-fade-in" style={{ animationDelay: '0.1s' }}>
            <p className="reg-suggestions-label">💡 단짝의 추천 제안</p>
            <div className="reg-suggestions-scroll">
              {suggestions.map((s, idx) => (
                <button
                  key={idx}
                  className="suggestion-chip"
                  style={{ '--chip-bg': s.color } as React.CSSProperties}
                  onClick={() => handleSuggestionClick(s)}
                  id={`btn-suggestion-${idx}`}
                >
                  <span className="suggestion-emoji">{s.emoji}</span>
                  <span className="suggestion-label">{s.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* 텍스트 입력 */}
          <div className="reg-input-area animate-fade-in" style={{ animationDelay: '0.2s' }}>
            <textarea
              className="reg-textarea"
              value={text}
              onChange={(e) => setText(e.target.value)}
              placeholder="오늘 어떤 좋은 일이 있었나요? 자유롭게 적어주세요..."
              rows={8}
              id="textarea-journal"
            />
            <div className="reg-char-count">
              {text.length} / 5,000
            </div>
          </div>

          {/* 저장 버튼 */}
          <div className="reg-footer animate-fade-in" style={{ animationDelay: '0.3s' }}>
            <button
              className={`reg-save-btn ${text.trim() ? 'reg-save-btn--active' : ''}`}
              disabled={!text.trim() || isSaving}
              onClick={handleSave}
              id="btn-save-journal"
            >
              {isSaving ? (
                <span className="reg-spinner" />
              ) : (
                '기록 저장하기'
              )}
            </button>
          </div>
        </div>
      </IonContent>
    </IonPage>
  );
};

export default RegistrationPage;
