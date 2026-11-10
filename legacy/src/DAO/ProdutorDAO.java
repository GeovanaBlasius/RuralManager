package DAO;

import database.ConnectionFactory;
import model.Produtor;
import model.Safra;
import model.Cultura;
import model.Gasto;
import model.Colheita;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutorDAO {

    public Produtor buscarCompletoPorId(Integer id) throws SQLException {
        // Busca o produtor básico
        Produtor produtor = buscarPorId(id);
        if (produtor == null) {
            return null;
        }

        carregarSafras(produtor);

        for (Safra safra : produtor.getSafras()) {
            carregarCulturas(safra);

            for (Cultura cultura : safra.getCulturas()) {
                carregarGastos(cultura);
                carregarColheitas(cultura);
            }
        }

        return produtor;
    }

    private void carregarSafras(Produtor produtor) throws SQLException {
        SafraDAO safraDAO = new SafraDAO();
        List<Safra> safras = safraDAO.listarPorProdutor(produtor.getId());
        produtor.getSafras().clear();
        produtor.getSafras().addAll(safras);
    }

    private void carregarCulturas(Safra safra) throws SQLException {
        CulturaDAO culturaDAO = new CulturaDAO();
        List<Cultura> culturas = culturaDAO.listarPorSafra(safra.getId());
        safra.getCulturas().clear();
        safra.getCulturas().addAll(culturas);
    }

    private void carregarGastos(Cultura cultura) throws SQLException {
        GastoDAO gastoDAO = new GastoDAO();
        List<Gasto> gastos = gastoDAO.listarPorCultura(cultura.getId());
        cultura.getGastos().clear();
        cultura.getGastos().addAll(gastos);
    }

    private void carregarColheitas(Cultura cultura) throws SQLException {
        ColheitaDAO colheitaDAO = new ColheitaDAO();
        List<Colheita> colheitas = colheitaDAO.listarPorCultura(cultura.getId());
        cultura.getColheitas().clear();
        cultura.getColheitas().addAll(colheitas);
    }

    public Produtor buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone FROM produtor WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produtor p = new Produtor(
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("telefone")
                    );
                    p.setId(rs.getInt("id"));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Produtor> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone FROM produtor ORDER BY nome";
        List<Produtor> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Produtor p = new Produtor(
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("telefone")
                );
                p.setId(rs.getInt("id"));
                lista.add(p);
            }
        }
        return lista;
    }

    public List<Produtor> getAll() throws SQLException {
        return listarTodos(); // Chama o mesmo método
    }

    public boolean cpfExiste(String cpf) throws SQLException {
        String sql = "SELECT 1 FROM produtor WHERE cpf = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int cadastrar(Produtor produtor) throws SQLException {
        String sql = "INSERT INTO produtor (nome, cpf, telefone) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produtor.getNome());
            ps.setString(2, produtor.getCpf());
            ps.setString(3, produtor.getTelefone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    produtor.setId(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public int insert(Produtor produtor) throws SQLException {
        return cadastrar(produtor);
    }

    public Produtor buscarPorCPF(String cpf) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone FROM produtor WHERE cpf = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Produtor p = new Produtor(
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("telefone")
                    );
                    p.setId(rs.getInt("id"));
                    return p;
                }
            }
        }
        return null;
    }

    public Produtor getByCpf(String cpf) throws SQLException {
        return buscarPorCPF(cpf);
    }

    public boolean atualizar(Produtor produtor) throws SQLException {
        String sql = "UPDATE produtor SET nome = ?, cpf = ?, telefone = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, produtor.getNome());
            ps.setString(2, produtor.getCpf());
            ps.setString(3, produtor.getTelefone());
            ps.setInt(4, produtor.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Produtor produtor) throws SQLException {
        return atualizar(produtor);
    }

    public boolean excluir(Integer id) throws SQLException {
        String sql = "DELETE FROM produtor WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluirPorCPF(String cpf) throws SQLException {
        String sql = "DELETE FROM produtor WHERE cpf = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpf);
            return ps.executeUpdate() > 0;
        }
    }

    public Integer buscarIdPorCPF(String cpf) throws SQLException {
        String sql = "SELECT id FROM produtor WHERE cpf = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return null;
    }

    public boolean atualizarTelefone(String cpf, String novoTelefone) throws SQLException {
        String sql = "UPDATE produtor SET telefone = ? WHERE cpf = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, novoTelefone);
            ps.setString(2, cpf);
            return ps.executeUpdate() > 0;
        }
    }
}