package br.com.ruralmanager.produtor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutorRepository extends JpaRepository<Produtor, Long> {

    Optional<Produtor> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
