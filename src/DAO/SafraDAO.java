package DAO;

import database.ConnectionFactory;
import model.Safra;
import util.ValidacaoUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SafraDAO {

    public int cadastrar(Integer produtorId, int ano) throws SQLException {
        ValidacaoUtil.validarAnoSafra(ano);
        String sql = "INSERT INTO safra (produtor_id, ano) VALUES (?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, produtorId);
            ps.setInt(2, ano);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public int cadastrar(Integer produtorId, Safra safra) throws SQLException {
        ValidacaoUtil.validarAnoSafra(safra.getAno());
        String sql = "INSERT INTO safra (produtor_id, ano, descricao) VALUES (?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, produtorId);
            ps.setInt(2, safra.getAno());
            ps.setString(3, safra.getDescricao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    safra.setId(idGerado);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public boolean safraExiste(Integer produtorId, int ano) throws SQLException {
        return anoExiste(produtorId, ano);
    }

    public boolean anoExiste(Integer produtorId, int ano) throws SQLException {
        String sql = "SELECT 1 FROM safra WHERE produtor_id = ? AND ano = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produtorId);
            ps.setInt(2, ano);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Safra> listarPorProdutor(Integer produtorId) throws SQLException {
        String sql = "SELECT id, ano, descricao FROM safra WHERE produtor_id = ? ORDER BY ano DESC";
        List<Safra> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produtorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Safra s = new Safra(rs.getInt("ano"));
                    s.setId(rs.getInt("id"));
                    s.setDescricao(rs.getString("descricao"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }

    public Safra buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT id, ano, descricao, produtor_id FROM safra WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Safra s = new Safra(rs.getInt("ano"));
                    s.setId(rs.getInt("id"));
                    s.setDescricao(rs.getString("descricao"));
                    s.setProdutorId(rs.getInt("produtor_id"));
                    return s;
                }
            }
        }
        return null;
    }

    public Integer buscarIdPorProdutorEAno(Integer produtorId, int ano) throws SQLException {
        String sql = "SELECT id FROM safra WHERE produtor_id = ? AND ano = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produtorId);
            ps.setInt(2, ano);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    public boolean atualizar(Safra safra) throws SQLException {
        String sql = "UPDATE safra SET ano = ?, descricao = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safra.getAno());
            ps.setString(2, safra.getDescricao());
            ps.setInt(3, safra.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Integer id) throws SQLException {
        String sql = "DELETE FROM safra WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluirPorProdutorEAno(Integer produtorId, int ano) throws SQLException {
        String sql = "DELETE FROM safra WHERE produtor_id = ? AND ano = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, produtorId);
            ps.setInt(2, ano);
            return ps.executeUpdate() > 0;
        }
    }
}