import React, { useState } from 'react';
import { Key, Copy, Check, Plus, Code, Terminal, Layers } from 'lucide-react';

export const ApiKeysView: React.FC = () => {
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [selectedSdk, setSelectedSdk] = useState<'spring' | 'express' | 'fastapi'>('spring');
  const [keys, setKeys] = useState([
    { key: 'ak_live_prod_9876543210abcdef', name: 'Production Cluster Ingestion Key', date: '2026-09-14' },
    { key: 'ak_test_stage_1234567890fedcba', name: 'Staging Environment Telemetry Key', date: '2026-09-10' }
  ]);

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(text);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const handleCreateKey = () => {
    const newKey = `ak_live_${Math.random().toString(36).substring(2, 18)}`;
    setKeys([...keys, { key: newKey, name: `Chave de Ingestão #${keys.length + 1}`, date: new Date().toISOString().substring(0, 10) }]);
  };

  const springSnippet = `// 1. Adicionar interceptor no WebMvcConfigurer do seu Spring Boot
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private HttpMetricsInterceptor metricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/api/**");
    }
}

// Cabeçalho de Autenticação de Telemetria:
// X-API-KEY: ${keys[0]?.key || 'ak_live_prod_9876...'}
`;

  const expressSnippet = `// 2. Middleware Node.js Express
const express = require('express');
const app = express();

const TELEMETRY_KEY = '${keys[0]?.key || 'ak_live_prod_9876...'}';

app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    // Envia telemetria em background para a plataforma
    fetch('http://localhost:8085/api/v1/telemetry/ingest', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'X-API-KEY': TELEMETRY_KEY 
      },
      body: JSON.stringify({
        path: req.path,
        method: req.method,
        statusCode: res.statusCode,
        durationMs: duration
      })
    }).catch(() => {});
  });
  next();
});`;

  const fastapiSnippet = `# 3. Middleware Python FastAPI
from fastapi import FastAPI, Request
import time, requests

app = FastAPI()
TELEMETRY_KEY = "${keys[0]?.key || 'ak_live_prod_9876...'}"

@app.middleware("http")
async def monitor_telemetry(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = (time.time() - start_time) * 1000
    
    # Enviar telemetria de forma assíncrona
    try:
        requests.post("http://localhost:8085/api/v1/telemetry/ingest", json={
            "path": request.url.path,
            "method": request.method,
            "statusCode": response.status_code,
            "durationMs": process_time
        }, headers={"X-API-KEY": TELEMETRY_KEY}, timeout=0.5)
    except:
        pass
        
    return response`;

  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
            API Keys & <span style={{ color: 'var(--purple-light)' }}>SDKs de Integração</span>
          </div>
          <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            Gere chaves de API para registrar novas aplicações e copie trechos de código prontos para instrumentação
          </div>
        </div>

        <button className="btn btn-primary" onClick={handleCreateKey}>
          <Plus size={16} /> Gerar Nova API Key
        </button>
      </div>

      {/* Keys List */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <span className="card-title"><Key size={16} /> Chaves de API Cadastradas (`X-API-KEY`)</span>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome da Chave</th>
                <th>Valor da API Key</th>
                <th>Data Criação</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {keys.map((k, idx) => (
                <tr key={idx}>
                  <td style={{ fontWeight: 600 }}>{k.name}</td>
                  <td style={{ fontFamily: 'JetBrains Mono, monospace', color: 'var(--purple-light)', fontWeight: 600 }}>
                    {k.key}
                  </td>
                  <td style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{k.date}</td>
                  <td>
                    <button
                      className="btn btn-secondary"
                      onClick={() => handleCopy(k.key)}
                      style={{ padding: '4px 10px', fontSize: '11px' }}
                    >
                      {copiedKey === k.key ? <Check size={14} style={{ color: 'var(--green-light)' }} /> : <Copy size={14} />}
                      <span>{copiedKey === k.key ? 'Copiado!' : 'Copiar'}</span>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Code Snippets Section */}
      <div className="card">
        <div className="card-header">
          <span className="card-title"><Code size={16} /> Guia de Instrumentação Rápida por Linguagem</span>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              className={`btn ${selectedSdk === 'spring' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setSelectedSdk('spring')}
              style={{ fontSize: '11px', padding: '4px 10px' }}
            >
              Spring Boot (Java)
            </button>
            <button
              className={`btn ${selectedSdk === 'express' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setSelectedSdk('express')}
              style={{ fontSize: '11px', padding: '4px 10px' }}
            >
              Node.js (Express)
            </button>
            <button
              className={`btn ${selectedSdk === 'fastapi' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setSelectedSdk('fastapi')}
              style={{ fontSize: '11px', padding: '4px 10px' }}
            >
              Python (FastAPI)
            </button>
          </div>
        </div>

        <div className="code-box">
          <pre>
            {selectedSdk === 'spring' && springSnippet}
            {selectedSdk === 'express' && expressSnippet}
            {selectedSdk === 'fastapi' && fastapiSnippet}
          </pre>
        </div>
      </div>
    </div>
  );
};
