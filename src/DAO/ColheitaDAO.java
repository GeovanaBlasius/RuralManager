package DAO;

import database.ConnectionFactory;
import model.Colheita;
import util.ValidacaoUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColheitaDAO {

    public Colheita buscarPorId(Integer id) throws SQLException {
        String sql = "SELECT id, quantidade, preco_venda, vendida, data_colheita, cultura_id FROM colheita WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Colheita c = new Colheita(
                            rs.getDouble("quantidade"),
                            rs.getDate("data_colheita").toLocalDate()
                    );
                    c.setId(rs.getInt("id"));
                    c.setPrecoVenda(rs.getDouble("preco_venda"));
                    c.setVendida(rs.getBoolean("vendida"));
                    c.setCulturaId(rs.getInt("cultura_id"));
                    return c;
                }
            }
        }
        return null;
    }

    public boolean atualizar(Colheita colheita) throws SQLException {
        String sql = "UPDATE colheita SET quantidade = ?, preco_venda = ?, vendida = ?, data_colheita = ? WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, colheita.getQuantidade());
            ps.setDouble(2, colheita.getPrecoVenda());
            ps.setBoolean(3, colheita.isVendida());
            ps.setDate(4, Date.valueOf(colheita.getDataColheita()));
            ps.setInt(5, colheita.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean excluir(Integer id) throws SQLException {
        String sql = "DELETE FROM colheita WHERE id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int cadastrar(Integer culturaId, Colheita c) throws SQLException {
        ValidacaoUtil.validarNaoNegativo(c.getQuantidade(), "Quantidade");
        ValidacaoUtil.validarDataNaoFutura(c.getDataColheita(), "Data da colheita");
        ValidacaoUtil.validarNaoNegativo(c.getPrecoVenda(), "Preco de venda");
        if (c.isVendida() && c.getPrecoVenda() <= 0) {
            throw new IllegalArgumentException("Colheita vendida deve ter preco de venda maior que zero!");
        }

        String sql = "INSERT INTO colheita (cultura_id, quantidade, preco_venda, vendida, data_colheita) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, culturaId);
            ps.setDouble(2, c.getQuantidade());
            ps.setDouble(3, c.getPrecoVenda());
            ps.setBoolean(4, c.isVendida());
            ps.setDate(5, Date.valueOf(c.getDataColheita()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    c.setId(idGerado);
                    c.setCulturaId(culturaId);
                    return idGerado;
                }
            }
        }
        return -1;
    }

    public List<Colheita> listarPorCultura(Integer culturaId) throws SQLException {
        String sql = "SELECT id, quantidade, preco_venda, vendida, data_colheita FROM colheita WHERE cultura_id = ? ORDER BY data_colheita DESC";
        List<Colheita> lista = new ArrayList<>();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Colheita c = new Colheita(
                            rs.getDouble("quantidade"),
                            rs.getDate("data_colheita").toLocalDate()
                    );
                    c.setId(rs.getInt("id"));
                    c.setPrecoVenda(rs.getDouble("preco_venda"));
                    c.setVendida(rs.getBoolean("vendida"));
                    c.setCulturaId(culturaId);
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    public double receitaPorCultura(Integer culturaId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE WHEN vendida THEN quantidade * preco_venda ELSE 0 END),0) AS receita FROM colheita WHERE cultura_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("receita");
            }
        }
        return 0.0;
    }

    public double totalColhidoPorCultura(Integer culturaId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantidade),0) AS total FROM colheita WHERE cultura_id = ?";
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
        String sql = "DELETE FROM colheita WHERE cultura_id = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, culturaId);
            return ps.executeUpdate() > 0;
        }
    }
}