import { useEffect, useState } from 'react';
import { AlertCircle, Eye, X } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type { GrauSuporte, NivelAcesso, UsuarioDetalhe } from '@/types/usuario';

interface UsuarioDetalheModalProps {
  usuarioId: number | null;
  onFechar: () => void;
}

const ROTULO_PERFIL: Record<NivelAcesso, string> = {
  ADMINISTRADOR: 'Administrador',
  MEDICO: 'Médico / Funcionário',
  PACIENTE: 'Paciente',
};

const ROTULO_SUPORTE: Record<GrauSuporte, string> = {
  NIVEL_1: 'Nível 1 (Suporte leve)',
  NIVEL_2: 'Nível 2 (Suporte substancial)',
  NIVEL_3: 'Nível 3 (Suporte muito substancial)',
};

export function UsuarioDetalheModal({ usuarioId, onFechar }: UsuarioDetalheModalProps) {
  const [detalhe, setDetalhe] = useState<UsuarioDetalhe | null>(null);
  const [carregando, setCarregando] = useState(false);
  const [revelando, setRevelando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    if (usuarioId == null) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [usuarioId, onFechar]);

  useEffect(() => {
    if (usuarioId == null) {
      setDetalhe(null);
      return;
    }

    let ativo = true;
    setCarregando(true);
    setErro(null);
    usuarioService
      .consultarUsuarioPorId(usuarioId, false)
      .then((dados) => {
        if (ativo) {
          setDetalhe(dados);
        }
      })
      .catch((error) => {
        if (ativo) {
          setErro(extrairErroApi(error).mensagem);
        }
      })
      .finally(() => {
        if (ativo) {
          setCarregando(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, [usuarioId]);

  if (usuarioId == null) {
    return null;
  }

  const handleRevelarCelular = async () => {
    setRevelando(true);
    setErro(null);
    try {
      const atualizado = await usuarioService.consultarUsuarioPorId(usuarioId, true);
      setDetalhe(atualizado);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setRevelando(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="detalhe-usuario-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-lg rounded-lg bg-neutral-surface border border-neutral-border shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <h2 id="detalhe-usuario-titulo" className="text-lg font-semibold text-neutral-text-primary">
            Detalhes do Usuário
          </h2>
          <button
            type="button"
            onClick={onFechar}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar detalhes"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-lg space-y-md">
          {erro && (
            <div
              role="alert"
              className="flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erro}</span>
            </div>
          )}

          {carregando ? (
            <div className="space-y-sm" aria-busy="true">
              <div className="h-5 w-2/3 rounded bg-neutral-border animate-pulse" />
              <div className="h-4 w-1/2 rounded bg-neutral-border animate-pulse" />
              <div className="h-20 rounded bg-neutral-border animate-pulse" />
            </div>
          ) : detalhe ? (
            <dl className="grid grid-cols-1 sm:grid-cols-2 gap-md text-sm">
              <div>
                <dt className="text-xs font-medium text-neutral-text-secondary">Nome</dt>
                <dd className="mt-0.5 font-medium text-neutral-text-primary">{detalhe.nome}</dd>
              </div>
              <div>
                <dt className="text-xs font-medium text-neutral-text-secondary">Perfil</dt>
                <dd className="mt-0.5 font-medium text-neutral-text-primary">
                  {ROTULO_PERFIL[detalhe.nivelAcesso]}
                </dd>
              </div>
              <div className="sm:col-span-2">
                <dt className="text-xs font-medium text-neutral-text-secondary">E-mail</dt>
                <dd className="mt-0.5 text-neutral-text-primary">{detalhe.email}</dd>
              </div>
              <div>
                <dt className="text-xs font-medium text-neutral-text-secondary">Celular</dt>
                <dd className="mt-0.5 flex items-center gap-2 font-mono text-neutral-text-primary">
                  <span>{detalhe.numCelular || 'Não informado'}</span>
                  {detalhe.numCelular && detalhe.celularMascarado && (
                    <button
                      type="button"
                      onClick={handleRevelarCelular}
                      disabled={revelando}
                      className="inline-flex items-center gap-1 rounded border border-brand-secondary px-2 py-0.5 font-sans text-xs font-medium text-brand-secondary hover:bg-status-info-bg disabled:opacity-50"
                    >
                      <Eye className="h-3.5 w-3.5" aria-hidden="true" />
                      {revelando ? 'Revelando...' : 'Revelar celular'}
                    </button>
                  )}
                </dd>
              </div>
              <div>
                <dt className="text-xs font-medium text-neutral-text-secondary">Data de nascimento</dt>
                <dd className="mt-0.5 text-neutral-text-primary">
                  {detalhe.dataNascimento
                    ? new Date(`${detalhe.dataNascimento}T00:00:00`).toLocaleDateString('pt-BR')
                    : 'Não informada'}
                </dd>
              </div>
              <div className="sm:col-span-2">
                <dt className="text-xs font-medium text-neutral-text-secondary">Endereço</dt>
                <dd className="mt-0.5 text-neutral-text-primary">
                  {[detalhe.rua, detalhe.cidade, detalhe.estado, detalhe.pais].filter(Boolean).join(', ') ||
                    'Não informado'}
                </dd>
              </div>

              {detalhe.funcionario && (
                <div className="sm:col-span-2 rounded-md border border-neutral-border bg-neutral-background p-sm">
                  <p className="text-xs font-semibold text-neutral-text-secondary mb-1">
                    Dados Profissionais
                  </p>
                  <p className="text-sm text-neutral-text-primary">
                    <strong>Cargo:</strong> {detalhe.funcionario.cargo}
                  </p>
                  {detalhe.funcionario.crm && (
                    <p className="text-sm text-neutral-text-primary">
                      <strong>Registro:</strong> {detalhe.funcionario.crm}
                    </p>
                  )}
                </div>
              )}

              {detalhe.paciente && (
                <div className="sm:col-span-2 rounded-md border border-neutral-border bg-neutral-background p-sm">
                  <p className="text-xs font-semibold text-neutral-text-secondary mb-1">
                    Dados Clínicos
                  </p>
                  <p className="text-sm text-neutral-text-primary">
                    <strong>Grau de suporte:</strong> {ROTULO_SUPORTE[detalhe.paciente.grauSuporte]}
                  </p>
                  {detalhe.paciente.descricao && (
                    <p className="mt-1 text-sm text-neutral-text-primary">
                      <strong>Observações:</strong> {detalhe.paciente.descricao}
                    </p>
                  )}
                </div>
              )}
            </dl>
          ) : null}
        </div>

        <div className="flex justify-end px-lg py-md border-t border-neutral-border">
          <button
            type="button"
            onClick={onFechar}
            className="rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}
