import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import type { Pagina } from '../api/types';
import { CampoForm, Carregando, Folha, MensagemErro, Vazio } from '../ui/componentes';
import { useOpcoes } from '../lib/fontes';
import type { Campo, Recurso, Valores } from './tipos';

interface Props<T extends { id: number }> {
  recurso: Recurso<T>;
  /** Se falso, esconde editar/excluir (ex.: perfil sem permissao). */
  podeEditar?: boolean;
  /** Se falso, esconde o botao de criar. Por padrao, igual a podeEditar. */
  podeCriar?: boolean;
}

export function CrudPagina<T extends { id: number }>({ recurso, podeEditar = true, podeCriar }: Props<T>) {
  const criar = podeCriar ?? podeEditar;
  const [params, setParams] = useSearchParams();
  const [pagina, setPagina] = useState(0);
  const [filtro, setFiltro] = useState('');
  const [aberto, setAberto] = useState<{ registro?: T } | null>(
    params.get('novo') === '1' && criar ? {} : null,
  );

  const filtroOpcoes = useOpcoes(recurso.filtro?.fonte ?? 'safras');

  const consulta = useQuery({
    queryKey: [recurso.chave, pagina, filtro],
    queryFn: async () => {
      const q = new URLSearchParams();
      if (recurso.paginado) {
        q.set('pagina', String(pagina));
        q.set('tamanho', '20');
      }
      if (recurso.filtro && filtro) q.set(recurso.filtro.parametro, filtro);
      const texto = q.toString();
      const r = await api.get<T[] | Pagina<T>>(`${recurso.caminho}${texto ? `?${texto}` : ''}`);
      return Array.isArray(r) ? { itens: r, totalPaginas: 1, total: r.length } : r;
    },
  });

  function fechar() {
    setAberto(null);
    if (params.has('novo')) {
      params.delete('novo');
      setParams(params, { replace: true });
    }
  }

  const itens = consulta.data?.itens ?? [];

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>{recurso.titulo}</h1>
        {criar && (
          <button className="btn" type="button" onClick={() => setAberto({})}>
            + Novo
          </button>
        )}
      </div>

      {recurso.filtro && (
        <div className="campo">
          <label htmlFor="filtro">{recurso.filtro.rotulo}</label>
          <select id="filtro" value={filtro} onChange={(e) => { setFiltro(e.target.value); setPagina(0); }}>
            <option value="">Todas</option>
            {filtroOpcoes.opcoes.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
          </select>
        </div>
      )}

      {consulta.isLoading && <Carregando />}
      <MensagemErro erro={consulta.error} />
      {!consulta.isLoading && !consulta.error && itens.length === 0 && <Vazio>{recurso.textoVazio}</Vazio>}

      <div className="lista">
        {itens.map((r) => {
          const l = recurso.linha(r);
          return (
            <button key={r.id} type="button" className="item" onClick={() => podeEditar && setAberto({ registro: r })}>
              <div className="corpo">
                <div className="titulo">{l.titulo}</div>
                {l.subtitulo && <div className="sub">{l.subtitulo}</div>}
                {l.detalhes && l.detalhes.length > 0 && (
                  <div className="detalhes">{l.detalhes.map((d) => <span key={d}>{d}</span>)}</div>
                )}
              </div>
              {l.destaque && <div className="destaque">{l.destaque}</div>}
            </button>
          );
        })}
      </div>

      {recurso.paginado && (consulta.data?.totalPaginas ?? 1) > 1 && (
        <div className="paginacao">
          <button className="btn secundario pequeno" type="button" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>
            ← Anteriores
          </button>
          <span>Página {pagina + 1} de {consulta.data?.totalPaginas}</span>
          <button className="btn secundario pequeno" type="button"
            disabled={pagina + 1 >= (consulta.data?.totalPaginas ?? 1)} onClick={() => setPagina(pagina + 1)}>
            Próximas →
          </button>
        </div>
      )}

      {aberto && <FormularioRecurso recurso={recurso} registro={aberto.registro} aoFechar={fechar} />}
    </>
  );
}

// ----------------------------------------------------------------------------

interface FormProps<T extends { id: number }> {
  recurso: Recurso<T>;
  registro?: T;
  aoFechar: () => void;
  /** Preenche campos antes de abrir (ex.: a safra ja escolhida no formulario pai). */
  valoresIniciais?: Valores;
  /** So quando criado (nao editando): devolve o id do item novo, para auto-selecionar no formulario pai. */
  aoCriar?: (id: number) => void;
  /** Quando aberto de dentro de outro formulario (criacao rapida): nao usa <form>, HTML nao permite form aninhado. */
  aninhado?: boolean;
}

export function FormularioRecurso<T extends { id: number }>({ recurso, registro, aoFechar, valoresIniciais, aoCriar, aninhado }: FormProps<T>) {
  const qc = useQueryClient();
  const editando = registro !== undefined;
  const [valores, setValores] = useState<Valores>(() => ({ ...recurso.paraFormulario(registro), ...valoresIniciais }));
  const [errosLocais, setErrosLocais] = useState<Record<string, string>>({});
  const [confirmando, setConfirmando] = useState(false);

  const salvar = useMutation({
    mutationFn: () =>
      editando
        ? api.put(`${recurso.caminho}/${registro.id}`, recurso.paraPayload(valores))
        : api.post<{ id: number }>(recurso.caminho, recurso.paraPayload(valores)),
    onSuccess: async (resultado) => {
      await qc.invalidateQueries(); // estoque e relatorios dependem de quase tudo
      if (!editando && aoCriar) aoCriar((resultado as { id: number }).id);
      aoFechar();
    },
  });

  const excluir = useMutation({
    mutationFn: () => api.del(`${recurso.caminho}/${registro?.id}`),
    onSuccess: async () => {
      await qc.invalidateQueries();
      aoFechar();
    },
  });

  function mudar(nome: string, valor: string) {
    const dependentes = recurso.campos.find((c) => c.nome === nome)?.limpa ?? [];
    setValores((v) => {
      const novo = { ...v, [nome]: valor };
      for (const d of dependentes) novo[d] = '';
      return novo;
    });
    setErrosLocais((e) => ({ ...e, [nome]: '' }));
  }

  function enviar(e?: FormEvent) {
    e?.preventDefault();
    const faltando: Record<string, string> = {};
    for (const c of recurso.campos) {
      if (c.obrigatorio && !(valores[c.nome] ?? '').trim()) faltando[c.nome] = 'Campo obrigatório';
    }
    setErrosLocais(faltando);
    if (Object.keys(faltando).length === 0) salvar.mutate();
  }

  const errosServidor = salvar.error instanceof ApiError ? salvar.error.campos : {};
  const erro = salvar.error ?? excluir.error;

  const corpo = (
    <>
      {recurso.campos.map((c) => (
        <CampoRecurso
          key={c.nome}
          campo={c}
          valores={valores}
          editando={editando}
          erro={errosLocais[c.nome] || errosServidor[c.nome]}
          aoMudar={mudar}
        />
      ))}
      <MensagemErro erro={erro} />
      <div className="acoes">
        <button
          className="btn cheio"
          type={aninhado ? 'button' : 'submit'}
          disabled={salvar.isPending}
          onClick={aninhado ? () => enviar() : undefined}
        >
          {salvar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
        {editando && !confirmando && (
          <button className="btn perigo cheio" type="button" onClick={() => setConfirmando(true)}>
            Excluir
          </button>
        )}
        {editando && confirmando && (
          <button className="btn perigo cheio" type="button" disabled={excluir.isPending} onClick={() => excluir.mutate()}>
            {excluir.isPending ? 'Excluindo…' : 'Toque de novo para confirmar a exclusão'}
          </button>
        )}
      </div>
    </>
  );

  return (
    <Folha titulo={editando ? `Editar ${recurso.singular}` : `Novo(a) ${recurso.singular}`} aoFechar={aoFechar}>
      {aninhado ? <div>{corpo}</div> : <form onSubmit={enviar} noValidate>{corpo}</form>}
    </Folha>
  );
}

interface CampoProps {
  campo: Campo;
  valores: Valores;
  editando: boolean;
  erro?: string;
  aoMudar: (nome: string, valor: string) => void;
}

function CampoRecurso({ campo, valores, editando, erro, aoMudar }: CampoProps) {
  const id = `campo-${campo.nome}`;
  const valor = valores[campo.nome] ?? '';
  const travado = editando && campo.travadoAoEditar;
  const rotulo = campo.obrigatorio ? campo.rotulo : `${campo.rotulo} (opcional)`;

  return (
    <CampoForm id={id} rotulo={rotulo} erro={erro} dica={campo.dica}>
      {campo.tipo === 'selecao' && campo.fonte ? (
        <Selecao id={id} campo={campo} valor={valor} valores={valores} travado={travado} aoMudar={aoMudar} />
      ) : campo.tipo === 'textarea' ? (
        <textarea id={id} rows={3} value={valor} maxLength={campo.maxLength} onChange={(e) => aoMudar(campo.nome, e.target.value)} />
      ) : (
        <input
          id={id}
          type={campo.tipo === 'data' ? 'date' : 'text'}
          inputMode={campo.tipo === 'numero' || campo.tipo === 'dinheiro' ? 'decimal' : campo.tipo === 'documento' ? 'numeric' : undefined}
          value={valor}
          maxLength={campo.maxLength}
          disabled={travado}
          onChange={(e) => aoMudar(campo.nome, e.target.value)}
          autoComplete="off"
        />
      )}
    </CampoForm>
  );
}

function Selecao({ id, campo, valor, valores, travado, aoMudar }: {
  id: string; campo: Campo; valor: string; valores: Valores; travado?: boolean;
  aoMudar: (nome: string, valor: string) => void;
}) {
  const { opcoes, carregando } = useOpcoes(campo.fonte!);
  const visiveis = campo.filtrar ? opcoes.filter((o) => campo.filtrar!(o, valores)) : opcoes;
  const [criando, setCriando] = useState(false);
  const criarRecurso = campo.criarRecurso;

  const select = (
    <select id={id} value={valor} disabled={travado || carregando} onChange={(e) => aoMudar(campo.nome, e.target.value)}>
      <option value="">{carregando ? 'Carregando…' : campo.obrigatorio ? 'Selecione…' : '— nenhum —'}</option>
      {visiveis.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
    </select>
  );

  if (!criarRecurso) return select;

  // Pre-preenche, no formulario novo, os campos que ja existem (pelo nome) no formulario pai.
  const preenchimento: Valores = {};
  for (const c of criarRecurso.campos) {
    if (valores[c.nome]) preenchimento[c.nome] = valores[c.nome];
  }

  return (
    <>
      <div className="selecao-com-criar">
        {select}
        <button type="button" className="btn secundario pequeno" onClick={() => setCriando(true)}>
          + Novo(a)
        </button>
      </div>
      {criando && (
        <FormularioRecurso
          recurso={criarRecurso}
          valoresIniciais={preenchimento}
          aninhado
          aoFechar={() => setCriando(false)}
          aoCriar={(novoId) => { aoMudar(campo.nome, String(novoId)); setCriando(false); }}
        />
      )}
    </>
  );
}
