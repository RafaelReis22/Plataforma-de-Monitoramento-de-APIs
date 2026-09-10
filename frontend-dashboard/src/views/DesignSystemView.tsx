import React from 'react';
import { Layers, Palette, Type, Shield, Sparkles, FileText } from 'lucide-react';

export const DesignSystemView: React.FC = () => {
  return (
    <div>
      {/* Title */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ fontSize: '22px', fontWeight: 800, letterSpacing: '-0.02em', marginBottom: '4px' }}>
          Design <span style={{ color: 'var(--purple-light)' }}>System</span> Tokens & Spec
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
          Padrões visuais, cores semânticas, tipografia e documentação OpenAPI 3.0 da plataforma
        </div>
      </div>

      {/* Color Palette Grid */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <span className="card-title"><Palette size={16} /> Paleta Semântica — Cores de Status & Superfícies</span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '12px' }}>
          <div style={{ background: '#10b981', height: '70px', borderRadius: '10px', padding: '10px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            <span style={{ fontWeight: 700, fontSize: '12px', color: '#000' }}>Success</span>
            <span style={{ fontSize: '10px', color: '#000', fontFamily: 'JetBrains Mono, monospace' }}>#10b981</span>
          </div>

          <div style={{ background: '#f59e0b', height: '70px', borderRadius: '10px', padding: '10px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            <span style={{ fontWeight: 700, fontSize: '12px', color: '#000' }}>Warning</span>
            <span style={{ fontSize: '10px', color: '#000', fontFamily: 'JetBrains Mono, monospace' }}>#f59e0b</span>
          </div>

          <div style={{ background: '#ef4444', height: '70px', borderRadius: '10px', padding: '10px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            <span style={{ fontWeight: 700, fontSize: '12px', color: '#fff' }}>Danger</span>
            <span style={{ fontSize: '10px', color: '#fff', fontFamily: 'JetBrains Mono, monospace' }}>#ef4444</span>
          </div>

          <div style={{ background: '#3b82f6', height: '70px', borderRadius: '10px', padding: '10px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            <span style={{ fontWeight: 700, fontSize: '12px', color: '#fff' }}>Info / Blue</span>
            <span style={{ fontSize: '10px', color: '#fff', fontFamily: 'JetBrains Mono, monospace' }}>#3b82f6</span>
          </div>

          <div style={{ background: '#8b5cf6', height: '70px', borderRadius: '10px', padding: '10px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
            <span style={{ fontWeight: 700, fontSize: '12px', color: '#fff' }}>Accent Purple</span>
            <span style={{ fontSize: '10px', color: '#fff', fontFamily: 'JetBrains Mono, monospace' }}>#8b5cf6</span>
          </div>
        </div>
      </div>

      {/* Badges & Buttons Showcase */}
      <div className="grid-2" style={{ marginBottom: '24px' }}>
        <div className="card">
          <div className="card-header">
            <span className="card-title"><Sparkles size={16} /> Badges Semânticos</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            <span className="badge badge-green">HEALTHY 100%</span>
            <span className="badge badge-yellow">WARNING DEGRADED</span>
            <span className="badge badge-red">CRITICAL FIRING</span>
            <span className="badge badge-blue">METRIC GAUGE</span>
            <span className="badge badge-purple">JWT AUTHENTICATED</span>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title"><Type size={16} /> Componentes de Ação</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
            <button className="btn btn-primary">Botão Primário</button>
            <button className="btn btn-secondary">Botão Secundário</button>
            <button className="btn btn-danger">Ação Perigosa</button>
          </div>
        </div>
      </div>

      {/* OpenAPI Documentation Card */}
      <div className="card">
        <div className="card-header">
          <span className="card-title"><FileText size={16} /> Documentação OpenAPI 3.0 / Swagger UI</span>
        </div>
        <div style={{ fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.6' }}>
          Todos os microsserviços da plataforma expõem contrato interativo via OpenAPI 3.0.
          Você pode acessar e testar a API REST diretamente através dos links de diagnóstico:
        </div>
        <div style={{ display: 'flex', gap: '12px', marginTop: '14px' }}>
          <a
            href="http://localhost:8085/actuator/health"
            target="_blank"
            rel="noreferrer"
            className="btn btn-secondary"
            style={{ fontSize: '12px' }}
          >
            Dashboard Service Actuator Health
          </a>
          <a
            href="http://localhost:8081/actuator/prometheus"
            target="_blank"
            rel="noreferrer"
            className="btn btn-secondary"
            style={{ fontSize: '12px' }}
          >
            Agent Telemetry Endpoint
          </a>
        </div>
      </div>
    </div>
  );
};
