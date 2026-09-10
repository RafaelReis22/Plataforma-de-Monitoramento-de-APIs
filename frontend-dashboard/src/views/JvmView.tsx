import React from 'react';
import { Server, Layers, Cpu, RefreshCw, Box } from 'lucide-react';
import { JvmMetrics } from '../types/telemetry';

interface JvmViewProps {
  data: JvmMetrics | null;
}

export const JvmView: React.FC<JvmViewProps> = ({ data }) => {
  if (!data) return <div style={{ color: 'var(--text-muted)' }}>Carregando métricas da JVM...</div>;

  const heapPct = Math.round((data.heapUsedMb / data.heapMaxMb) * 100);

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          JVM Internals & <span style={{ color: 'var(--purple-light)' }}>Runtime Java 21</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Diagnóstico em tempo real da Máquina Virtual Java: Heap, Metaspace, GC Pause, Threads e Carregamento de Classes
        </div>
      </div>

      {/* Grid top KPIs */}
      <div className="grid-4" style={{ marginBottom: '24px' }}>
        {/* Heap Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Layers size={16} style={{ color: 'var(--purple-light)' }} /> Memória Heap</span>
            <span className="badge badge-purple">{data.heapMaxMb} MB MAX</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.heapUsedMb}</span>
            <span className="metric-unit">MB</span>
          </div>
          <div className="progress-bar-bg">
            <div
              className="progress-bar-fill"
              style={{
                width: `${heapPct}%`,
                background: heapPct > 80 ? 'var(--red)' : 'linear-gradient(90deg, var(--purple), var(--cyan))'
              }}
            />
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px', display: 'flex', justifyContent: 'space-between' }}>
            <span>Uso: {heapPct}%</span>
            <span>Committed: {data.heapCommittedMb} MB</span>
          </div>
        </div>

        {/* Non-Heap Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Box size={16} style={{ color: 'var(--cyan)' }} /> Non-Heap / Metaspace</span>
            <span className="badge badge-purple">STABLE</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.nonHeapUsedMb}</span>
            <span className="metric-unit">MB</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '12px' }}>
            Metaspace, Compressed Class Space e CodeCache
          </div>
        </div>

        {/* Threads Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Cpu size={16} style={{ color: 'var(--green-light)' }} /> Threads Ativas</span>
            <span className="badge badge-green">PICO: {data.peakThreads}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.activeThreads}</span>
            <span className="metric-unit">threads</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Threads Daemon: <strong>{data.daemonThreads}</strong>
          </div>
        </div>

        {/* GC Pause Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><RefreshCw size={16} style={{ color: 'var(--yellow-light)' }} /> Pause de GC</span>
            <span className="badge badge-yellow">G1 GC</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.gcPauseTotalMs}</span>
            <span className="metric-unit">ms</span>
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>
            Total de coleções: <strong>{data.gcCollectionsCount}</strong>
          </div>
        </div>
      </div>

      {/* Runtime details card */}
      <div className="card">
        <div className="card-header">
          <span className="card-title"><Server size={16} /> Informações do Ambiente de Execução Java</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
          <div style={{ background: 'var(--surface)', padding: '14px', borderRadius: '10px', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Versão do Runtime</div>
            <div style={{ fontSize: '13px', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--purple-light)' }}>
              {data.javaVersion}
            </div>
          </div>
          <div style={{ background: 'var(--surface)', padding: '14px', borderRadius: '10px', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Tempo de Uptime da JVM</div>
            <div style={{ fontSize: '13px', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--green-light)' }}>
              {data.jvmUptimeHours} horas seguidas
            </div>
          </div>
          <div style={{ background: 'var(--surface)', padding: '14px', borderRadius: '10px', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>Classes Carregadas</div>
            <div style={{ fontSize: '13px', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--blue-light)' }}>
              {data.loadedClasses.toLocaleString()} classes ativas
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
