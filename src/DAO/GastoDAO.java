package DAO;

import database.ConnectionFactory;
import model.Gasto;
import util.ValidacaoUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {

    public Gasto buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT id, descricao, categoria, valor, data, cultura_id FROM gasto WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Gasto g = new Gasto(
                            rs.getString("descricao"),
                            rs.getString("categoria"),
                            rs.getDouble("valor"),
                            rs.getDate("data").toLocalDate()
                    );
                    g.setId(rs.getInt("id"));
                    g.setCulturaId(rs.getInt("cultura_id"));
                    return g;
                }
            }
        }
        return null;
    }

    public boolean atualizar(Gasto gasto) throws SQLException {
        String sql = "UPDATE gasto SET descricao = ?, categoria = ?, valor = ?, data = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gasto.getDescricao());
            ps.setString(2, gasto.getCategoria());
            ps.setDouble(3, gasto.getValor());
            ps.setDate(4, Date.valueOf(gasto.getData()));
            ps.setInt(5, gasto.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Integer id) throws SQLException {
        String sql = "DELETE FROM gasto WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int cadastrar(Integer culturaId, Gasto g) throws SQLException {
        ValidacaoUtil.validarNaoNegativo(g.getValor(), "Valor do gasto");
        ValidacaoUtil.validarDataNaoFutura(g.getData(), "Data do gasto");
        ValidacaoUtil.validarStringNaoVazia(g.getDescricao(), "Descricao");
        ValidacaoUtil.validarStringNaoVazia(g.getCategoria(), "Categoria");

        String sql = "INSERT INTO gasto (cultura_id, descricao, categoria, valor, data) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, culturaId);
            ps.setString(2, g.getDescricao());
            ps.setString(3, g.getCategoria());
            ps.setDouble(4, g.getValor());
            ps.setDate(5, Date.valueOf(g.getData()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    g.setId(idGerado);
                    g.setCulturaId(culturaId);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public List<Gasto> listarPorCultura(Integer culturaId) throws SQLException {
        String sql = "SELECT id, descricao, categoria, valor, data FROM gasto WHERE cultura_id = ? ORDER BY data DESC";
        List<Gasto> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Gasto g = new Gasto(
                            rs.getString("descricao"),
                            rs.getString("categoria"),
                            rs.getDouble("valor"),
                            rs.getDate("data").toLocalDate()
                    );
                    g.setId(rs.getInt("id"));
                    g.setCulturaId(culturaId);
                    lista.add(g);
                }
            }
        }
        return lista;
    }

    public double somarPorCultura(Integer culturaId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor),0) AS total FROM gasto WHERE cultura_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    public boolean excluirTodosPorCultura(Integer culturaId) throws SQLException {
        String sql = "DELETE FROM gasto WHERE cultura_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            return ps.executeUpdate() > 0;
        }
    }
}