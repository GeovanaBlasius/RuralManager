package br.com.ruralmanager.producao;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColheitaRepository extends JpaRepository<Colheita, Long> {

    Page<Colheita> findByCultura_Safra_Produtor_IdOrderByDataColheitaDesc(Long produtorId, Pageable pageable);

    Page<Colheita> findByCulturaIdAndCultura_Safra_Produtor_IdOrderByDataColheitaDesc(
            Long culturaId, Long produtorId, Pageable pageable);

    Optional<Colheita> findByIdAndCultura_Safra_Produtor_Id(Long id, Long produtorId);

    boolean existsByCulturaId(Long culturaId);

    boolean existsByCulturaIdAndDataColheitaBefore(Long culturaId, LocalDate data);

    /**
     * Trava a linha da colheita ate o fim da transacao. Toda operacao que mexe no
     * estoque (venda, saida, reducao da quantidade) passa por aqui, o que serializa
     * as operacoes concorrentes sobre a mesma colheita e impede vender duas vezes
     * a mesma saca. Usar somente depois de confirmar que a colheita e do produtor.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Colheita c where c.id = :id")
    Optional<Colheita> bloquear(@Param("id") Long id);
}
