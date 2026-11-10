package br.com.ruralmanager.comercial;

import java.math.BigDecimal;
import br.com.ruralmanager.producao.TotalPorColheita;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemFaturaRepository extends JpaRepository<ItemFatura, Long> {

    List<ItemFatura> findByFaturaId(Long faturaId);

    List<ItemFatura> findByFaturaIdIn(List<Long> faturaIds);

    boolean existsByColheitaId(Long colheitaId);

    /** Total ja vendido de uma colheita, usado para nao vender mais do que foi colhido. */
    @Query("select coalesce(sum(i.quantidade), 0) from ItemFatura i where i.colheita.id = :colheitaId")
    BigDecimal totalVendido(@Param("colheitaId") Long colheitaId);

    /** Igual ao anterior, desconsiderando os itens de uma fatura (a que esta sendo editada). */
    @Query("select coalesce(sum(i.quantidade), 0) from ItemFatura i "
            + "where i.colheita.id = :colheitaId and i.fatura.id <> :faturaId")
    BigDecimal totalVendidoExceto(@Param("colheitaId") Long colheitaId, @Param("faturaId") Long faturaId);

    @Modifying(flushAutomatically = true)
    @Query("delete from ItemFatura i where i.fatura.id = :faturaId")
    void excluirDaFatura(@Param("faturaId") Long faturaId);

    @Query("select i.colheita.id as colheitaId, sum(i.quantidade) as total from ItemFatura i "
            + "where i.colheita.id in :ids group by i.colheita.id")
    List<TotalPorColheita> totaisVendidos(@Param("ids") Collection<Long> ids);
}
