import React from 'react';
import { Radio, ShieldCheck, Clock, CheckCircle, AlertTriangle, ExternalLink } from 'lucide-react';
import { SyntheticProbe } from '../types/telemetry';

interface SyntheticViewProps {
  data: any | null;
}

export const SyntheticView: React.FC<SyntheticViewProps> = ({ data }) => {
  const probes: SyntheticProbe[] = data?.probes || [
    { name: "Auth API Health Probe", url: "https://api.monitoring.internal/v1/auth/health", status: "UP", latencyMs: 12, sslDaysRemaining: 84, uptime30d: 100.0 },
    { name: "Checkout Payment Gateway Probe", url: "https://api.monitoring.internal/v1/checkout", status: "UP", latencyMs: 142, sslDaysRemaining: 120, uptime30d: 99.92 },
    { name: "Catalog Search Index Probe", url: "https://api.monitoring.internal/v1/catalog", status: "UP", latencyMs: 24, sslDaysRemaining: 45, uptime30d: 99.99 },
    { name: "Notifications Webhook Endpoint", url: "https://api.monitoring.internal/v1/webhooks", status: "DEGRADED", latencyMs: 480, sslDaysRemaining: 14, uptime30d: 99.85 }
  ];

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          Synthetic Probes & <span style={{ color: 'var(--green-light)' }}>SLA / SLO Meter</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Monitoramento ativo de disponibilidade (Ping HTTP/HTTPS, validação de payload, expiração de certificados SSL/TLS e cálculo de Error Budget)
        </div>
      </div>

      {/* Grid top cards */}
      <div className="grid-3" style={{ marginBottom: '24px' }}>
        <div className="card">
          <div className="card-header">
            <span className="card-title"><ShieldCheck size={16} style={{ color: 'var(--green-light)' }} /> Disponibilidade Geral (30 dias)</span>
            <span className="badge badge-green">TARGET: 99.9%</span>
          </div>
          <div style={{ fontSize: '34px', fontWeight: 800, color: 'var(--green-light)' }}>99.96%</div>
          <div className="progress-bar-bg" style={{ marginTop: '8px' }}>
            <div className="progress-bar-fill" style={{ width: '99.96%', background: 'var(--green)' }} />
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><Clock size={16} style={{ color: 'var(--purple-light)' }} /> Error Budget Restante</span>
            <span className="badge badge-purple">MÊS ATUAL</span>
          </div>
          <div style={{ fontSize: '34px', fontWeight: 800, color: 'var(--purple-light)' }}>84.5%</div>
          <div className="progress-bar-bg" style={{ marginTop: '8px' }}>
            <div className="progress-bar-fill" style={{ width: '84.5%', background: 'linear-gradient(90deg, var(--purple), var(--blue))' }} />
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><Radio size={16} style={{ color: 'var(--blue-light)' }} /> Probes Ativos</span>
            <span className="badge badge-blue">INTERVALO: 60s</span>
          </div>
          <div style={{ fontSize: '34px', fontWeight: 800 }}>4 / 4</div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Testes executados continuamente de 3 regiões
          </div>
        </div>
      </div>

      {/* Probes Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Lista de Testes Sintéticos & Certificados SSL</span>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome do Teste</th>
                <th>URL Alvo</th>
                <th>Status</th>
                <th>Latência Ping</th>
                <th>Expiração SSL</th>
                <th>Uptime 30d</th>
              </tr>
            </thead>
            <tbody>
              {probes.map((p, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 600 }}>{p.name}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '11px', color: 'var(--text-muted)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span>{p.url}</span>
                      <ExternalLink size={12} />
                    </div>
                  </td>
                  <td>
                    <span className={`badge ${p.status === 'UP' ? 'badge-green' : 'badge-yellow'}`}>
                      {p.status}
                    </span>
                  </td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 700, color: p.latencyMs > 200 ? 'var(--yellow-light)' : 'var(--green-light)' }}>
                    {p.latencyMs} ms
                  </td>
                  <td>
                    <span className={`badge ${p.sslDaysRemaining < 30 ? 'badge-yellow' : 'badge-purple'}`}>
                      {p.sslDaysRemaining} dias restantes
                    </span>
                  </td>
                  <td style={{ fontWeight: 700, color: 'var(--green-light)' }}>
                    {p.uptime30d}%
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
