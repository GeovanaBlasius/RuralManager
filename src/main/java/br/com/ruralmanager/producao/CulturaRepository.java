package br.com.ruralmanager.producao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CulturaRepository extends JpaRepository<Cultura, Long> {

    List<Cultura> findBySafraIdAndSafra_Produtor_IdOrderByDataPlantioDesc(Long safraId, Long produtorId);

    List<Cultura> findBySafra_Produtor_IdOrderByDataPlantioDesc(Long produtorId);

    Optional<Cultura> findByIdAndSafra_Produtor_Id(Long id, Long produtorId);

    boolean existsBySafraId(Long safraId);

    boolean existsBySafraIdAndProdutoIdAndVariedadeIgnoreCase(Long safraId, Long produtoId, String variedade);

    boolean existsBySafraIdAndProdutoIdAndVariedadeIgnoreCaseAndIdNot(Long safraId, Long produtoId,
                                                                      String variedade, Long id);
}
