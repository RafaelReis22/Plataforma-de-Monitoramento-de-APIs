import React, { useState, useEffect } from 'react';
import { Topbar } from './components/Topbar';
import { AuthModal } from './components/AuthModal';
import { HardwareView } from './views/HardwareView';
import { ApiHealthView } from './views/ApiHealthView';
import { JvmView } from './views/JvmView';
import { AlertsView } from './views/AlertsView';
import { SyntheticView } from './views/SyntheticView';
import { ApiKeysView } from './views/ApiKeysView';
import { TracesLogsView } from './views/TracesLogsView';
import { DesignSystemView } from './views/DesignSystemView';
import { HardwareMetrics, ApiHealthMetrics, JvmMetrics, UserProfile } from './types/telemetry';

export function App() {
  const [activeTab, setActiveTab] = useState<string>('hardware');
  const [autoRefreshSec, setAutoRefreshSec] = useState<number>(5);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState<boolean>(false);

  const [user, setUser] = useState<UserProfile | null>({
    username: 'admin',
    role: 'ADMIN',
    tenantId: 'tenant-enterprise-01',
    email: 'admin@monitoring-platform.io',
    organization: 'Enterprise Telemetry Corp'
  });

  const [hardwareData, setHardwareData] = useState<HardwareMetrics | null>(null);
  const [apiHealthData, setApiHealthData] = useState<ApiHealthMetrics | null>(null);
  const [jvmData, setJvmData] = useState<JvmMetrics | null>(null);
  const [alertsData, setAlertsData] = useState<any | null>(null);
  const [syntheticData, setSyntheticData] = useState<any | null>(null);

  const fetchTelemetry = async () => {
    setIsRefreshing(true);
    try {
      const [hwRes, apiRes, jvmRes, altRes, synRes] = await Promise.allSettled([
        fetch('/api/v1/telemetry/hardware').then(r => r.ok ? r.json() : null),
        fetch('/api/v1/telemetry/api-health').then(r => r.ok ? r.json() : null),
        fetch('/api/v1/telemetry/jvm').then(r => r.ok ? r.json() : null),
        fetch('/api/v1/telemetry/alerts').then(r => r.ok ? r.json() : null),
        fetch('/api/v1/telemetry/synthetic').then(r => r.ok ? r.json() : null),
      ]);

      if (hwRes.status === 'fulfilled' && hwRes.value) setHardwareData(hwRes.value);
      if (apiRes.status === 'fulfilled' && apiRes.value) setApiHealthData(apiRes.value);
      if (jvmRes.status === 'fulfilled' && jvmRes.value) setJvmData(jvmRes.value);
      if (altRes.status === 'fulfilled' && altRes.value) setAlertsData(altRes.value);
      if (synRes.status === 'fulfilled' && synRes.value) setSyntheticData(synRes.value);
    } catch (e) {
      console.warn("Telemetry fetch fallback active", e);
    } finally {
      setTimeout(() => setIsRefreshing(false), 400);
    }
  };

  useEffect(() => {
    fetchTelemetry();
    if (autoRefreshSec <= 0) return;

    const interval = setInterval(() => {
      fetchTelemetry();
    }, autoRefreshSec * 1000);

    return () => clearInterval(interval);
  }, [autoRefreshSec]);

  return (
    <div className="app-container">
      <Topbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        autoRefreshSec={autoRefreshSec}
        setAutoRefreshSec={setAutoRefreshSec}
        user={user}
        onOpenAuthModal={() => setIsAuthModalOpen(true)}
        isRefreshing={isRefreshing}
      />

      <main className="main-content">
        {activeTab === 'hardware' && <HardwareView data={hardwareData} />}
        {activeTab === 'api-health' && <ApiHealthView data={apiHealthData} />}
        {activeTab === 'jvm' && <JvmView data={jvmData} />}
        {activeTab === 'alerts' && <AlertsView data={alertsData} />}
        {activeTab === 'synthetic' && <SyntheticView data={syntheticData} />}
        {activeTab === 'api-keys' && <ApiKeysView />}
        {activeTab === 'traces' && <TracesLogsView />}
        {activeTab === 'design-system' && <DesignSystemView />}
      </main>

      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        user={user}
        onLoginSuccess={(u) => setUser(u)}
      />
    </div>
  );
}

export default App;
