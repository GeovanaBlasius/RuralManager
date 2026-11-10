package br.com.ruralmanager.producao;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaidaRepository extends JpaRepository<Saida, Long> {

    Page<Saida> findByColheita_Cultura_Safra_Produtor_IdOrderByDataSaidaDesc(Long produtorId, Pageable pageable);

    Optional<Saida> findByIdAndColheita_Cultura_Safra_Produtor_Id(Long id, Long produtorId);

    boolean existsByColheitaId(Long colheitaId);

    /** Total ja destinado sem venda, usado junto com o vendido para calcular o estoque. */
    @Query("select coalesce(sum(s.quantidade), 0) from Saida s where s.colheita.id = :colheitaId")
    BigDecimal totalDestinado(@Param("colheitaId") Long colheitaId);

    /** Igual ao anterior, desconsiderando uma saida (a que esta sendo editada). */
    @Query("select coalesce(sum(s.quantidade), 0) from Saida s "
            + "where s.colheita.id = :colheitaId and s.id <> :saidaId")
    BigDecimal totalDestinadoExceto(@Param("colheitaId") Long colheitaId, @Param("saidaId") Long saidaId);

    @Query("select s.colheita.id as colheitaId, sum(s.quantidade) as total from Saida s "
            + "where s.colheita.id in :ids group by s.colheita.id")
    List<TotalPorColheita> totaisDestinados(@Param("ids") Collection<Long> ids);
}
