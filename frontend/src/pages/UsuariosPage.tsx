import { useCallback, useEffect, useState } from 'react';
import {
  AlertCircle,
  ArrowDown,
  ArrowUp,
  ArrowUpDown,
  Eye,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Trash2,
  Users,
} from 'lucide-react';
import { ConfirmarExclusaoDialog } from '@/components/usuarios/ConfirmarExclusaoDialog';
import { UsuarioDetalheModal } from '@/components/usuarios/UsuarioDetalheModal';
import { UsuarioFormModal } from '@/components/usuarios/UsuarioFormModal';
import { extrairErroApi } from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type {
  DirecaoOrdenacao,
  ListaUsuariosResponse,
  NivelAcesso,
  OrdenacaoUsuarioCampo,
  UsuarioResumo,
} from '@/types/usuario';

const ROTULO_PERFIL: Record<NivelAcesso, string> = {
  ADMINISTRADOR: 'Administrador',
  MEDICO: 'Médico / Funcionário',
  PACIENTE: 'Paciente',
};

const ESTILO_BADGE_PERFIL: Record<NivelAcesso, { container: string; dot: string }> = {
  ADMINISTRADOR: {
    container: 'bg-status-danger-bg border-role-admin-badge text-neutral-text-primary',
    dot: 'bg-role-admin-badge',
  },
  MEDICO: {
    container: 'bg-status-info-bg border-role-medico-badge text-neutral-text-primary',
    dot: 'bg-role-medico-badge',
  },
  PACIENTE: {
    container: 'bg-status-success-bg border-role-paciente-badge text-neutral-text-primary',
    dot: 'bg-role-paciente-badge',
  },
};

export function UsuariosPage() {
  const [termoInput, setTermoInput] = useState('');
  const [termoAplicado, setTermoAplicado] = useState('');
  const [filtroPerfil, setFiltroPerfil] = useState<NivelAcesso | ''>('');
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState<OrdenacaoUsuarioCampo>('nome');
  const [direction, setDirection] = useState<DirecaoOrdenacao>('ASC');

  const [dados, setDados] = useState<ListaUsuariosResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [modalFormAberto, setModalFormAberto] = useState(false);
  const [usuarioEmEdicao, setUsuarioEmEdicao] = useState<UsuarioResumo | null>(null);
  const [usuarioDetalheId, setUsuarioDetalheId] = useState<number | null>(null);
  const [usuarioParaExcluir, setUsuarioParaExcluir] = useState<UsuarioResumo | null>(null);

  const carregarUsuarios = useCallback(async () => {
    setCarregando(true);
    setErro(null);
    try {
      const resposta = await usuarioService.listarUsuarios({
        termo: termoAplicado,
        nivelAcesso: filtroPerfil,
        page,
        size: 10,
        sort,
        direction,
      });
      setDados(resposta);
    } catch (error) {
      setErro(extrairErroApi(error).mensagem);
    } finally {
      setCarregando(false);
    }
  }, [termoAplicado, filtroPerfil, page, sort, direction]);

  useEffect(() => {
    void carregarUsuarios();
  }, [carregarUsuarios]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const normalizado = termoInput.trim().slice(0, 100);
      setTermoAplicado(normalizado);
      setPage(0);
    }, 300);
    return () => window.clearTimeout(timer);
  }, [termoInput]);

  const alternarOrdenacao = (campo: OrdenacaoUsuarioCampo) => {
    if (sort === campo) {
      setDirection((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
    } else {
      setSort(campo);
      setDirection('ASC');
    }
    setPage(0);
  };

  const renderIconeOrdenacao = (campo: OrdenacaoUsuarioCampo) => {
    if (sort !== campo) {
      return <ArrowUpDown className="h-3.5 w-3.5 text-neutral-text-muted" aria-hidden="true" />;
    }
    return direction === 'ASC' ? (
      <ArrowUp className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    ) : (
      <ArrowDown className="h-3.5 w-3.5 text-brand-primary" aria-hidden="true" />
    );
  };

  const abrirNovoUsuario = () => {
    setUsuarioEmEdicao(null);
    setModalFormAberto(true);
  };

  const abrirEdicao = (usuario: UsuarioResumo) => {
    setUsuarioEmEdicao(usuario);
    setModalFormAberto(true);
  };

  const totalizadores = dados?.totalizadores ?? {
    total: 0,
    administradores: 0,
    medicos: 0,
    pacientes: 0,
  };

  return (
    <div className="space-y-lg">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-md">
        <div>
          <h1 className="text-2xl font-bold text-neutral-text-primary">Gestão de Usuários</h1>
          <p className="text-sm text-neutral-text-secondary">
            Cadastre, consulte e gerencie administradores, profissionais e pacientes ativos
          </p>
        </div>

        <button
          type="button"
          onClick={abrirNovoUsuario}
          className="inline-flex items-center justify-center gap-2 rounded-md bg-brand-primary px-4 py-2.5 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover transition-colors"
        >
          <Plus className="h-4 w-4" aria-hidden="true" />
          Novo Usuário
        </button>
      </div>

      {carregando && !dados ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-md" aria-busy="true">
          {[1, 2, 3, 4].map((item) => (
            <div
              key={item}
              className="h-24 rounded-lg border border-neutral-border bg-neutral-surface p-md animate-pulse"
            >
              <div className="h-4 w-1/2 rounded bg-neutral-border" />
              <div className="mt-3 h-7 w-1/3 rounded bg-neutral-border" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-md">
          <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm">
            <p className="text-xs font-medium text-neutral-text-secondary">Total de Usuários</p>
            <p className="mt-xs text-2xl font-bold text-neutral-text-primary">{totalizadores.total}</p>
          </div>
          <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm">
            <p className="text-xs font-medium text-neutral-text-secondary">Administradores</p>
            <p className="mt-xs text-2xl font-bold text-role-admin-badge">{totalizadores.administradores}</p>
          </div>
          <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm">
            <p className="text-xs font-medium text-neutral-text-secondary">Médicos / Funcionários</p>
            <p className="mt-xs text-2xl font-bold text-role-medico-badge">{totalizadores.medicos}</p>
          </div>
          <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm">
            <p className="text-xs font-medium text-neutral-text-secondary">Pacientes</p>
            <p className="mt-xs text-2xl font-bold text-role-paciente-badge">{totalizadores.pacientes}</p>
          </div>
        </div>
      )}

      <div className="rounded-lg border border-neutral-border bg-neutral-surface p-md shadow-sm space-y-md">
        <div className="flex flex-col md:flex-row gap-md">
          <div className="relative flex-1">
            <label htmlFor="busca-usuarios" className="sr-only">
              Buscar por nome ou e-mail
            </label>
            <Search
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-neutral-text-muted"
              aria-hidden="true"
            />
            <input
              id="busca-usuarios"
              type="search"
              maxLength={100}
              value={termoInput}
              onChange={(e) => setTermoInput(e.target.value)}
              placeholder="Buscar por nome ou e-mail..."
              className="w-full rounded-md border border-neutral-border bg-neutral-surface pl-9 pr-3 py-2 text-sm text-neutral-text-primary placeholder:text-neutral-text-muted"
            />
          </div>

          <div className="w-full md:w-64">
            <label htmlFor="filtro-perfil" className="sr-only">
              Filtrar por perfil
            </label>
            <select
              id="filtro-perfil"
              value={filtroPerfil}
              onChange={(e) => {
                setFiltroPerfil(e.target.value as NivelAcesso | '');
                setPage(0);
              }}
              className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm text-neutral-text-primary"
            >
              <option value="">Todos os perfis</option>
              <option value="ADMINISTRADOR">Administrador</option>
              <option value="MEDICO">Médico / Funcionário</option>
              <option value="PACIENTE">Paciente</option>
            </select>
          </div>
        </div>

        {carregando ? (
          <div className="space-y-sm py-sm" aria-busy="true" aria-label="Carregando usuários">
            {[1, 2, 3, 4, 5].map((linha) => (
              <div key={linha} className="h-12 rounded-md bg-neutral-background animate-pulse" />
            ))}
          </div>
        ) : erro ? (
          <div
            role="alert"
            className="rounded-md border border-status-danger bg-status-danger-bg p-lg text-center space-y-sm"
          >
            <div className="mx-auto flex h-10 w-10 items-center justify-center rounded-full bg-neutral-surface text-status-danger">
              <AlertCircle className="h-5 w-5" aria-hidden="true" />
            </div>
            <p className="text-sm font-medium text-status-danger">{erro}</p>
            <button
              type="button"
              onClick={() => void carregarUsuarios()}
              className="inline-flex items-center gap-2 rounded-md bg-brand-primary px-4 py-2 text-sm font-medium text-neutral-surface hover:bg-brand-primary-hover"
            >
              <RefreshCw className="h-4 w-4" aria-hidden="true" />
              Tentar novamente
            </button>
          </div>
        ) : !dados || dados.itens.length === 0 ? (
          <div className="py-xl text-center space-y-sm">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-neutral-background text-neutral-text-muted">
              <Users className="h-6 w-6" aria-hidden="true" />
            </div>
            <h2 className="text-base font-semibold text-neutral-text-primary">Nenhum usuário encontrado</h2>
            <p className="text-sm text-neutral-text-secondary">
              Não há registros ativos para os filtros informados.
            </p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm">
                <thead>
                  <tr className="border-b border-neutral-border text-xs font-semibold uppercase tracking-wide text-neutral-text-secondary">
                    <th className="py-3 px-3">
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('nome')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Nome {renderIconeOrdenacao('nome')}
                      </button>
                    </th>
                    <th className="py-3 px-3">
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('email')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        E-mail {renderIconeOrdenacao('email')}
                      </button>
                    </th>
                    <th className="py-3 px-3">
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('nivelAcesso')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Perfil {renderIconeOrdenacao('nivelAcesso')}
                      </button>
                    </th>
                    <th className="py-3 px-3">Celular</th>
                    <th className="py-3 px-3">
                      <button
                        type="button"
                        onClick={() => alternarOrdenacao('criadoEm')}
                        className="inline-flex items-center gap-1 hover:text-neutral-text-primary"
                      >
                        Cadastro {renderIconeOrdenacao('criadoEm')}
                      </button>
                    </th>
                    <th className="py-3 px-3 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-neutral-border">
                  {dados.itens.map((item) => {
                    const badge = ESTILO_BADGE_PERFIL[item.nivelAcesso];
                    return (
                      <tr key={item.id} className="hover:bg-neutral-background/70">
                        <td className="py-3 px-3 font-medium text-neutral-text-primary">{item.nome}</td>
                        <td className="py-3 px-3 text-neutral-text-secondary">{item.email}</td>
                        <td className="py-3 px-3">
                          <span
                            className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-medium ${badge.container}`}
                          >
                            <span className={`h-2 w-2 rounded-full ${badge.dot}`} aria-hidden="true" />
                            {ROTULO_PERFIL[item.nivelAcesso]}
                          </span>
                        </td>
                        <td className="py-3 px-3 font-mono text-xs text-neutral-text-secondary">
                          {item.numCelular || '—'}
                        </td>
                        <td className="py-3 px-3 text-neutral-text-secondary">
                          {new Date(item.criadoEm).toLocaleDateString('pt-BR')}
                        </td>
                        <td className="py-3 px-3">
                          <div className="flex items-center justify-end gap-1">
                            <button
                              type="button"
                              onClick={() => setUsuarioDetalheId(item.id)}
                              className="rounded p-1.5 text-neutral-text-secondary hover:bg-status-info-bg hover:text-brand-secondary"
                              aria-label={`Visualizar detalhes de ${item.nome}`}
                              title="Visualizar detalhes"
                            >
                              <Eye className="h-4 w-4" aria-hidden="true" />
                            </button>
                            <button
                              type="button"
                              onClick={() => abrirEdicao(item)}
                              className="rounded p-1.5 text-neutral-text-secondary hover:bg-status-info-bg hover:text-brand-primary"
                              aria-label={`Editar usuário ${item.nome}`}
                              title="Editar"
                            >
                              <Pencil className="h-4 w-4" aria-hidden="true" />
                            </button>
                            <button
                              type="button"
                              onClick={() => setUsuarioParaExcluir(item)}
                              className="rounded p-1.5 text-neutral-text-secondary hover:bg-status-danger-bg hover:text-status-danger"
                              aria-label={`Inativar usuário ${item.nome}`}
                              title="Inativar"
                            >
                              <Trash2 className="h-4 w-4" aria-hidden="true" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

            <div className="flex flex-col sm:flex-row items-center justify-between gap-sm pt-sm border-t border-neutral-border text-sm">
              <p className="text-neutral-text-secondary">
                Exibindo {dados.itens.length} de {dados.totalItens} usuários ativos
              </p>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={dados.pagina <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Anterior
                </button>
                <span className="text-xs font-medium text-neutral-text-secondary px-2">
                  Página {dados.pagina + 1} de {Math.max(1, dados.totalPaginas)}
                </span>
                <button
                  type="button"
                  disabled={dados.pagina + 1 >= dados.totalPaginas}
                  onClick={() => setPage((p) => p + 1)}
                  className="rounded-md border border-neutral-border bg-neutral-surface px-3 py-1.5 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Próxima
                </button>
              </div>
            </div>
          </>
        )}
      </div>

      <UsuarioFormModal
        aberto={modalFormAberto}
        usuarioParaEditar={usuarioEmEdicao}
        onFechar={() => setModalFormAberto(false)}
        onSalvo={() => {
          setModalFormAberto(false);
          void carregarUsuarios();
        }}
      />

      <UsuarioDetalheModal
        usuarioId={usuarioDetalheId}
        onFechar={() => setUsuarioDetalheId(null)}
      />

      <ConfirmarExclusaoDialog
        usuario={usuarioParaExcluir}
        onFechar={() => setUsuarioParaExcluir(null)}
        onExcluido={() => {
          setUsuarioParaExcluir(null);
          void carregarUsuarios();
        }}
      />
    </div>
  );
}
