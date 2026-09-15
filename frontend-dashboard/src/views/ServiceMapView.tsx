import React from 'react';
import { Network, Server, Database, Cpu, ShieldCheck, ArrowRight, Zap } from 'lucide-react';

export const ServiceMapView: React.FC = () => {
  const nodes = [
    { id: 'gw-01', name: 'api-gateway-interceptor', type: 'GATEWAY', rps: 1250, latency: '12.4ms', status: 'HEALTHY' },
    { id: 'pipeline-01', name: 'observability-pipeline', type: 'PIPELINE', rps: 1250, latency: '5.1ms', status: 'HEALTHY' },
    { id: 'storage-01', name: 'storage-layer', type: 'SERVICE', rps: 850, latency: '24.5ms', status: 'HEALTHY' },
    { id: 'agent-01', name: 'monitoring-agent', type: 'AGENT', rps: 300, latency: '2.1ms', status: 'HEALTHY' },
    { id: 'dash-01', name: 'dashboard-service', type: 'BACKEND', rps: 450, latency: '6.5ms', status: 'HEALTHY' },
    { id: 'timescale-01', name: 'TimescaleDB (Hypertables)', type: 'DATABASE', rps: 850, latency: '8.2ms', status: 'HEALTHY' },
    { id: 'redis-01', name: 'Redis Cache L2 / Streams', type: 'CACHE', rps: 2100, latency: '1.8ms', status: 'HEALTHY' },
  ];

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          Mapa Topológico de <span style={{ color: 'var(--purple-light)' }}>Dependências dos Serviços</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Visualização em tempo real das interações entre microsserviços, vazão (RPS) e latência de rede
        </div>
      </div>

      {/* Grid of Nodes */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', marginBottom: '24px' }}>
        {nodes.map(node => (
          <div key={node.id} className="card" style={{ borderLeft: '4px solid var(--purple-light)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
              <div>
                <span className="badge badge-purple" style={{ fontSize: '9px', padding: '2px 6px', marginBottom: '6px', display: 'inline-block' }}>
                  {node.type}
                </span>
                <div style={{ fontSize: '14px', fontWeight: 700 }}>{node.name}</div>
              </div>
              <ShieldCheck size={18} style={{ color: 'var(--green)' }} />
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', background: 'var(--surface)', padding: '10px', borderRadius: '8px' }}>
              <div>
                <div style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Throughput</div>
                <div style={{ fontWeight: 700, color: 'var(--cyan)' }}>{node.rps} req/s</div>
              </div>
              <div>
                <div style={{ fontSize: '10px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Latência Média</div>
                <div style={{ fontWeight: 700, color: 'var(--green)' }}>{node.latency}</div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Dependency Flow */}
      <div className="card">
        <div className="card-header">
          <span className="card-title"><Network size={16} /> Fluxo de Comunicação & Topologia da Arquitetura</span>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', padding: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '13px' }}>
            <span className="badge badge-purple">api-gateway-interceptor</span>
            <ArrowRight size={14} style={{ color: 'var(--text-muted)' }} />
            <span className="badge badge-blue">observability-pipeline</span>
            <span style={{ fontSize: '11px', color: 'var(--cyan)', marginLeft: 'auto', fontFamily: 'JetBrains Mono, monospace' }}>
              1,250 req/s (12.4ms)
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '13px' }}>
            <span className="badge badge-blue">observability-pipeline</span>
            <ArrowRight size={14} style={{ color: 'var(--text-muted)' }} />
            <span className="badge badge-green">storage-layer</span>
            <span style={{ fontSize: '11px', color: 'var(--cyan)', marginLeft: 'auto', fontFamily: 'JetBrains Mono, monospace' }}>
              850 req/s (24.5ms)
            </span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '13px' }}>
            <span className="badge badge-green">storage-layer</span>
            <ArrowRight size={14} style={{ color: 'var(--text-muted)' }} />
            <span className="badge badge-yellow">TimescaleDB</span>
            <span style={{ fontSize: '11px', color: 'var(--cyan)', marginLeft: 'auto', fontFamily: 'JetBrains Mono, monospace' }}>
              850 req/s (8.2ms)
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
