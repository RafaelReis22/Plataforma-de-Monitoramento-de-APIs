import React, { useState, useEffect } from 'react';
import { GitBranch, Search, FileText, Server, Clock, Layers, Filter } from 'lucide-react';

export const TracesLogsView: React.FC = () => {
  const [traceIdInput, setTraceIdInput] = useState('4c9b809a128e45f9a012345678abcdef');
  const [activeTrace, setActiveTrace] = useState<any | null>(null);
  const [correlatedLogs, setCorrelatedLogs] = useState<any[]>([]);

  const fetchTraceData = async (tid: string) => {
    try {
      const [traceRes, logsRes] = await Promise.all([
        fetch(`/api/v1/telemetry/traces?traceId=${tid}`).then(r => r.json()),
        fetch(`/api/v1/telemetry/logs?traceId=${tid}`).then(r => r.json())
      ]);
      setActiveTrace(traceRes);
      setCorrelatedLogs(logsRes);
    } catch (e) {
      console.warn("Error fetching trace & log telemetry", e);
    }
  };

  useEffect(() => {
    fetchTraceData(traceIdInput);
  }, []);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchTraceData(traceIdInput);
  };

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
            Tracing Distribuído & <span style={{ color: 'var(--cyan)' }}>Correlação de Logs</span>
          </div>
          <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            Visualização de Spans OpenTelemetry (W3C traceparent) e busca de logs SLF4J correlacionados por TraceId
          </div>
        </div>

        {/* Search Bar */}
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '8px' }}>
          <div style={{ position: 'relative' }}>
            <Search size={14} style={{ position: 'absolute', left: '10px', top: '12px', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-input"
              value={traceIdInput}
              onChange={(e) => setTraceIdInput(e.target.value)}
              placeholder="Digite o TraceId..."
              style={{ paddingLeft: '32px', width: '320px', fontFamily: 'JetBrains Mono, monospace', fontSize: '12px' }}
            />
          </div>
          <button type="submit" className="btn btn-primary">
            Buscar Trace
          </button>
        </form>
      </div>

      {/* Trace Overview Summary */}
      {activeTrace && (
        <div className="card" style={{ marginBottom: '24px' }}>
          <div className="card-header">
            <span className="card-title">
              <GitBranch size={16} style={{ color: 'var(--purple-light)' }} /> Trace ID: 
              <code style={{ color: 'var(--purple-light)', marginLeft: '6px' }}>{activeTrace.traceId}</code>
            </span>
            <span className="badge badge-purple">{activeTrace.servicesCount} MICROSSERVIÇOS</span>
          </div>

          {/* Span Waterfall Timeline */}
          <div style={{ background: 'var(--surface)', padding: '16px', borderRadius: '12px', border: '1px solid var(--border)', marginBottom: '16px' }}>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '12px', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 700 }}>
              Waterfall Diagram de Duração de Spans (Duração Total: {activeTrace.totalDurationMs}ms)
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {activeTrace.spans.map((span: any, idx: number) => {
                const offsetPct = idx === 0 ? 0 : idx === 1 ? 25 : 55;
                const widthPct = idx === 0 ? 100 : idx === 1 ? 40 : 35;
                return (
                  <div key={span.spanId} style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '12px' }}>
                    <div style={{ width: '180px', fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      <span className="badge badge-blue" style={{ fontSize: '9px', padding: '1px 5px', marginRight: '6px' }}>
                        {span.service}
                      </span>
                    </div>

                    <div style={{ flex: 1, background: 'var(--border-subtle)', height: '24px', borderRadius: '6px', position: 'relative', overflow: 'hidden' }}>
                      <div
                        style={{
                          position: 'absolute',
                          left: `${offsetPct}%`,
                          width: `${widthPct}%`,
                          height: '100%',
                          background: idx === 0 ? 'linear-gradient(90deg, var(--purple), var(--blue))' :
                                      idx === 1 ? 'linear-gradient(90deg, var(--blue), var(--cyan))' : 'linear-gradient(90deg, var(--cyan), var(--green))',
                          borderRadius: '4px',
                          display: 'flex',
                          alignItems: 'center',
                          paddingLeft: '8px',
                          fontSize: '10px',
                          color: '#fff',
                          fontWeight: 700,
                          fontFamily: 'JetBrains Mono, monospace'
                        }}
                      >
                        {span.name} ({span.durationMs}ms)
                      </div>
                    </div>

                    <div style={{ width: '60px', textAlign: 'right', fontFamily: 'JetBrains Mono, monospace', fontSize: '11px', color: 'var(--text-muted)' }}>
                      {span.durationMs}ms
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Correlated Logs Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title"><FileText size={16} /> Logs Correlacionados por MDC SLF4J</span>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Nível</th>
                <th>Serviço</th>
                <th>TraceId</th>
                <th>Mensagem de Log</th>
              </tr>
            </thead>
            <tbody>
              {correlatedLogs.map((logItem, idx) => (
                <tr key={idx}>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '11px', color: 'var(--text-muted)' }}>
                    {logItem.timestamp}
                  </td>
                  <td>
                    <span className={`badge ${logItem.level === 'ERROR' ? 'badge-red' : logItem.level === 'WARN' ? 'badge-yellow' : 'badge-green'}`}>
                      {logItem.level}
                    </span>
                  </td>
                  <td style={{ fontWeight: 600 }}>{logItem.service}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '11px', color: 'var(--purple-light)' }}>
                    {logItem.traceId}
                  </td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: '12px' }}>
                    {logItem.message}
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
