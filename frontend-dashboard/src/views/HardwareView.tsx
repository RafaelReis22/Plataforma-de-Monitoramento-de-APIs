import React from 'react';
import { Cpu, HardDrive, Zap, Network, Activity, Server } from 'lucide-react';
import { HardwareMetrics } from '../types/telemetry';

interface HardwareViewProps {
  data: HardwareMetrics | null;
}

export const HardwareView: React.FC<HardwareViewProps> = ({ data }) => {
  if (!data) return <div style={{ color: 'var(--text-muted)' }}>Carregando telemetria de hardware...</div>;

  return (
    <div>
      {/* Title section */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          Hardware & Host <span style={{ color: 'var(--purple-light)' }}>Overview</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Monitoramento de consumo físico do servidor via agente OSHI (CPU, Memória RAM, Disco e Interface de Rede)
        </div>
      </div>

      {/* Grid top KPIs */}
      <div className="grid-4" style={{ marginBottom: '24px' }}>
        {/* CPU Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Cpu size={16} style={{ color: 'var(--purple-light)' }} /> Uso de CPU</span>
            <span className={`badge ${data.cpuUsagePercentage > 80 ? 'badge-red' : 'badge-green'}`}>
              {data.cpuCores} CORES
            </span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val" style={{ color: data.cpuUsagePercentage > 80 ? 'var(--red-light)' : 'var(--text)' }}>
              {data.cpuUsagePercentage}
            </span>
            <span className="metric-unit">%</span>
          </div>
          <div className="progress-bar-bg">
            <div
              className="progress-bar-fill"
              style={{
                width: `${data.cpuUsagePercentage}%`,
                background: data.cpuUsagePercentage > 80 ? 'var(--red)' : 'linear-gradient(90deg, var(--purple), var(--blue))'
              }}
            />
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-dim)', marginTop: '8px', display: 'flex', justifyContent: 'space-between' }}>
            <span>Load Avg (1m): {data.cpuLoadAverage[0]}</span>
            <span>(5m): {data.cpuLoadAverage[1]}</span>
          </div>
        </div>

        {/* Memory Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Zap size={16} style={{ color: 'var(--blue-light)' }} /> Memória RAM</span>
            <span className="badge badge-blue">{data.memoryTotalGb} GB TOTAL</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.memoryUsagePercentage}</span>
            <span className="metric-unit">%</span>
          </div>
          <div className="progress-bar-bg">
            <div
              className="progress-bar-fill"
              style={{
                width: `${data.memoryUsagePercentage}%`,
                background: 'linear-gradient(90deg, var(--blue), var(--cyan))'
              }}
            />
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-dim)', marginTop: '8px', display: 'flex', justifyContent: 'space-between' }}>
            <span>Usado: {data.memoryUsedGb} GB</span>
            <span>Swap: {data.swapUsedGb} / {data.swapTotalGb} GB</span>
          </div>
        </div>

        {/* Storage Disk Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><HardDrive size={16} style={{ color: 'var(--cyan)' }} /> Armazenamento</span>
            <span className="badge badge-purple">{data.diskTotalGb} GB</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <span className="metric-val">{data.diskUsagePercentage}</span>
            <span className="metric-unit">%</span>
          </div>
          <div className="progress-bar-bg">
            <div
              className="progress-bar-fill"
              style={{
                width: `${data.diskUsagePercentage}%`,
                background: 'linear-gradient(90deg, var(--cyan), var(--green))'
              }}
            />
          </div>
          <div style={{ fontSize: '11px', color: 'var(--text-dim)', marginTop: '8px' }}>
            Livre: {(data.diskTotalGb - data.diskUsedGb).toFixed(1)} GB de {data.diskTotalGb} GB
          </div>
        </div>

        {/* Network I/O Card */}
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Network size={16} style={{ color: 'var(--green-light)' }} /> Rede I/O</span>
            <span className="badge badge-green">ETH0</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '6px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Entrada (Rx):</span>
              <span style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--green-light)' }}>
                {data.networkRxKbps} KB/s
              </span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Saída (Tx):</span>
              <span style={{ fontSize: '16px', fontWeight: 700, fontFamily: 'JetBrains Mono, monospace', color: 'var(--blue-light)' }}>
                {data.networkTxKbps} KB/s
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Core Breakdown */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <span className="card-title"><Activity size={16} /> Carga Individual por Núcleo de Processador (8 Cores)</span>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px' }}>
          {data.cores.map((c) => (
            <div key={c.coreId} style={{ background: 'var(--surface)', padding: '12px 14px', borderRadius: '10px', border: '1px solid var(--border)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', marginBottom: '6px' }}>
                <span style={{ fontWeight: 600, color: 'var(--text-muted)' }}>Core #{c.coreId}</span>
                <span style={{ fontFamily: 'JetBrains Mono, monospace', fontWeight: 700, color: c.usage > 50 ? 'var(--yellow-light)' : 'var(--green-light)' }}>
                  {c.usage}%
                </span>
              </div>
              <div className="progress-bar-bg" style={{ marginTop: '0', height: '6px' }}>
                <div
                  className="progress-bar-fill"
                  style={{
                    width: `${c.usage}%`,
                    background: c.usage > 50 ? 'var(--yellow)' : 'var(--purple)'
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
