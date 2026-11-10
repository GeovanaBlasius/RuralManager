package br.com.ruralmanager.producao;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    Page<Gasto> findBySafraProdutor_IdOrderByDataGastoDesc(Long produtorId, Pageable pageable);

    Page<Gasto> findBySafraIdAndSafraProdutor_IdOrderByDataGastoDesc(Long safraId, Long produtorId, Pageable pageable);

    Optional<Gasto> findByIdAndSafraProdutor_Id(Long id, Long produtorId);

    boolean existsBySafraId(Long safraId);

    boolean existsByCulturaId(Long culturaId);

    boolean existsByColheitaId(Long colheitaId);
}
