package service;

import DAO.*;
import model.*;
import util.ValidacaoUtil;
import database.LoggerUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class CadastroService {
    private ProdutorDAO produtorDAO;
    private SafraDAO safraDAO;
    private CulturaDAO culturaDAO;
    private GastoDAO gastoDAO;
    private ColheitaDAO colheitaDAO;
    private Scanner scanner;
    private Map<String, Produtor> cacheProdutores;

    public CadastroService() {
        this.produtorDAO = new ProdutorDAO();
        this.safraDAO = new SafraDAO();
        this.culturaDAO = new CulturaDAO();
        this.gastoDAO = new GastoDAO();
        this.colheitaDAO = new ColheitaDAO();
        this.scanner = new Scanner(System.in);
        this.cacheProdutores = new HashMap<>();
        carregarProdutores();
    }

    private void carregarProdutores() {
        try {
            List<Produtor> produtores = produtorDAO.listarTodos();
            for (Produtor produtor : produtores) {
                Produtor completo = produtorDAO.buscarCompletoPorId(produtor.getId());
                cacheProdutores.put(produtor.getCpf(), completo);
            }
            LoggerUtil.logInfo("Carregados " + produtores.size() + " produtores do banco");
        } catch (SQLException e) {
            LoggerUtil.logErro("Erro ao carregar produtores: " + e.getMessage());
        }
    }

    public Produtor cadastrarSeNaoExistir(Produtor produtor) {
        try {
            // Verifica cache primeiro (mais rápido)
            if (cacheProdutores.containsKey(produtor.getCpf())) {
                Produtor existente = cacheProdutores.get(produtor.getCpf());
                System.out.println("✅ Produtor já cadastrado: " + existente.getNome());
                return existente;
            }

            // Verifica no banco
            if (produtorDAO.cpfExiste(produtor.getCpf())) {
                Produtor existente = buscarProdutorPorCPF(produtor.getCpf());
                if (existente != null) {
                    System.out.println("✅ Produtor encontrado: " + existente.getNome());
                    return existente;
                }
            }

            // Se chegou aqui, CPF não existe, então cadastra
            return cadastrarProdutorCompletoERetornar(produtor);

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
            return null;
        }
    }

    public Produtor cadastrarOuRecuperarProdutor(Produtor produtor) {
        try {
            // Primeiro verifica se o CPF já existe
            if (produtorDAO.cpfExiste(produtor.getCpf())) {
                System.out.println("ℹ️  CPF já cadastrado. Recuperando produtor existente...");

                // Busca o produtor existente
                Produtor produtorExistente = buscarProdutorPorCPF(produtor.getCpf());

                if (produtorExistente != null) {
                    System.out.println("✅ Produtor encontrado: " + produtorExistente.getNome());

                    // Verifica se os dados são diferentes e sugere atualização
                    if (!produtorExistente.getNome().equalsIgnoreCase(produtor.getNome())) {
                        System.out.println("⚠️  Nome diferente do cadastrado:");
                        System.out.println("   Cadastrado: " + produtorExistente.getNome());
                        System.out.println("   Informado: " + produtor.getNome());
                    }

                    if (!produtorExistente.getTelefone().equals(produtor.getTelefone())) {
                        System.out.println("⚠️  Telefone diferente do cadastrado:");
                        System.out.println("   Cadastrado: " + produtorExistente.getTelefone());
                        System.out.println("   Informado: " + produtor.getTelefone());
                    }

                    return produtorExistente;
                }
            }

            return cadastrarProdutorCompletoERetornar(produtor);

        } catch (Exception e) {
            System.out.println("❌ Erro ao verificar/cadastrar produtor: " + e.getMessage());
            return null;
        }
    }

    public Produtor upsertProdutor(Produtor produtor) {
        try {
            Produtor existente = buscarProdutorPorCPF(produtor.getCpf());

            if (existente != null) {
                System.out.println("ℹ️  CPF já cadastrado. Atualizando dados do produtor...");

                // Verifica o que mudou
                boolean atualizou = false;

                if (!existente.getNome().equalsIgnoreCase(produtor.getNome())) {
                    existente.setNome(produtor.getNome());
                    System.out.println("   ↪ Nome atualizado: " + produtor.getNome());
                    atualizou = true;
                }

                if (!existente.getTelefone().equals(produtor.getTelefone())) {
                    existente.setTelefone(produtor.getTelefone());
                    System.out.println("   ↪ Telefone atualizado: " + produtor.getTelefone());
                    atualizou = true;
                }

                if (atualizou) {
                    // Atualiza no banco
                    produtorDAO.atualizar(existente);
                    cacheProdutores.put(existente.getCpf(), existente);
                    System.out.println("✅ Dados do produtor atualizados!");
                    LoggerUtil.logOperacao("ATUALIZAR PRODUTOR",
                            "CPF: " + existente.getCpf() + ", Nome: " + existente.getNome());
                } else {
                    System.out.println("✅ Produtor encontrado (dados idênticos)");
                }

                return existente;
            } else {
                // Cadastra novo
                return cadastrarProdutorCompletoERetornar(produtor);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro no processamento do produtor: " + e.getMessage());
            return null;
        }
    }

    private Produtor cadastrarProdutorCompletoERetornar(Produtor produtor) {
        try {
            int idProdutor = produtorDAO.cadastrar(produtor);

            if (idProdutor > 0) {
                produtor.setId(idProdutor);
                cacheProdutores.put(produtor.getCpf(), produtor);
                System.out.println("✅ Produtor cadastrado com sucesso! ID: " + idProdutor);
                LoggerUtil.logOperacao("CADASTRO PRODUTOR",
                        "ID: " + idProdutor + ", Nome: " + produtor.getNome() + ", CPF: " + produtor.getCpf());
                return produtor;
            }

            return null;

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
            LoggerUtil.logErro("Erro cadastro produtor: " + e.getMessage());
            return null;
        }
    }

    public void cadastrarProdutorCompleto(Produtor produtor) {
        try {
            if (produtorDAO.cpfExiste(produtor.getCpf())) {
                System.out.println("❌ CPF já cadastrado no sistema!");
                LoggerUtil.logOperacao("CADASTRO PRODUTOR", "Tentativa com CPF duplicado: " + produtor.getCpf());
                return;
            }

            int idProdutor = produtorDAO.cadastrar(produtor);

            if (idProdutor > 0) {
                produtor.setId(idProdutor);
                cacheProdutores.put(produtor.getCpf(), produtor);
                System.out.println("✅ Produtor cadastrado com sucesso! ID: " + idProdutor);
                LoggerUtil.logOperacao("CADASTRO PRODUTOR",
                        "ID: " + idProdutor + ", Nome: " + produtor.getNome() + ", CPF: " + produtor.getCpf());
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
            LoggerUtil.logErro("Erro cadastro produtor: " + e.getMessage());
        }
    }

    public List<Produtor> getProdutores() {
        return new ArrayList<>(cacheProdutores.values());
    }

    public Produtor buscarProdutorPorCPF(String cpf) {
        try {
            // Verifica cache primeiro
            if (cacheProdutores.containsKey(cpf)) {
                return cacheProdutores.get(cpf);
            }

            // Busca no banco
            Produtor produtor = produtorDAO.buscarPorCPF(cpf);
            if (produtor != null) {
                // Carrega dados completos
                Produtor completo = produtorDAO.buscarCompletoPorId(produtor.getId());
                cacheProdutores.put(cpf, completo);
                return completo;
            }

            return null;

        } catch (SQLException e) {
            System.out.println("❌ Erro na busca: " + e.getMessage());
            return null;
        }
    }

    public void cadastrarSafraParaProdutor(String cpf, int ano) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            ValidacaoUtil.validarAno(ano);

            if (safraDAO.safraExiste(produtor.getId(), ano)) {
                System.out.println("❌ Este produtor já possui safra cadastrada para o ano " + ano);
                return;
            }

            Safra safra = new Safra(ano);
            int idSafra = safraDAO.cadastrar(produtor.getId(), safra);

            if (idSafra > 0) {
                safra.setId(idSafra);
                safra.setProdutorId(produtor.getId());
                produtor.adicionarSafra(safra);
                System.out.println("✅ Safra cadastrada com sucesso! ID: " + idSafra);
                LoggerUtil.logOperacao("CADASTRAR SAFRA",
                        "Produtor: " + produtor.getNome() + ", Ano: " + ano);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void cadastrarCulturaParaSafra(String cpf, int ano, String nome, String variedade, double area) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            ValidacaoUtil.validarStringNaoVazia(nome, "Nome da cultura");
            ValidacaoUtil.validarStringNaoVazia(variedade, "Variedade");
            ValidacaoUtil.validarAreaPlantada(area);

            if (culturaDAO.culturaExiste(safra.getId(), nome)) {
                System.out.println("❌ Já existe uma cultura com este nome nesta safra!");
                return;
            }

            Cultura cultura = new Cultura(nome, variedade, area);
            int idCultura = culturaDAO.cadastrar(safra.getId(), cultura);

            if (idCultura > 0) {
                cultura.setId(idCultura);
                cultura.setSafraId(safra.getId());
                safra.adicionarCultura(cultura);
                System.out.println("✅ Cultura cadastrada com sucesso! ID: " + idCultura);
                LoggerUtil.logOperacao("CADASTRAR CULTURA",
                        "Safra: " + ano + ", Cultura: " + nome);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void registrarGastoParaCultura(String cpf, int ano, String nomeCultura,
                                          String descricao, String categoria,
                                          double valor, LocalDate data) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            // Validações
            ValidacaoUtil.validarStringNaoVazia(descricao, "Descrição");
            ValidacaoUtil.validarStringNaoVazia(categoria, "Categoria");
            ValidacaoUtil.validarValorNaoNegativo(valor, "Valor");

            if (data.isAfter(LocalDate.now())) {
                System.out.println("❌ Data do gasto não pode ser futura!");
                return;
            }

            Gasto gasto = new Gasto(descricao, categoria, valor, data);
            gastoDAO.cadastrar(cultura.getId(), gasto);
            cultura.adicionarGasto(gasto);

            System.out.println("✅ Gasto registrado com sucesso!");
            LoggerUtil.logOperacao("REGISTRAR GASTO",
                    "Cultura: " + nomeCultura + ", Valor: R$ " + valor);

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void registrarColheitaParaCultura(String cpf, int ano, String nomeCultura,
                                             double quantidade, LocalDate data,
                                             boolean vendida, double precoVenda) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            ValidacaoUtil.validarQuantidade(quantidade);

            if (vendida && precoVenda <= 0) {
                System.out.println("❌ Colheita vendida deve ter preço de venda maior que zero!");
                return;
            }

            Colheita colheita = new Colheita(quantidade, data);
            colheita.setVendida(vendida);
            if (vendida) {
                colheita.setPrecoVenda(precoVenda);
            }

            colheitaDAO.cadastrar(cultura.getId(), colheita);
            cultura.adicionarColheita(colheita);

            System.out.println("✅ Colheita registrada com sucesso!");
            LoggerUtil.logOperacao("REGISTRAR COLHEITA",
                    "Cultura: " + nomeCultura + ", Quantidade: " + quantidade + " kg");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void atualizarProdutor(Produtor produtor) {
        try {
            boolean sucesso = produtorDAO.atualizar(produtor);
            if (sucesso) {
                // Atualizar no cache
                cacheProdutores.put(produtor.getCpf(), produtor);
                System.out.println("✅ Dados do produtor atualizados no banco!");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar produtor: " + e.getMessage());
        }
    }

    public void excluirCulturaDaSafra(String cpf, int ano, String nomeCultura) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            boolean sucesso = culturaDAO.excluir(cultura.getId());
            if (sucesso) {
                safra.removerCultura(nomeCultura);
                System.out.println("✅ Cultura excluída com sucesso!");
                LoggerUtil.logExclusao("CULTURA", cultura.getId(),
                        "Produtor: " + produtor.getNome() + ", Safra: " + ano);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void excluirSafraDoProdutor(String cpf, int ano) {
        try {
            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            boolean sucesso = safraDAO.excluir(safra.getId());
            if (sucesso) {
                produtor.removerSafra(ano);
                System.out.println("✅ Safra excluída com sucesso!");
                LoggerUtil.logExclusao("SAFRA", safra.getId(),
                        "Produtor: " + produtor.getNome() + ", Ano: " + ano);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void cadastrarProdutor() {
        System.out.println("\n=== CADASTRAR NOVO PRODUTOR ===");

        try {
            System.out.print("Nome completo: ");
            String nome = scanner.nextLine();
            ValidacaoUtil.validarNome(nome);

            System.out.print("CPF (somente números): ");
            String cpf = scanner.nextLine();
            ValidacaoUtil.validarCPF(cpf);

            if (produtorDAO.cpfExiste(cpf)) {
                System.out.println("❌ CPF já cadastrado no sistema!");
                LoggerUtil.logOperacao("CADASTRO PRODUTOR", "Tentativa com CPF duplicado: " + cpf);
                return;
            }

            System.out.print("Telefone (com DDD): ");
            String telefone = scanner.nextLine();
            ValidacaoUtil.validarTelefone(telefone);

            Produtor produtor = new Produtor(nome, cpf, telefone);
            int idProdutor = produtorDAO.cadastrar(produtor);

            if (idProdutor > 0) {
                produtor.setId(idProdutor);
                cacheProdutores.put(cpf, produtor);
                System.out.println("✅ Produtor cadastrado com sucesso! ID: " + idProdutor);
                LoggerUtil.logOperacao("CADASTRO PRODUTOR",
                        "ID: " + idProdutor + ", Nome: " + nome + ", CPF: " + cpf);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
            LoggerUtil.logErro("Erro cadastro produtor: " + e.getMessage());
        }
    }

    public void listarProdutores() {
        System.out.println("\n=== LISTA DE PRODUTORES ===");

        try {
            List<Produtor> produtores = produtorDAO.listarTodos();

            if (produtores.isEmpty()) {
                System.out.println("Nenhum produtor cadastrado.");
                return;
            }

            System.out.printf("%-5s %-30s %-15s %-15s%n", "ID", "NOME", "CPF", "TELEFONE");
            System.out.println("-".repeat(70));

            for (Produtor p : produtores) {
                System.out.printf("%-5d %-30s %-15s %-15s%n",
                        p.getId(),
                        p.getNome(),
                        p.getCpf(),
                        p.getTelefone());
            }

            LoggerUtil.logOperacao("LISTAR PRODUTORES", "Total: " + produtores.size());

        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar produtores: " + e.getMessage());
        }
    }

    public void editarProdutor() {
        System.out.println("\n=== EDITAR PRODUTOR ===");

        try {
            System.out.print("CPF do produtor a editar: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.println("\nDados atuais:");
            System.out.println("Nome: " + produtor.getNome());
            System.out.println("Telefone: " + produtor.getTelefone());

            System.out.print("\nNovo nome (ou Enter para manter): ");
            String novoNome = scanner.nextLine();
            if (!novoNome.trim().isEmpty()) {
                ValidacaoUtil.validarNome(novoNome);
                produtor.setNome(novoNome);
            }

            System.out.print("Novo telefone (ou Enter para manter): ");
            String novoTelefone = scanner.nextLine();
            if (!novoTelefone.trim().isEmpty()) {
                ValidacaoUtil.validarTelefone(novoTelefone);
                produtor.setTelefone(novoTelefone);
            }

            boolean sucesso = produtorDAO.atualizar(produtor);
            if (sucesso) {
                cacheProdutores.put(cpf, produtor);
                System.out.println("✅ Produtor atualizado com sucesso!");
                LoggerUtil.logOperacao("EDITAR PRODUTOR",
                        "CPF: " + cpf + ", Novo nome: " + produtor.getNome());
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void cadastrarSafra() {
        System.out.println("\n=== CADASTRAR SAFRA ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());
            ValidacaoUtil.validarAno(ano);

            // RF02.3: Validar ano único por safra/produtor
            if (safraDAO.safraExiste(produtor.getId(), ano)) {
                System.out.println("❌ Este produtor já possui safra cadastrada para o ano " + ano);
                return;
            }

            Safra safra = new Safra(ano);
            int idSafra = safraDAO.cadastrar(produtor.getId(), safra);

            if (idSafra > 0) {
                safra.setId(idSafra);
                produtor.adicionarSafra(safra);
                System.out.println("✅ Safra cadastrada com sucesso! ID: " + idSafra);
                LoggerUtil.logOperacao("CADASTRAR SAFRA",
                        "Produtor: " + produtor.getNome() + ", Ano: " + ano);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void listarSafrasPorProdutor() {
        System.out.println("\n=== SAFRAS POR PRODUTOR ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.println("\nProdutor: " + produtor.getNome());
            System.out.println("Total de safras: " + produtor.getSafras().size());

            if (produtor.getSafras().isEmpty()) {
                System.out.println("Nenhuma safra cadastrada.");
                return;
            }

            for (Safra safra : produtor.getSafras()) {
                System.out.println("\nSafra " + safra.getAno() + ":");
                System.out.println("  Culturas: " + safra.getCulturas().size());
                System.out.println("  Gastos totais: R$ " +
                        String.format("%.2f", safra.calcularTotalGastos()));
                System.out.println("  Receita total: R$ " +
                        String.format("%.2f", safra.calcularTotalReceita()));
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void cadastrarCultura() {
        System.out.println("\n=== CADASTRAR CULTURA ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            System.out.print("Nome da cultura: ");
            String nome = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(nome, "Nome da cultura");

            System.out.print("Variedade: ");
            String variedade = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(variedade, "Variedade");

            System.out.print("Área plantada (hectares): ");
            double area = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarAreaPlantada(area);

            if (culturaDAO.culturaExiste(safra.getId(), nome)) {
                System.out.println("❌ Já existe uma cultura com este nome nesta safra!");
                return;
            }

            Cultura cultura = new Cultura(nome, variedade, area);
            int idCultura = culturaDAO.cadastrar(safra.getId(), cultura);

            if (idCultura > 0) {
                cultura.setId(idCultura);
                safra.adicionarCultura(cultura);
                System.out.println("✅ Cultura cadastrada com sucesso! ID: " + idCultura);
                LoggerUtil.logOperacao("CADASTRAR CULTURA",
                        "Safra: " + ano + ", Cultura: " + nome);
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void excluirCultura() {
        System.out.println("\n=== EXCLUIR CULTURA ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            System.out.print("Nome da cultura: ");
            String nomeCultura = scanner.nextLine();

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            System.out.println("\n⚠️  ATENÇÃO: Esta exclusão será em CASCATA!");
            System.out.println("Serão removidos:");
            System.out.println("  • " + cultura.getGastos().size() + " gastos registrados");
            System.out.println("  • " + cultura.getColheitas().size() + " colheitas registradas");
            System.out.println("  • A cultura: " + cultura.getNome());

            System.out.print("\nConfirma exclusão? (digite 'SIM' para confirmar): ");
            String confirmacao = scanner.nextLine();

            if ("SIM".equalsIgnoreCase(confirmacao)) {
                // RF03.3: Excluir cultura (com gastos e colheitas vinculados)
                boolean sucesso = culturaDAO.excluir(cultura.getId());

                if (sucesso) {
                    safra.removerCultura(nomeCultura);
                    System.out.println("✅ Cultura excluída com sucesso!");
                    LoggerUtil.logExclusao("CULTURA", cultura.getId(),
                            "Produtor: " + produtor.getNome() + ", Safra: " + ano);
                }
            } else {
                System.out.println("❌ Exclusão cancelada.");
            }

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void registrarGasto() {
        System.out.println("\n=== REGISTRAR GASTO ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            System.out.print("Nome da cultura: ");
            String nomeCultura = scanner.nextLine();

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            System.out.print("Descrição do gasto: ");
            String descricao = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(descricao, "Descrição");

            System.out.print("Categoria (ex: Sementes, Fertilizantes, Mão de obra): ");
            String categoria = scanner.nextLine();
            ValidacaoUtil.validarStringNaoVazia(categoria, "Categoria");

            System.out.print("Valor (R$): ");
            double valor = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarValorNaoNegativo(valor, "Valor");

            System.out.print("Data do gasto (AAAA-MM-DD): ");
            String dataStr = scanner.nextLine();
            LocalDate data = ValidacaoUtil.validarData(dataStr, "Data do gasto");

            if (data.isAfter(LocalDate.now())) {
                System.out.println("❌ Data do gasto não pode ser futura!");
                return;
            }

            Gasto gasto = new Gasto(descricao, categoria, valor, data);
            gastoDAO.cadastrar(cultura.getId(), gasto);
            cultura.adicionarGasto(gasto);

            System.out.println("✅ Gasto registrado com sucesso!");
            LoggerUtil.logOperacao("REGISTRAR GASTO",
                    "Cultura: " + nomeCultura + ", Valor: R$ " + valor);

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }

    public void registrarColheita() {
        System.out.println("\n=== REGISTRAR COLHEITA ===");

        try {
            System.out.print("CPF do produtor: ");
            String cpf = scanner.nextLine();

            Produtor produtor = buscarProdutorPorCPF(cpf);
            if (produtor == null) {
                System.out.println("❌ Produtor não encontrado!");
                return;
            }

            System.out.print("Ano da safra: ");
            int ano = Integer.parseInt(scanner.nextLine());

            Safra safra = produtor.buscarSafraPorAno(ano);
            if (safra == null) {
                System.out.println("❌ Safra não encontrada!");
                return;
            }

            System.out.print("Nome da cultura: ");
            String nomeCultura = scanner.nextLine();

            Cultura cultura = safra.buscarCulturaPorNome(nomeCultura);
            if (cultura == null) {
                System.out.println("❌ Cultura não encontrada!");
                return;
            }

            System.out.print("Quantidade colhida (kg): ");
            double quantidade = Double.parseDouble(scanner.nextLine().replace(",", "."));
            ValidacaoUtil.validarQuantidade(quantidade);

            System.out.print("Data da colheita (AAAA-MM-DD): ");
            String dataStr = scanner.nextLine();
            LocalDate data = ValidacaoUtil.validarData(dataStr, "Data da colheita");

            System.out.print("Foi vendida? (s/n): ");
            boolean vendida = scanner.nextLine().equalsIgnoreCase("s");

            double precoVenda = 0;
            if (vendida) {
                System.out.print("Preço de venda por kg (R$): ");
                precoVenda = Double.parseDouble(scanner.nextLine().replace(",", "."));
                ValidacaoUtil.validarValorNaoNegativo(precoVenda, "Preço de venda");

                if (precoVenda <= 0) {
                    System.out.println("❌ Colheita vendida deve ter preço de venda maior que zero!");
                    return;
                }
            }

            Colheita colheita = new Colheita(quantidade, data);
            colheita.setVendida(vendida);
            if (vendida) {
                colheita.setPrecoVenda(precoVenda);
            }

            colheitaDAO.cadastrar(cultura.getId(), colheita);
            cultura.adicionarColheita(colheita);

            System.out.println("✅ Colheita registrada com sucesso!");
            LoggerUtil.logOperacao("REGISTRAR COLHEITA",
                    "Cultura: " + nomeCultura + ", Quantidade: " + quantidade + " kg");

        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
}