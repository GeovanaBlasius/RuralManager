package br.com.ruralmanager.suporte;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoSuporteRepository extends JpaRepository<ChamadoSuporte, Long> {

    /** So os chamados do proprio usuario: cada um ve so o que relatou. */
    List<ChamadoSuporte> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    /** Busca por id restrita a quem relatou: nao se edita nem exclui chamado alheio. */
    Optional<ChamadoSuporte> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Visao da equipe (ADMIN): todos os chamados, mais recentes primeiro. */
    Page<ChamadoSuporte> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
