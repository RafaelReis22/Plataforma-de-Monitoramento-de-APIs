import React, { useState } from 'react';
import { X, Lock, Key, ShieldCheck, UserCheck, AlertCircle } from 'lucide-react';
import { UserProfile } from '../types/telemetry';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  user: UserProfile | null;
  onLoginSuccess: (user: UserProfile, token: string) => void;
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  onClose,
  user,
  onLoginSuccess
}) => {
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');

    try {
      const res = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.erro || 'Falha na autenticação');
      }

      const data = await res.json();
      const profile: UserProfile = {
        username: data.username,
        role: data.role,
        tenantId: data.tenantId,
        email: `${data.username}@monitoring-platform.io`,
        organization: 'Enterprise Telemetry Corp'
      };

      onLoginSuccess(profile, data.token);
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'Erro ao realizar login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ background: 'var(--purple-dim)', padding: '8px', borderRadius: '10px', color: 'var(--purple-light)' }}>
              <Lock size={20} />
            </div>
            <div>
              <h3 style={{ fontSize: '16px', fontWeight: 700 }}>Autenticação IAM & Seguranças</h3>
              <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Gerenciamento de acesso e tokens JWT</p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        {user ? (
          <div style={{ background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '12px', padding: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '14px' }}>
              <UserCheck size={28} style={{ color: 'var(--green)' }} />
              <div>
                <div style={{ fontWeight: 700, fontSize: '14px' }}>{user.username}</div>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{user.email}</div>
              </div>
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div><strong>Organização:</strong> {user.organization}</div>
              <div><strong>Tenant ID:</strong> <code style={{ color: 'var(--purple-light)' }}>{user.tenantId}</code></div>
              <div><strong>Permissão:</strong> <span className="badge badge-green">{user.role}</span></div>
            </div>
          </div>
        ) : (
          <form onSubmit={handleLogin}>
            {errorMsg && (
              <div style={{ background: 'var(--red-dim)', border: '1px solid var(--red)', color: 'var(--red-light)', padding: '10px 14px', borderRadius: '8px', fontSize: '12px', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <AlertCircle size={16} />
                <span>{errorMsg}</span>
              </div>
            )}

            <div className="form-group">
              <label className="form-label">Usuário / Email</label>
              <input
                type="text"
                className="form-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Senha de Acesso</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <span style={{ fontSize: '10px', color: 'var(--text-dim)', marginTop: '4px', display: 'block' }}>
                Dica de demonstração: usuário <code>admin</code> / senha <code>admin123</code>
              </span>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '24px' }}>
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Cancelar
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Autenticando...' : 'Entrar com JWT'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};
