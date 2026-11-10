/**
 * Guarda do token de acesso.
 *
 * O token vive no localStorage para o app continuar logado ao ser reaberto (o uso
 * normal de um PWA no celular). O custo e que um XSS poderia le-lo; por isso o token
 * e de curta duracao (8h por padrao), o app nao injeta HTML vindo da API e a CSP deve
 * ser configurada na hospedagem. Todo acesso e protegido por try/catch: modo privado
 * ou armazenamento bloqueado nao pode derrubar o app.
 */
const CHAVE = 'rm.sessao';

export interface Sessao {
  token: string;
  expiraEm: number; // epoch ms
  usuario: Usuario;
}

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: 'ADMIN' | 'PRODUTOR' | 'OPERADOR';
  produtorId: number | null;
  produtorNome: string | null;
}

export function lerSessao(): Sessao | null {
  try {
    const bruto = localStorage.getItem(CHAVE);
    if (!bruto) return null;
    const s = JSON.parse(bruto) as Sessao;
    if (!s.token || s.expiraEm <= Date.now()) {
      limparSessao();
      return null;
    }
    return s;
  } catch {
    return null;
  }
}

export function gravarSessao(s: Sessao): void {
  try {
    localStorage.setItem(CHAVE, JSON.stringify(s));
  } catch {
    /* sem armazenamento: o app segue funcionando, so nao lembra o login */
  }
}

export function limparSessao(): void {
  try {
    localStorage.removeItem(CHAVE);
  } catch {
    /* ignorado */
  }
}
