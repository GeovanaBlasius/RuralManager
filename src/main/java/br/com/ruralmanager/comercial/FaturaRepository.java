package br.com.ruralmanager.comercial;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    Page<Fatura> findByClienteProdutorIdOrderByDataCompraDescIdDesc(Long produtorId, Pageable pageable);

    Optional<Fatura> findByIdAndClienteProdutorId(Long id, Long produtorId);

    boolean existsByClienteId(Long clienteId);
}
