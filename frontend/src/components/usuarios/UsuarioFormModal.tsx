import { useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, X } from 'lucide-react';
import { extrairErroApi } from '@/services/api';
import { usuarioService } from '@/services/usuarioService';
import type {
  AtualizarUsuarioRequest,
  CadastrarUsuarioRequest,
  GrauSuporte,
  NivelAcesso,
  UsuarioResumo,
} from '@/types/usuario';

interface UsuarioFormModalProps {
  aberto: boolean;
  usuarioParaEditar: UsuarioResumo | null;
  onFechar: () => void;
  onSalvo: () => void;
}

const EMAIL_REGEX = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
const CELULAR_REGEX = /^[0-9]{10,11}$/;
const UFS_BRASIL = new Set([
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA',
  'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI', 'RJ', 'RN',
  'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
]);

interface FormState {
  nome: string;
  email: string;
  senha: string;
  nivelAcesso: NivelAcesso;
  dataNascimento: string;
  numCelular: string;
  pais: string;
  estado: string;
  cidade: string;
  rua: string;
  cargo: string;
  crm: string;
  grauSuporte: GrauSuporte;
  descricao: string;
}

const ESTADO_INICIAL: FormState = {
  nome: '',
  email: '',
  senha: '',
  nivelAcesso: 'MEDICO',
  dataNascimento: '',
  numCelular: '',
  pais: 'Brasil',
  estado: '',
  cidade: '',
  rua: '',
  cargo: '',
  crm: '',
  grauSuporte: 'NIVEL_1',
  descricao: '',
};

export function UsuarioFormModal({
  aberto,
  usuarioParaEditar,
  onFechar,
  onSalvo,
}: UsuarioFormModalProps) {
  const isEdicao = Boolean(usuarioParaEditar);
  const [form, setForm] = useState<FormState>(ESTADO_INICIAL);
  const [erros, setErros] = useState<Record<string, string>>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [carregandoDados, setCarregandoDados] = useState(false);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (!aberto) {
      return;
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !salvando) {
        onFechar();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [aberto, salvando, onFechar]);

  useEffect(() => {
    if (!aberto) {
      return;
    }
    setErros({});
    setErroGeral(null);

    if (!usuarioParaEditar) {
      setForm(ESTADO_INICIAL);
      return;
    }

    let ativo = true;
    setCarregandoDados(true);
    usuarioService
      .consultarUsuarioPorId(usuarioParaEditar.id, true)
      .then((detalhe) => {
        if (!ativo) {
          return;
        }
        setForm({
          nome: detalhe.nome || '',
          email: detalhe.email || '',
          senha: '',
          nivelAcesso: detalhe.nivelAcesso,
          dataNascimento: detalhe.dataNascimento || '',
          numCelular: detalhe.numCelular ? detalhe.numCelular.replace(/\D/g, '') : '',
          pais: detalhe.pais || 'Brasil',
          estado: detalhe.estado || '',
          cidade: detalhe.cidade || '',
          rua: detalhe.rua || '',
          cargo: detalhe.funcionario?.cargo || '',
          crm: detalhe.funcionario?.crm || '',
          grauSuporte: detalhe.paciente?.grauSuporte || 'NIVEL_1',
          descricao: detalhe.paciente?.descricao || '',
        });
      })
      .catch((error) => {
        if (!ativo) {
          return;
        }
        setErroGeral(extrairErroApi(error).mensagem);
      })
      .finally(() => {
        if (ativo) {
          setCarregandoDados(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, [aberto, usuarioParaEditar]);

  if (!aberto) {
    return null;
  }

  const validarCampo = (campo: keyof FormState, valorAtual: FormState): string | undefined => {
    const val = String(valorAtual[campo] ?? '');
    switch (campo) {
      case 'nome': {
        const t = val.trim();
        if (!t) return 'Nome é obrigatório';
        if (t.length < 2 || t.length > 100) return 'Nome deve ter entre 2 e 100 caracteres';
        return undefined;
      }
      case 'email': {
        const t = val.trim().toLowerCase();
        if (!t) return 'E-mail é obrigatório';
        if (t.length < 5 || t.length > 254 || !EMAIL_REGEX.test(t)) return 'E-mail inválido';
        return undefined;
      }
      case 'senha': {
        if (!isEdicao && !val) return 'Senha é obrigatória';
        if (val) {
          if (!val.trim()) return 'Senha não pode ser formada apenas por espaços';
          if (val.length < 8) return 'Senha deve ter no mínimo 8 caracteres';
          if (new TextEncoder().encode(val).length > 72) {
            return 'Senha deve ter no máximo 72 bytes em UTF-8';
          }
        }
        return undefined;
      }
      case 'dataNascimento': {
        if (!val) return undefined;
        const hoje = new Date().toISOString().slice(0, 10);
        if (val < '1900-01-01' || val > hoje) {
          return 'Data de nascimento deve estar entre 1900-01-01 e hoje';
        }
        return undefined;
      }
      case 'numCelular': {
        const digits = val.trim();
        if (!digits) return undefined;
        if (!CELULAR_REGEX.test(digits)) {
          return 'Celular deve conter 10 ou 11 dígitos numéricos';
        }
        return undefined;
      }
      case 'pais': {
        const t = val.trim();
        if (!t) return undefined;
        if (t.length < 2 || t.length > 60) return 'País deve ter entre 2 e 60 caracteres';
        return undefined;
      }
      case 'estado': {
        const uf = val.trim().toUpperCase();
        if (!uf) return undefined;
        if (!UFS_BRASIL.has(uf)) return 'Informe uma sigla UF brasileira válida (ex.: SP)';
        return undefined;
      }
      case 'cidade': {
        const t = val.trim();
        if (!t) return undefined;
        if (t.length < 2 || t.length > 100) return 'Cidade deve ter entre 2 e 100 caracteres';
        return undefined;
      }
      case 'rua': {
        const t = val.trim();
        if (!t) return undefined;
        if (t.length < 2 || t.length > 150) return 'Rua deve ter entre 2 e 150 caracteres';
        return undefined;
      }
      case 'cargo': {
        if (valorAtual.nivelAcesso !== 'MEDICO') return undefined;
        const t = val.trim();
        if (!t) return 'Cargo é obrigatório para Médico / Funcionário';
        if (t.length < 2 || t.length > 80) return 'Cargo deve ter entre 2 e 80 caracteres';
        return undefined;
      }
      case 'crm': {
        if (valorAtual.nivelAcesso !== 'MEDICO') return undefined;
        const t = val.trim();
        if (!t) return undefined;
        if (t.length < 3 || t.length > 30) {
          return 'Registro profissional deve ter entre 3 e 30 caracteres';
        }
        return undefined;
      }
      case 'descricao': {
        if (valorAtual.nivelAcesso !== 'PACIENTE') return undefined;
        if (val.trim().length > 5000) {
          return 'Descrição clínica deve ter no máximo 5000 caracteres';
        }
        return undefined;
      }
      default:
        return undefined;
    }
  };

  const atualizarCampo = <K extends keyof FormState>(campo: K, valor: FormState[K]) => {
    const proximo = { ...form, [campo]: valor };
    setForm(proximo);
    if (erros[campo]) {
      const msg = validarCampo(campo, proximo);
      setErros((prev) => {
        const copy = { ...prev };
        if (msg) copy[campo] = msg;
        else delete copy[campo];
        return copy;
      });
    }
  };

  const handleBlur = (campo: keyof FormState) => {
    const msg = validarCampo(campo, form);
    setErros((prev) => {
      const copy = { ...prev };
      if (msg) copy[campo] = msg;
      else delete copy[campo];
      return copy;
    });
  };

  const validarFormulario = (): boolean => {
    const camposValidar: (keyof FormState)[] = [
      'nome',
      'email',
      'senha',
      'dataNascimento',
      'numCelular',
      'pais',
      'estado',
      'cidade',
      'rua',
      'cargo',
      'crm',
      'descricao',
    ];
    const novosErros: Record<string, string> = {};
    for (const campo of camposValidar) {
      const msg = validarCampo(campo, form);
      if (msg) {
        novosErros[campo] = msg;
      }
    }
    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const opcional = (valor: string): string | null => {
    const trimmed = valor.trim();
    return trimmed ? trimmed : null;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setErroGeral(null);
    if (!validarFormulario()) {
      return;
    }

    setSalvando(true);
    try {
      const funcionarioPayload =
        form.nivelAcesso === 'MEDICO'
          ? { cargo: form.cargo.trim(), crm: opcional(form.crm) }
          : null;
      const pacientePayload =
        form.nivelAcesso === 'PACIENTE'
          ? { grauSuporte: form.grauSuporte, descricao: opcional(form.descricao) }
          : null;

      if (isEdicao && usuarioParaEditar) {
        const payload: AtualizarUsuarioRequest = {
          nome: form.nome.trim(),
          email: form.email.trim(),
          senha: form.senha ? form.senha : null,
          dataNascimento: form.dataNascimento || null,
          numCelular: opcional(form.numCelular),
          pais: opcional(form.pais),
          estado: opcional(form.estado.toUpperCase()),
          cidade: opcional(form.cidade),
          rua: opcional(form.rua),
          funcionario: funcionarioPayload,
          paciente: pacientePayload,
        };
        await usuarioService.atualizarUsuario(usuarioParaEditar.id, payload);
      } else {
        const payload: CadastrarUsuarioRequest = {
          nome: form.nome.trim(),
          email: form.email.trim(),
          senha: form.senha,
          nivelAcesso: form.nivelAcesso,
          dataNascimento: form.dataNascimento || null,
          numCelular: opcional(form.numCelular),
          pais: opcional(form.pais) || 'Brasil',
          estado: opcional(form.estado.toUpperCase()),
          cidade: opcional(form.cidade),
          rua: opcional(form.rua),
          funcionario: funcionarioPayload,
          paciente: pacientePayload,
        };
        await usuarioService.cadastrarUsuario(payload);
      }
      onSalvo();
    } catch (error) {
      const { status, mensagem, errosCampo } = extrairErroApi(error);
      if (status === 409) {
        setErros((prev) => ({ ...prev, email: mensagem }));
        setErroGeral(mensagem);
      } else if (Object.keys(errosCampo).length > 0) {
        const mapeado: Record<string, string> = {};
        for (const [k, v] of Object.entries(errosCampo)) {
          const chaveLocal = k.includes('.') ? k.split('.')[1] : k;
          mapeado[chaveLocal] = v;
        }
        setErros((prev) => ({ ...prev, ...mapeado }));
        setErroGeral(mensagem);
      } else {
        setErroGeral(mensagem);
      }
    } finally {
      setSalvando(false);
    }
  };

  const possuiErros = Object.keys(erros).length > 0;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-md bg-neutral-text-primary/50"
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-usuario-titulo"
      onClick={(e) => {
        if (e.target === e.currentTarget && !salvando) {
          onFechar();
        }
      }}
    >
      <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto rounded-lg bg-neutral-surface border border-neutral-border shadow-lg">
        <div className="flex items-center justify-between px-lg py-md border-b border-neutral-border">
          <h2 id="modal-usuario-titulo" className="text-lg font-semibold text-neutral-text-primary">
            {isEdicao ? 'Editar Usuário' : 'Cadastrar Novo Usuário'}
          </h2>
          <button
            type="button"
            onClick={onFechar}
            disabled={salvando}
            className="rounded-md p-1 text-neutral-text-muted hover:text-neutral-text-primary"
            aria-label="Fechar modal"
          >
            <X className="h-5 w-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-lg">
          {erroGeral && (
            <div
              role="alert"
              className="mb-md flex items-start gap-2 rounded-md border border-status-danger bg-status-danger-bg px-3 py-2 text-sm text-status-danger"
            >
              <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" aria-hidden="true" />
              <span>{erroGeral}</span>
            </div>
          )}

          {carregandoDados ? (
            <div className="space-y-md py-md" aria-busy="true">
              <div className="h-10 rounded-md bg-neutral-border animate-pulse" />
              <div className="h-10 rounded-md bg-neutral-border animate-pulse" />
              <div className="h-10 rounded-md bg-neutral-border animate-pulse" />
            </div>
          ) : (
            <form onSubmit={handleSubmit} noValidate className="space-y-md">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
                <div>
                  <label htmlFor="usr-nome" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Nome completo <span className="text-status-danger">*</span>
                  </label>
                  <input
                    id="usr-nome"
                    type="text"
                    value={form.nome}
                    onChange={(e) => atualizarCampo('nome', e.target.value)}
                    onBlur={() => handleBlur('nome')}
                    aria-invalid={Boolean(erros.nome)}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.nome ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.nome && <p className="mt-1 text-xs text-status-danger">{erros.nome}</p>}
                </div>

                <div>
                  <label htmlFor="usr-email" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    E-mail <span className="text-status-danger">*</span>
                  </label>
                  <input
                    id="usr-email"
                    type="email"
                    value={form.email}
                    onChange={(e) => atualizarCampo('email', e.target.value)}
                    onBlur={() => handleBlur('email')}
                    aria-invalid={Boolean(erros.email)}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.email ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.email && <p className="mt-1 text-xs text-status-danger">{erros.email}</p>}
                </div>

                <div>
                  <label htmlFor="usr-senha" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Senha {!isEdicao && <span className="text-status-danger">*</span>}
                    {isEdicao && (
                      <span className="ml-1 text-xs font-normal text-neutral-text-secondary">
                        (opcional na edição)
                      </span>
                    )}
                  </label>
                  <input
                    id="usr-senha"
                    type="password"
                    value={form.senha}
                    onChange={(e) => atualizarCampo('senha', e.target.value)}
                    onBlur={() => handleBlur('senha')}
                    aria-invalid={Boolean(erros.senha)}
                    placeholder={isEdicao ? 'Deixe vazio para manter a atual' : 'Mínimo 8 caracteres'}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.senha ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.senha && <p className="mt-1 text-xs text-status-danger">{erros.senha}</p>}
                </div>

                <div>
                  <label htmlFor="usr-perfil" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Perfil de acesso <span className="text-status-danger">*</span>
                  </label>
                  <select
                    id="usr-perfil"
                    value={form.nivelAcesso}
                    disabled={isEdicao}
                    onChange={(e) => atualizarCampo('nivelAcesso', e.target.value as NivelAcesso)}
                    className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm disabled:bg-neutral-background disabled:text-neutral-text-secondary"
                  >
                    <option value="ADMINISTRADOR">Administrador</option>
                    <option value="MEDICO">Médico / Funcionário</option>
                    <option value="PACIENTE">Paciente</option>
                  </select>
                  {isEdicao && (
                    <p className="mt-1 text-xs text-neutral-text-secondary">
                      O perfil não pode ser alterado após o cadastro.
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="usr-nascimento" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Data de nascimento
                  </label>
                  <input
                    id="usr-nascimento"
                    type="date"
                    value={form.dataNascimento}
                    onChange={(e) => atualizarCampo('dataNascimento', e.target.value)}
                    onBlur={() => handleBlur('dataNascimento')}
                    aria-invalid={Boolean(erros.dataNascimento)}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.dataNascimento ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.dataNascimento && (
                    <p className="mt-1 text-xs text-status-danger">{erros.dataNascimento}</p>
                  )}
                </div>

                <div>
                  <label htmlFor="usr-celular" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Celular (DDD + número, somente dígitos)
                  </label>
                  <input
                    id="usr-celular"
                    type="tel"
                    inputMode="numeric"
                    maxLength={11}
                    placeholder="15999991234"
                    value={form.numCelular}
                    onChange={(e) => atualizarCampo('numCelular', e.target.value.replace(/\D/g, ''))}
                    onBlur={() => handleBlur('numCelular')}
                    aria-invalid={Boolean(erros.numCelular)}
                    className={`w-full rounded-md border px-3 py-2 text-sm font-mono ${
                      erros.numCelular ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.numCelular && <p className="mt-1 text-xs text-status-danger">{erros.numCelular}</p>}
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-4 gap-md">
                <div>
                  <label htmlFor="usr-pais" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    País
                  </label>
                  <input
                    id="usr-pais"
                    type="text"
                    value={form.pais}
                    onChange={(e) => atualizarCampo('pais', e.target.value)}
                    onBlur={() => handleBlur('pais')}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.pais ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.pais && <p className="mt-1 text-xs text-status-danger">{erros.pais}</p>}
                </div>

                <div>
                  <label htmlFor="usr-estado" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    UF
                  </label>
                  <input
                    id="usr-estado"
                    type="text"
                    maxLength={2}
                    placeholder="SP"
                    value={form.estado}
                    onChange={(e) => atualizarCampo('estado', e.target.value.toUpperCase())}
                    onBlur={() => handleBlur('estado')}
                    className={`w-full rounded-md border px-3 py-2 text-sm uppercase ${
                      erros.estado ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.estado && <p className="mt-1 text-xs text-status-danger">{erros.estado}</p>}
                </div>

                <div className="sm:col-span-2">
                  <label htmlFor="usr-cidade" className="block text-sm font-medium text-neutral-text-primary mb-1">
                    Cidade
                  </label>
                  <input
                    id="usr-cidade"
                    type="text"
                    value={form.cidade}
                    onChange={(e) => atualizarCampo('cidade', e.target.value)}
                    onBlur={() => handleBlur('cidade')}
                    className={`w-full rounded-md border px-3 py-2 text-sm ${
                      erros.cidade ? 'border-status-danger' : 'border-neutral-border'
                    }`}
                  />
                  {erros.cidade && <p className="mt-1 text-xs text-status-danger">{erros.cidade}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="usr-rua" className="block text-sm font-medium text-neutral-text-primary mb-1">
                  Endereço (Rua e número)
                </label>
                <input
                  id="usr-rua"
                  type="text"
                  value={form.rua}
                  onChange={(e) => atualizarCampo('rua', e.target.value)}
                  onBlur={() => handleBlur('rua')}
                  className={`w-full rounded-md border px-3 py-2 text-sm ${
                    erros.rua ? 'border-status-danger' : 'border-neutral-border'
                  }`}
                />
                {erros.rua && <p className="mt-1 text-xs text-status-danger">{erros.rua}</p>}
              </div>

              {form.nivelAcesso === 'MEDICO' && (
                <div className="rounded-md border border-neutral-border bg-neutral-background p-md space-y-md">
                  <h3 className="text-sm font-semibold text-neutral-text-primary">
                    Especialização: Médico / Funcionário
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-md">
                    <div>
                      <label htmlFor="usr-cargo" className="block text-sm font-medium text-neutral-text-primary mb-1">
                        Cargo / Especialidade <span className="text-status-danger">*</span>
                      </label>
                      <input
                        id="usr-cargo"
                        type="text"
                        placeholder="Ex.: Psicologia"
                        value={form.cargo}
                        onChange={(e) => atualizarCampo('cargo', e.target.value)}
                        onBlur={() => handleBlur('cargo')}
                        aria-invalid={Boolean(erros.cargo)}
                        className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm ${
                          erros.cargo ? 'border-status-danger' : 'border-neutral-border'
                        }`}
                      />
                      {erros.cargo && <p className="mt-1 text-xs text-status-danger">{erros.cargo}</p>}
                    </div>

                    <div>
                      <label htmlFor="usr-crm" className="block text-sm font-medium text-neutral-text-primary mb-1">
                        CRM / Registro no Conselho
                      </label>
                      <input
                        id="usr-crm"
                        type="text"
                        placeholder="Ex.: CRP/SP 06/12345"
                        value={form.crm}
                        onChange={(e) => atualizarCampo('crm', e.target.value)}
                        onBlur={() => handleBlur('crm')}
                        aria-invalid={Boolean(erros.crm)}
                        className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm ${
                          erros.crm ? 'border-status-danger' : 'border-neutral-border'
                        }`}
                      />
                      {erros.crm && <p className="mt-1 text-xs text-status-danger">{erros.crm}</p>}
                    </div>
                  </div>
                </div>
              )}

              {form.nivelAcesso === 'PACIENTE' && (
                <div className="rounded-md border border-neutral-border bg-neutral-background p-md space-y-md">
                  <h3 className="text-sm font-semibold text-neutral-text-primary">
                    Especialização: Paciente TEA
                  </h3>
                  <div>
                    <label
                      htmlFor="usr-grau-suporte"
                      className="block text-sm font-medium text-neutral-text-primary mb-1"
                    >
                      Grau de suporte <span className="text-status-danger">*</span>
                    </label>
                    <select
                      id="usr-grau-suporte"
                      value={form.grauSuporte}
                      onChange={(e) => atualizarCampo('grauSuporte', e.target.value as GrauSuporte)}
                      className="w-full rounded-md border border-neutral-border bg-neutral-surface px-3 py-2 text-sm"
                    >
                      <option value="NIVEL_1">Nível 1 (Suporte leve)</option>
                      <option value="NIVEL_2">Nível 2 (Suporte substancial)</option>
                      <option value="NIVEL_3">Nível 3 (Suporte muito substancial)</option>
                    </select>
                  </div>

                  <div>
                    <label
                      htmlFor="usr-descricao"
                      className="block text-sm font-medium text-neutral-text-primary mb-1"
                    >
                      Descrição clínica inicial
                    </label>
                    <textarea
                      id="usr-descricao"
                      rows={3}
                      value={form.descricao}
                      onChange={(e) => atualizarCampo('descricao', e.target.value)}
                      onBlur={() => handleBlur('descricao')}
                      className={`w-full rounded-md border bg-neutral-surface px-3 py-2 text-sm ${
                        erros.descricao ? 'border-status-danger' : 'border-neutral-border'
                      }`}
                    />
                    {erros.descricao && <p className="mt-1 text-xs text-status-danger">{erros.descricao}</p>}
                  </div>
                </div>
              )}

              <div className="flex justify-end gap-sm pt-md border-t border-neutral-border">
                <button
                  type="button"
                  onClick={onFechar}
                  disabled={salvando}
                  className="rounded-md border border-neutral-border bg-neutral-surface px-4 py-2 text-sm font-medium text-neutral-text-primary hover:bg-neutral-background"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={salvando || possuiErros}
                  className="rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-neutral-surface hover:bg-brand-primary-hover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  {salvando ? 'Salvando...' : isEdicao ? 'Salvar alterações' : 'Cadastrar'}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
