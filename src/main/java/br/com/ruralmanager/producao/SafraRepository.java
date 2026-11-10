package br.com.ruralmanager.producao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Toda busca por id recebe o produtor dono. Nunca use findById com um id vindo
 * do cliente: ele devolveria o registro de qualquer produtor.
 */
public interface SafraRepository extends JpaRepository<Safra, Long> {

    List<Safra> findByProdutor_IdOrderByAnoInicioDesc(Long produtorId);

    Optional<Safra> findByIdAndProdutor_Id(Long id, Long produtorId);

    boolean existsByProdutor_IdAndAnoInicioAndAnoFim(Long produtorId, short anoInicio, short anoFim);

    boolean existsByProdutor_IdAndAnoInicioAndAnoFimAndIdNot(Long produtorId, short anoInicio, short anoFim, Long id);
}
