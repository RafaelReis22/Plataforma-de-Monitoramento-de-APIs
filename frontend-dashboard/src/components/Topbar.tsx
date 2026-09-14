import React from 'react';
import { 
  Activity, 
  Server, 
  Cpu, 
  AlertTriangle, 
  Key, 
  Radio, 
  Layers, 
  GitBranch,
  RefreshCw, 
  User, 
  Lock, 
  ChevronDown 
} from 'lucide-react';
import { UserProfile } from '../types/telemetry';

interface TopbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  autoRefreshSec: number;
  setAutoRefreshSec: (sec: number) => void;
  user: UserProfile | null;
  onOpenAuthModal: () => void;
  isRefreshing: boolean;
}

export const Topbar: React.FC<TopbarProps> = ({
  activeTab,
  setActiveTab,
  autoRefreshSec,
  setAutoRefreshSec,
  user,
  onOpenAuthModal,
  isRefreshing
}) => {
  return (
    <header className="topbar">
      {/* Brand & Status */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <div className="logo-dot" title="Telemetria em tempo real Ativa" />
        <div>
          <span className="logo-text">API Monitor</span>
          <span className="badge badge-purple" style={{ marginLeft: '8px', fontSize: '9px', padding: '2px 6px' }}>
            ENTERPRISE
          </span>
        </div>
      </div>

      <div style={{ width: '1px', height: '24px', background: 'var(--border)' }} />

      {/* Nav Tabs */}
      <nav className="nav-tabs">
        <button
          className={`nav-tab ${activeTab === 'hardware' ? 'active' : ''}`}
          onClick={() => setActiveTab('hardware')}
        >
          <Cpu size={15} /> Hardware
        </button>

        <button
          className={`nav-tab ${activeTab === 'api-health' ? 'active' : ''}`}
          onClick={() => setActiveTab('api-health')}
        >
          <Activity size={15} /> API Health
        </button>

        <button
          className={`nav-tab ${activeTab === 'jvm' ? 'active' : ''}`}
          onClick={() => setActiveTab('jvm')}
        >
          <Server size={15} /> JVM Internals
        </button>

        <button
          className={`nav-tab ${activeTab === 'alerts' ? 'active' : ''}`}
          onClick={() => setActiveTab('alerts')}
        >
          <AlertTriangle size={15} /> Central de Alertas
        </button>

        <button
          className={`nav-tab ${activeTab === 'synthetic' ? 'active' : ''}`}
          onClick={() => setActiveTab('synthetic')}
        >
          <Radio size={15} /> Synthetic Probes
        </button>

        <button
          className={`nav-tab ${activeTab === 'api-keys' ? 'active' : ''}`}
          onClick={() => setActiveTab('api-keys')}
        >
          <Key size={15} /> API Keys & SDKs
        </button>

        <button
          className={`nav-tab ${activeTab === 'traces' ? 'active' : ''}`}
          onClick={() => setActiveTab('traces')}
        >
          <GitBranch size={15} /> Traces & Logs
        </button>

        <button
          className={`nav-tab ${activeTab === 'design-system' ? 'active' : ''}`}
          onClick={() => setActiveTab('design-system')}
        >
          <Layers size={15} /> Design System
        </button>
      </nav>

      <div style={{ flex: 1 }} />

      {/* Auto Refresh & Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'var(--surface)', padding: '4px 10px', borderRadius: '8px', border: '1px solid var(--border)' }}>
          <RefreshCw size={13} className={isRefreshing ? 'animate-spin' : ''} style={{ color: 'var(--purple-light)' }} />
          <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Atualizar:</span>
          <select
            value={autoRefreshSec}
            onChange={(e) => setAutoRefreshSec(Number(e.target.value))}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text)',
              fontSize: '11px',
              fontFamily: 'inherit',
              cursor: 'pointer',
              outline: 'none'
            }}
          >
            <option value={5} style={{ background: 'var(--card)' }}>5s</option>
            <option value={10} style={{ background: 'var(--card)' }}>10s</option>
            <option value={30} style={{ background: 'var(--card)' }}>30s</option>
            <option value={0} style={{ background: 'var(--card)' }}>Pausado</option>
          </select>
        </div>

        {/* User Auth Profile Button */}
        <button
          className="btn btn-secondary"
          onClick={onOpenAuthModal}
          style={{ fontSize: '11px', padding: '6px 12px' }}
        >
          {user ? (
            <>
              <User size={14} style={{ color: 'var(--green-light)' }} />
              <span>{user.username}</span>
              <span className="badge badge-green" style={{ fontSize: '9px', padding: '1px 5px' }}>
                {user.role}
              </span>
            </>
          ) : (
            <>
              <Lock size={14} style={{ color: 'var(--yellow-light)' }} />
              <span>Entrar / Autenticar</span>
            </>
          )}
        </button>
      </div>
    </header>
  );
};
