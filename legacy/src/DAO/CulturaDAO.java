package DAO;

import database.ConnectionFactory;
import model.Cultura;
import util.ValidacaoUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CulturaDAO {

    public boolean culturaExiste(Integer safraId, String nome) throws SQLException {
        return nomeExisteNaSafra(safraId, nome);
    }

    public int cadastrar(Integer safraId, Cultura c) throws SQLException {
        ValidacaoUtil.validarStringNaoVazia(c.getNome(), "Nome da cultura");
        ValidacaoUtil.validarStringNaoVazia(c.getVariedade(), "Variedade");
        ValidacaoUtil.validarAreaPlantada(c.getAreaPlantada());

        String sql = "INSERT INTO cultura (safra_id, nome, variedade, area_plantada) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, safraId);
            ps.setString(2, c.getNome());
            ps.setString(3, c.getVariedade());
            ps.setDouble(4, c.getAreaPlantada());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    c.setId(idGerado);
                    c.setSafraId(safraId);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public boolean nomeExisteNaSafra(Integer safraId, String nome) throws SQLException {
        String sql = "SELECT 1 FROM cultura WHERE safra_id = ? AND nome = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safraId);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Cultura> listarPorSafra(Integer safraId) throws SQLException {
        String sql = "SELECT id, nome, variedade, area_plantada FROM cultura WHERE safra_id = ? ORDER BY nome";
        List<Cultura> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safraId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cultura c = new Cultura(
                            rs.getString("nome"),
                            rs.getString("variedade"),
                            rs.getDouble("area_plantada")
                    );
                    c.setId(rs.getInt("id"));
                    c.setSafraId(safraId); // ⚠️ IMPORTANTE: Setar safraId
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    public Cultura buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT id, nome, variedade, area_plantada, safra_id FROM cultura WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cultura c = new Cultura(
                            rs.getString("nome"),
                            rs.getString("variedade"),
                            rs.getDouble("area_plantada")
                    );
                    c.setId(rs.getInt("id"));
                    c.setSafraId(rs.getInt("safra_id"));
                    return c;
                }
            }
        }
        return null;
    }

    public Cultura buscarPorNomeESafra(Integer safraId, String nome) throws SQLException {
        String sql = "SELECT id, nome, variedade, area_plantada FROM cultura WHERE safra_id = ? AND nome = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safraId);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cultura c = new Cultura(
                            rs.getString("nome"),
                            rs.getString("variedade"),
                            rs.getDouble("area_plantada")
                    );
                    c.setId(rs.getInt("id"));
                    c.setSafraId(safraId);
                    return c;
                }
            }
        }
        return null;
    }

    public boolean excluir(Integer id) throws SQLException {
        String sql = "DELETE FROM cultura WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean atualizar(Cultura cultura) throws SQLException {
        String sql = "UPDATE cultura SET nome = ?, variedade = ?, area_plantada = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cultura.getNome());
            ps.setString(2, cultura.getVariedade());
            ps.setDouble(3, cultura.getAreaPlantada());
            ps.setInt(4, cultura.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public Integer buscarIdPorNome(Integer safraId, String nome) throws SQLException {
        String sql = "SELECT id FROM cultura WHERE safra_id = ? AND nome = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safraId);
            ps.setString(2, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
                return null;
            }
        }
    }

    public boolean excluirPorNome(Integer safraId, String nome) throws SQLException {
        String sql = "DELETE FROM cultura WHERE safra_id = ? AND nome = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, safraId);
            ps.setString(2, nome);
            return ps.executeUpdate() > 0;
        }
    }
}