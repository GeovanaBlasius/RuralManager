package br.com.ruralmanager.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca pelo e-mail, usado na autenticacao.
     *
     * Devolve tambem usuarios inativos de proposito: quem decide o que fazer
     * com um usuario desativado e o servico de autenticacao, que registra o
     * motivo real no log e devolve ao cliente sempre a mesma mensagem
     * generica, para nao revelar quais e-mails existem no sistema.
     */
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /** Usuarios de um produtor. Usado na tela de gestao de acessos. */
    List<Usuario> findByProdutor_IdOrderByNome(Long produtorId);

    /**
     * Busca por id restrita ao produtor informado.
     *
     * Toda consulta que recebe um identificador vindo do cliente deve passar
     * por aqui, e nao por findById. Sem o filtro de dono na propria consulta,
     * trocar o numero na URL devolve o registro de outro produtor.
     */
    Optional<Usuario> findByIdAndProdutor_Id(Long id, Long produtorId);
}
