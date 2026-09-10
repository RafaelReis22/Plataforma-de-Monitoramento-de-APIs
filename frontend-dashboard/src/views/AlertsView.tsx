import React, { useState } from 'react';
import { AlertTriangle, Bell, CheckCircle, ShieldAlert, Plus, Sliders, VolumeX } from 'lucide-react';

interface AlertItem {
  id: string;
  name: string;
  service: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  status: 'FIRING' | 'RESOLVED';
  condition: string;
  currentValue: string;
  triggeredAt: string;
}

interface AlertsViewProps {
  data: any | null;
}

export const AlertsView: React.FC<AlertsViewProps> = ({ data }) => {
  const [alerts, setAlerts] = useState<AlertItem[]>(
    data?.alerts || [
      {
        id: 'ALT-8902',
        name: 'High P99 Latency Spike',
        service: 'api-gateway-interceptor',
        severity: 'CRITICAL',
        status: 'FIRING',
        condition: 'P99 Latency > 300ms por 5 min',
        currentValue: '345 ms',
        triggeredAt: '2026-09-14 16:02:10'
      },
      {
        id: 'ALT-8903',
        name: 'JVM Heap Memory Usage > 80%',
        service: 'storage-layer',
        severity: 'WARNING',
        status: 'FIRING',
        condition: 'Heap > 80% por 10 min',
        currentValue: '84.2 %',
        triggeredAt: '2026-09-14 15:45:00'
      },
      {
        id: 'ALT-8901',
        name: 'High CPU Load on Host',
        service: 'monitoring-agent',
        severity: 'WARNING',
        status: 'RESOLVED',
        condition: 'CPU > 80% por 5 min',
        currentValue: '32.1 %',
        triggeredAt: '2026-09-14 12:30:00'
      }
    ]
  );

  const [ruleName, setRuleName] = useState('');
  const [ruleService, setRuleService] = useState('api-gateway-interceptor');
  const [ruleCondition, setRuleCondition] = useState('Error Rate > 5%');
  const [showForm, setShowForm] = useState(false);

  const handleSilence = (id: string) => {
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, status: 'RESOLVED' } : a));
  };

  const handleAddRule = (e: React.FormEvent) => {
    e.preventDefault();
    if (!ruleName) return;

    const newAlert: AlertItem = {
      id: `ALT-${Math.floor(1000 + Math.random() * 9000)}`,
      name: ruleName,
      service: ruleService,
      severity: 'WARNING',
      status: 'FIRING',
      condition: ruleCondition,
      currentValue: 'Disparado manual',
      triggeredAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
    };

    setAlerts([newAlert, ...alerts]);
    setRuleName('');
    setShowForm(false);
  };

  const firingCount = alerts.filter(a => a.status === 'FIRING').length;
  const criticalCount = alerts.filter(a => a.severity === 'CRITICAL' && a.status === 'FIRING').length;

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
            Central de Alertas & <span style={{ color: 'var(--red-light)' }}>Gestão de Incidentes</span>
          </div>
          <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            Regras ativas, disparos de emergência e canais de notificação (Slack, Teams, PagerDuty, Webhooks)
          </div>
        </div>

        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
          <Plus size={16} /> Nova Regra de Alerta
        </button>
      </div>

      {/* Stats Summary */}
      <div className="grid-3" style={{ marginBottom: '24px' }}>
        <div className="card" style={{ borderColor: firingCount > 0 ? 'rgba(239,68,68,0.3)' : 'var(--border)' }}>
          <div className="card-header">
            <span className="card-title"><AlertTriangle size={16} style={{ color: 'var(--red-light)' }} /> Alertas Disparados</span>
            <span className="badge badge-red">EM ATENÇÃO</span>
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--red-light)' }}>{firingCount}</div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
            Alertas aguardando resolução ou silenciamento
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><ShieldAlert size={16} style={{ color: 'var(--red)' }} /> Incidentes Críticos</span>
            <span className="badge badge-red">P1 / P2</span>
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--red)' }}>{criticalCount}</div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
            Violação de SLA ou degradação grave de serviço
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><CheckCircle size={16} style={{ color: 'var(--green-light)' }} /> Serviços Operacionais</span>
            <span className="badge badge-green">HEALTHY</span>
          </div>
          <div style={{ fontSize: '32px', fontWeight: 800, color: 'var(--green-light)' }}>14 / 15</div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
            93.3% dos microsserviços dentro do comportamento normal
          </div>
        </div>
      </div>

      {/* New Rule Form */}
      {showForm && (
        <div className="card" style={{ marginBottom: '24px', border: '1px solid var(--purple)' }}>
          <div className="card-header">
            <span className="card-title"><Sliders size={16} /> Configurar Nova Regra de Alerta</span>
          </div>
          <form onSubmit={handleAddRule} style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Nome do Alerta</label>
              <input
                type="text"
                className="form-input"
                placeholder="Ex: High Memory Usage > 90%"
                value={ruleName}
                onChange={(e) => setRuleName(e.target.value)}
                required
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Serviço Alvo</label>
              <select
                className="form-input"
                value={ruleService}
                onChange={(e) => setRuleService(e.target.value)}
              >
                <option value="api-gateway-interceptor">api-gateway-interceptor</option>
                <option value="monitoring-agent">monitoring-agent</option>
                <option value="observability-pipeline">observability-pipeline</option>
                <option value="storage-layer">storage-layer</option>
                <option value="dashboard-service">dashboard-service</option>
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Condição PromQL / Métrica</label>
              <input
                type="text"
                className="form-input"
                value={ruleCondition}
                onChange={(e) => setRuleCondition(e.target.value)}
              />
            </div>

            <div style={{ gridColumn: 'span 3', display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
              <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                Cancelar
              </button>
              <button type="submit" className="btn btn-primary">
                Salvar Regra
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Incident List Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Feed de Incidentes e Regras Disparadas</span>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID Alerta</th>
                <th>Nome da Regra</th>
                <th>Serviço Afetado</th>
                <th>Severidade</th>
                <th>Condição Limiar</th>
                <th>Valor Atual</th>
                <th>Data Disparo</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {alerts.map((a) => (
                <tr key={a.id}>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '11px', color: 'var(--text-muted)' }}>
                    {a.id}
                  </td>
                  <td style={{ fontWeight: 600 }}>{a.name}</td>
                  <td>
                    <span className="badge badge-purple">{a.service}</span>
                  </td>
                  <td>
                    <span className={`badge ${a.severity === 'CRITICAL' ? 'badge-red' : 'badge-yellow'}`}>
                      {a.severity}
                    </span>
                  </td>
                  <td style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{a.condition}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 700, color: 'var(--yellow-light)' }}>
                    {a.currentValue}
                  </td>
                  <td style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{a.triggeredAt}</td>
                  <td>
                    {a.status === 'FIRING' ? (
                      <button
                        className="btn btn-danger"
                        onClick={() => handleSilence(a.id)}
                        style={{ padding: '4px 8px', fontSize: '10px' }}
                      >
                        <VolumeX size={12} /> Silenciar 1h
                      </button>
                    ) : (
                      <span className="badge badge-green">RESOLVIDO</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
