package br.org.amde.digital.repository;

import br.org.amde.digital.domain.entity.Usuario;
import br.org.amde.digital.domain.enums.NivelAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("SELECT u.nivelAcesso, COUNT(u) FROM Usuario u GROUP BY u.nivelAcesso")
    List<Object[]> contarAtivosPorNivelAcesso();

    long countByNivelAcesso(NivelAcesso nivelAcesso);
}
