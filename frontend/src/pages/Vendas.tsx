import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, ApiError } from '../api/client';
import type { Pagina, Venda } from '../api/types';
import { dataBr, hojeIso, lerDecimal, moeda } from '../lib/formato';
import { useOpcoes } from '../lib/fontes';
import { FormularioRecurso } from '../resources/CrudPagina';
import { clientes as recursoClientes, colheitas as recursoColheitas } from '../resources/definicoes';
import { CampoForm, Carregando, Folha, MensagemErro, Vazio } from '../ui/componentes';

interface ItemForm { colheitaId: string; quantidade: string; precoUnitario: string }
const itemVazio = (): ItemForm => ({ colheitaId: '', quantidade: '', precoUnitario: '' });
const txt = (n: number) => String(n).replace('.', ',');

export function Vendas() {
  const [params, setParams] = useSearchParams();
  const [pagina, setPagina] = useState(0);
  const [aberto, setAberto] = useState<{ venda?: Venda } | null>(params.get('novo') === '1' ? {} : null);

  const q = useQuery({
    queryKey: ['vendas', pagina],
    queryFn: () => api.get<Pagina<Venda>>(`/api/vendas?pagina=${pagina}&tamanho=20`),
  });

  function fechar() {
    setAberto(null);
    if (params.has('novo')) { params.delete('novo'); setParams(params, { replace: true }); }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Vendas</h1>
        <button className="btn" type="button" onClick={() => setAberto({})}>+ Nova</button>
      </div>
      {q.isLoading && <Carregando />}
      <MensagemErro erro={q.error} />
      {q.data && q.data.itens.length === 0 && <Vazio>Nenhuma venda registrada ainda.</Vazio>}
      <div className="lista">
        {q.data?.itens.map((v) => (
          <button key={v.id} type="button" className="item" onClick={() => setAberto({ venda: v })}>
            <div className="corpo">
              <div className="titulo">{v.clienteNome}</div>
              <div className="sub">{dataBr(v.dataCompra)}</div>
              <div className="detalhes">
                {v.itens.map((i) => <span key={i.id}>{i.produtoNome} {txt(i.quantidade)} {i.unidadeSigla}</span>)}
              </div>
            </div>
            <div className="destaque">{moeda(v.total)}</div>
          </button>
        ))}
      </div>
      {q.data && q.data.totalPaginas > 1 && (
        <div className="paginacao">
          <button className="btn secundario pequeno" type="button" disabled={pagina === 0} onClick={() => setPagina(pagina - 1)}>← Anteriores</button>
          <span>Página {pagina + 1} de {q.data.totalPaginas}</span>
          <button className="btn secundario pequeno" type="button" disabled={pagina + 1 >= q.data.totalPaginas} onClick={() => setPagina(pagina + 1)}>Próximas →</button>
        </div>
      )}
      {aberto && <FormularioVenda venda={aberto.venda} aoFechar={fechar} />}
    </>
  );
}

function FormularioVenda({ venda, aoFechar }: { venda?: Venda; aoFechar: () => void }) {
  const qc = useQueryClient();
  const clientes = useOpcoes('clientes');
  const colheitas = useOpcoes('colheitas');
  const editando = venda !== undefined;

  const [clienteId, setClienteId] = useState(venda ? String(venda.clienteId) : '');
  const [data, setData] = useState(venda?.dataCompra ?? hojeIso());
  const [itens, setItens] = useState<ItemForm[]>(
    venda ? venda.itens.map((i) => ({ colheitaId: String(i.colheitaId), quantidade: txt(i.quantidade), precoUnitario: txt(i.precoUnitario) })) : [itemVazio()],
  );
  const [confirmando, setConfirmando] = useState(false);
  const [erroLocal, setErroLocal] = useState('');
  const [criandoCliente, setCriandoCliente] = useState(false);
  const [criandoColheitaEm, setCriandoColheitaEm] = useState<number | null>(null);

  const total = itens.reduce((s, i) => s + (lerDecimal(i.quantidade) ?? 0) * (lerDecimal(i.precoUnitario) ?? 0), 0);

  const salvar = useMutation({
    mutationFn: () => {
      const corpo = {
        clienteId: Number(clienteId),
        dataCompra: data,
        itens: itens.map((i) => ({ colheitaId: Number(i.colheitaId), quantidade: lerDecimal(i.quantidade), precoUnitario: lerDecimal(i.precoUnitario) })),
      };
      return editando ? api.put(`/api/vendas/${venda.id}`, corpo) : api.post('/api/vendas', corpo);
    },
    onSuccess: async () => { await qc.invalidateQueries(); aoFechar(); },
  });
  const excluir = useMutation({
    mutationFn: () => api.del(`/api/vendas/${venda?.id}`),
    onSuccess: async () => { await qc.invalidateQueries(); aoFechar(); },
  });

  const mudaItem = (k: number, campo: keyof ItemForm, valor: string) =>
    setItens((l) => l.map((it, n) => (n === k ? { ...it, [campo]: valor } : it)));

  function enviar(e: FormEvent) {
    e.preventDefault();
    setErroLocal('');
    if (!clienteId) return setErroLocal('Escolha o cliente.');
    for (const i of itens) {
      if (!i.colheitaId || !lerDecimal(i.quantidade) || !lerDecimal(i.precoUnitario)) {
        return setErroLocal('Preencha colheita, quantidade e preço de todos os itens.');
      }
    }
    salvar.mutate();
  }

  const erroServidor = salvar.error instanceof ApiError ? salvar.error.campos : {};

  return (
    <Folha titulo={editando ? 'Editar venda' : 'Nova venda'} aoFechar={aoFechar}>
      <form onSubmit={enviar} noValidate>
        <CampoForm id="cliente" rotulo="Cliente" erro={erroServidor.clienteId}
          dica={clientes.opcoes.length === 0 && !clientes.carregando ? 'Cadastre um cliente em Mais > Clientes.' : undefined}>
          <div className="selecao-com-criar">
            <select id="cliente" value={clienteId} disabled={editando} onChange={(e) => setClienteId(e.target.value)}>
              <option value="">Selecione…</option>
              {clientes.opcoes.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
            </select>
            {!editando && (
              <button type="button" className="btn secundario pequeno" onClick={() => setCriandoCliente(true)}>+ Novo(a)</button>
            )}
          </div>
        </CampoForm>
        {criandoCliente && (
          <FormularioRecurso
            recurso={recursoClientes}
            aninhado
            aoFechar={() => setCriandoCliente(false)}
            aoCriar={(novoId) => { setClienteId(String(novoId)); setCriandoCliente(false); }}
          />
        )}
        <CampoForm id="data" rotulo="Data da venda" erro={erroServidor.dataCompra}>
          <input id="data" type="date" value={data} onChange={(e) => setData(e.target.value)} />
        </CampoForm>

        <h3 style={{ margin: '4px 0 10px' }}>Itens</h3>
        {itens.map((it, k) => (
          <div className="linha-item-venda" key={k}>
            <CampoForm id={`col-${k}`} rotulo="Colheita">
              <div className="selecao-com-criar">
                <select id={`col-${k}`} value={it.colheitaId} onChange={(e) => mudaItem(k, 'colheitaId', e.target.value)}>
                  <option value="">Selecione…</option>
                  {colheitas.opcoes.map((o) => <option key={o.valor} value={o.valor}>{o.rotulo}</option>)}
                </select>
                <button type="button" className="btn secundario pequeno" onClick={() => setCriandoColheitaEm(k)}>+ Novo(a)</button>
              </div>
            </CampoForm>
            {criandoColheitaEm === k && (
              <FormularioRecurso
                recurso={recursoColheitas}
                aninhado
                aoFechar={() => setCriandoColheitaEm(null)}
                aoCriar={(novoId) => { mudaItem(k, 'colheitaId', String(novoId)); setCriandoColheitaEm(null); }}
              />
            )}
            <CampoForm id={`qtd-${k}`} rotulo="Quantidade">
              <input id={`qtd-${k}`} inputMode="decimal" value={it.quantidade} onChange={(e) => mudaItem(k, 'quantidade', e.target.value)} />
            </CampoForm>
            <CampoForm id={`preco-${k}`} rotulo="Preço por unidade (R$)">
              <input id={`preco-${k}`} inputMode="decimal" value={it.precoUnitario} onChange={(e) => mudaItem(k, 'precoUnitario', e.target.value)} />
            </CampoForm>
            {itens.length > 1 && (
              <button type="button" className="btn perigo pequeno" onClick={() => { setItens((l) => l.filter((_, n) => n !== k)); setCriandoColheitaEm(null); }}>Remover item</button>
            )}
          </div>
        ))}
        <button type="button" className="btn secundario pequeno" onClick={() => setItens((l) => [...l, itemVazio()])}>+ Adicionar item</button>

        <p style={{ margin: '16px 0', fontWeight: 700 }}>Total: {moeda(total)}</p>
        {erroLocal && <div className="aviso erro" role="alert">{erroLocal}</div>}
        <MensagemErro erro={salvar.error ?? excluir.error} />
        <div className="acoes">
          <button className="btn cheio" type="submit" disabled={salvar.isPending}>{salvar.isPending ? 'Salvando…' : 'Salvar venda'}</button>
          {editando && !confirmando && <button className="btn perigo cheio" type="button" onClick={() => setConfirmando(true)}>Excluir venda</button>}
          {editando && confirmando && (
            <button className="btn perigo cheio" type="button" disabled={excluir.isPending} onClick={() => excluir.mutate()}>
              Toque de novo para confirmar a exclusão
            </button>
          )}
        </div>
      </form>
    </Folha>
  );
}
