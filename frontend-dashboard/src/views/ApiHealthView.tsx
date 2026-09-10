import React, { useState } from 'react';
import { Activity, Clock, ShieldAlert, CheckCircle, Search, Filter } from 'lucide-react';
import { ApiHealthMetrics } from '../types/telemetry';

interface ApiHealthViewProps {
  data: ApiHealthMetrics | null;
}

export const ApiHealthView: React.FC<ApiHealthViewProps> = ({ data }) => {
  const [searchTerm, setSearchTerm] = useState('');

  if (!data) return <div style={{ color: 'var(--text-muted)' }}>Carregando telemetria de APIs...</div>;

  const filteredEndpoints = data.endpoints.filter(e =>
    e.path.toLowerCase().includes(searchTerm.toLowerCase()) ||
    e.method.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          Saúde das APIs & <span style={{ color: 'var(--blue-light)' }}>Tráfego HTTP</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Análise de desempenho, percentis de latência (P50 a P99), distribuição de status e rotas ativas
        </div>
      </div>

      {/* Grid KPI Metrics */}
      <div className="grid-4" style={{ marginBottom: '24px' }}>
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Activity size={16} style={{ color: 'var(--blue-light)' }} /> Throughput (RPS)</span>
            <span className="badge badge-blue">LIVE</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.requestsPerSecond}</span>
            <span className="metric-unit">req/s</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Total processado: <strong>{data.totalRequests.toLocaleString()}</strong> requisições
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><Clock size={16} style={{ color: 'var(--purple-light)' }} /> Latência P95</span>
            <span className="badge badge-purple">SLA: &lt;100ms</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val" style={{ color: data.p95LatencyMs > 100 ? 'var(--yellow-light)' : 'var(--green-light)' }}>
              {data.p95LatencyMs}
            </span>
            <span className="metric-unit">ms</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px', display: 'flex', gap: '12px' }}>
            <span>P50: {data.p50LatencyMs}ms</span>
            <span>P90: {data.p90LatencyMs}ms</span>
            <span>P99: {data.p99LatencyMs}ms</span>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><ShieldAlert size={16} style={{ color: 'var(--red-light)' }} /> Taxa de Erros</span>
            <span className="badge badge-green">SLO: &lt;1.0%</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val" style={{ color: data.errorRatePercentage > 1.0 ? 'var(--red-light)' : 'var(--green-light)' }}>
              {data.errorRatePercentage}
            </span>
            <span className="metric-unit">%</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Combinação de erros 4xx (cliente) e 5xx (servidor)
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><CheckCircle size={16} style={{ color: 'var(--green-light)' }} /> Status HTTP</span>
            <span className="badge badge-green">2xx OK: 99.3%</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', marginTop: '6px' }}>
            <span className="badge badge-green">200: {data.statusDistribution["200_OK"]}</span>
            <span className="badge badge-green">201: {data.statusDistribution["201_CREATED"]}</span>
            <span className="badge badge-yellow">400: {data.statusDistribution["400_BAD_REQUEST"]}</span>
            <span className="badge badge-red">500: {data.statusDistribution["500_SERVER_ERROR"]}</span>
          </div>
        </div>
      </div>

      {/* Endpoints Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">Métricas Detalhadas por Endpoint de API</span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ position: 'relative' }}>
              <Search size={14} style={{ position: 'absolute', left: '10px', top: '10px', color: 'var(--text-muted)' }} />
              <input
                type="text"
                className="form-input"
                placeholder="Filtrar endpoint..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ paddingLeft: '32px', width: '220px', fontSize: '12px' }}
              />
            </div>
          </div>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Método</th>
                <th>Rota / Endpoint</th>
                <th>Volume (Chamadas)</th>
                <th>Latência P95 (ms)</th>
                <th>Taxa de Erro (%)</th>
                <th>Status Saúde</th>
              </tr>
            </thead>
            <tbody>
              {filteredEndpoints.map((ep, idx) => (
                <tr key={idx}>
                  <td>
                    <span className={`badge ${
                      ep.method === 'GET' ? 'badge-blue' :
                      ep.method === 'POST' ? 'badge-green' :
                      ep.method === 'PUT' ? 'badge-yellow' : 'badge-red'
                    }`}>
                      {ep.method}
                    </span>
                  </td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 600 }}>{ep.path}</td>
                  <td>{ep.count.toLocaleString()}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 700, color: ep.p95Ms > 100 ? 'var(--yellow-light)' : 'var(--green-light)' }}>
                    {ep.p95Ms} ms
                  </td>
                  <td>
                    <span style={{ color: ep.errorRate > 1.0 ? 'var(--red-light)' : 'var(--text-muted)' }}>
                      {ep.errorRate}%
                    </span>
                  </td>
                  <td>
                    <span className={`badge ${
                      ep.status === 'HEALTHY' ? 'badge-green' :
                      ep.status === 'WARNING' ? 'badge-yellow' : 'badge-red'
                    }`}>
                      {ep.status}
                    </span>
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
