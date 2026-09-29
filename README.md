# Rural Manager

Sistema de gestão rural em Java com banco de dados MySQL, desenvolvido como trabalho final da disciplina.

**Disciplina:** Programação Orientada a Objetos I · trabalho final  
**Tecnologias:** Java, MySQL, JDBC

## Funcionalidades

- Cadastro e seleção de produtores rurais
- Registro de culturas, safras, colheitas e gastos por produtor
- Relatórios e cálculos por produtor (produção, custos)
- Exportação de dados
- Backup do banco em `.sql` e registro de logs
- Inserção de dados de exemplo para testes

## Arquitetura

O projeto segue o padrão **DAO**, separando as camadas:

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades: `Produtor`, `Cultura`, `Safra`, `Colheita`, `Gasto` |
| `DAO` | Acesso ao banco para cada entidade |
| `service` | Regras de negócio, menus, cálculos e relatórios |
| `database` | Conexão (`ConnectionFactory`), logs e dados de exemplo |
| `util` | Validação, formatação e exportação |
| `app` | Classe `Main` |

## Como executar

1. Tenha um servidor **MySQL** rodando localmente.
2. Ajuste `config.properties` com a URL, o usuário e a senha do seu banco.
3. Adicione `lib/mysql-connector-j-9.5.0.jar` às bibliotecas do projeto no IntelliJ.
4. Rode `src/app/Main.java` e use o menu no terminal.

---

Desenvolvido por [Geovana Blasius](https://github.com/GeovanaBlasius) · Ciência da Computação, IFC Campus Rio do Sul
