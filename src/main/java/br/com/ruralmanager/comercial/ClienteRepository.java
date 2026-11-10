package br.com.ruralmanager.comercial;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Page<Cliente> findByProdutorIdOrderByNome(Long produtorId, Pageable pageable);

    Optional<Cliente> findByIdAndProdutorId(Long id, Long produtorId);

    boolean existsByProdutorIdAndCpfCnpj(Long produtorId, String cpfCnpj);

    boolean existsByProdutorIdAndCpfCnpjAndIdNot(Long produtorId, String cpfCnpj, Long id);
}
