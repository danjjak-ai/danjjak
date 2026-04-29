import { IonApp, IonRouterOutlet, IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { Route, Redirect } from 'react-router-dom';
import { homeOutline, createOutline, calendarOutline } from 'ionicons/icons';

import LoginPage from './pages/LoginPage';
import ConsentPage from './pages/ConsentPage';
import DashboardPage from './pages/DashboardPage';
import RegistrationPage from './pages/RegistrationPage';
import TimelinePage from './pages/TimelinePage';

import './App.css';

const MainTabs: React.FC = () => (
  <IonTabs>
    <IonRouterOutlet>
      <Route exact path="/app/dashboard" component={DashboardPage} />
      <Route exact path="/app/register" component={RegistrationPage} />
      <Route exact path="/app/timeline" component={TimelinePage} />
      <Route exact path="/app">
        <Redirect to="/app/dashboard" />
      </Route>
    </IonRouterOutlet>

    <IonTabBar slot="bottom">
      <IonTabButton tab="dashboard" href="/app/dashboard">
        <IonIcon icon={homeOutline} />
        <IonLabel>홈</IonLabel>
      </IonTabButton>
      <IonTabButton tab="register" href="/app/register">
        <IonIcon icon={createOutline} />
        <IonLabel>기록</IonLabel>
      </IonTabButton>
      <IonTabButton tab="timeline" href="/app/timeline">
        <IonIcon icon={calendarOutline} />
        <IonLabel>타임라인</IonLabel>
      </IonTabButton>
    </IonTabBar>
  </IonTabs>
);

const App: React.FC = () => {
  return (
    <IonApp>
      <IonReactRouter>
        <IonRouterOutlet>
          <Route exact path="/login" component={LoginPage} />
          <Route exact path="/consent" component={ConsentPage} />
          <Route path="/app" component={MainTabs} />
          <Route exact path="/">
            <Redirect to="/login" />
          </Route>
        </IonRouterOutlet>
      </IonReactRouter>
    </IonApp>
  );
};

export default App;
