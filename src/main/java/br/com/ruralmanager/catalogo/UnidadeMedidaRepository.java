package br.com.ruralmanager.catalogo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {

    List<UnidadeMedida> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
}
